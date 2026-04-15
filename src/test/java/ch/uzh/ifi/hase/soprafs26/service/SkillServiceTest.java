package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;
    @Mock
    private SkillMapRepository skillMapRepository;
    @Mock
    private SkillMapMembershipRepository skillMapMembershipRepository;
    @Mock
    private DependencyRepository dependencyRepository;

    @InjectMocks
    private SkillService skillService;

    // ─── fixtures ─────────────────────────────────────────────────────────────

    private User owner;
    private User otherUser;
    private SkillMap skillMap;
    private Skill skill;

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setId(1L);

        otherUser = new User();
        otherUser.setId(2L);

        skillMap = new SkillMap();
        skillMap.setId(10L);
        skillMap.setTitle("Test Map");
        skillMap.setOwnerId(owner.getId());
        skillMap.setNumberOfLevels(3);
        skillMap.setIsPublic(true);

        skill = new Skill();
        skill.setId(5L);
        skill.setName("Recursion");
        skill.setLevel(2);
        skill.setSkillMap(skillMap);
        skill.setIsLocked(false);
    }

    // ─── createSkill ──────────────────────────────────────────────────────────

    @Test
    void createSkill_validInput_returnsCreatedSkill() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillRepository.save(any(Skill.class))).willReturn(skill);

        Skill input = new Skill();
        input.setName("Recursion");
        input.setLevel(2);

        Skill result = skillService.createSkill(10L, input, owner);

        assertEquals("Recursion", result.getName());
        assertEquals(skillMap, result.getSkillMap());
        assertFalse(result.getIsLocked());
        verify(skillRepository).save(any(Skill.class));
    }

    @Test
    void createSkill_notOwner_throws403() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));

        Skill input = new Skill();
        input.setName("Recursion");
        input.setLevel(2);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillService.createSkill(10L, input, otherUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(skillRepository, never()).save(any());
    }

    @Test
    void createSkill_blankName_throws400() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));

        Skill input = new Skill();
        input.setName("   ");
        input.setLevel(2);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillService.createSkill(10L, input, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createSkill_levelTooHigh_throws400() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));

        Skill input = new Skill();
        input.setName("Recursion");
        input.setLevel(99);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillService.createSkill(10L, input, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createSkill_levelZero_throws400() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));

        Skill input = new Skill();
        input.setName("Recursion");
        input.setLevel(0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillService.createSkill(10L, input, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createSkill_skillMapNotFound_throws404() {
        given(skillMapRepository.findById(99L)).willReturn(Optional.empty());

        Skill input = new Skill();
        input.setName("Recursion");
        input.setLevel(1);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillService.createSkill(99L, input, owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ─── deleteSkill ──────────────────────────────────────────────────────────

    @Test
    void deleteSkill_owner_deletesSuccessfully() {
        given(skillRepository.findById(5L)).willReturn(Optional.of(skill));

        assertDoesNotThrow(() -> skillService.deleteSkill(5L, owner));
        verify(skillRepository).delete(skill);
    }

    @Test
    void deleteSkill_notOwner_throws403() {
        given(skillRepository.findById(5L)).willReturn(Optional.of(skill));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillService.deleteSkill(5L, otherUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(skillRepository, never()).delete(any());
    }

    @Test
    void deleteSkill_notFound_throws404() {
        given(skillRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillService.deleteSkill(99L, owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ─── getSkillsByMap ───────────────────────────────────────────────────────

    @Test
    void getSkillsByMap_owner_returnsAllSkills() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillRepository.findBySkillMap(skillMap)).willReturn(List.of(skill));

        List<Skill> result = skillService.getSkillsByMap(10L, owner);

        assertEquals(1, result.size());
        assertEquals("Recursion", result.get(0).getName());
    }

    @Test
    void getSkillsByMap_nonMember_throws403() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, otherUser.getId())).willReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillService.getSkillsByMap(10L, otherUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void getSkillsByMap_mapNotFound_throws404() {
        given(skillMapRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillService.getSkillsByMap(99L, owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ─── getSkillById ─────────────────────────────────────────────────────────

    @Test
    void getSkillById_owner_returnsSkill() {
        given(skillRepository.findById(5L)).willReturn(Optional.of(skill));

        Skill result = skillService.getSkillById(5L, owner);

        assertEquals(skill.getId(), result.getId());
        assertEquals("Recursion", result.getName());
    }

    @Test
    void getSkillById_member_returnsSkill() {
        given(skillRepository.findById(5L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, otherUser.getId())).willReturn(true);

        Skill result = skillService.getSkillById(5L, otherUser);

        assertEquals(skill.getId(), result.getId());
    }

    @Test
    void getSkillById_nonMember_throws403() {
        given(skillRepository.findById(5L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, otherUser.getId())).willReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillService.getSkillById(5L, otherUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void getSkillById_notFound_throws404() {
        given(skillRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillService.getSkillById(99L, owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ─── updateSkill ──────────────────────────────────────────────────────────

    @Test
    void updateSkill_name_updatesCorrectly() {
        given(skillRepository.findById(5L)).willReturn(Optional.of(skill));
        given(skillRepository.save(any(Skill.class))).willReturn(skill);

        Skill updates = new Skill();
        updates.setName("Updated Name");

        Skill result = skillService.updateSkill(5L, updates, owner);

        assertEquals("Updated Name", result.getName());
        verify(skillRepository).save(skill);
    }

    @Test
    void updateSkill_level_updatesCorrectly() {
        given(skillRepository.findById(5L)).willReturn(Optional.of(skill));
        given(skillRepository.save(any(Skill.class))).willReturn(skill);

        Skill updates = new Skill();
        updates.setLevel(3);

        Skill result = skillService.updateSkill(5L, updates, owner);

        assertEquals(3, result.getLevel());
    }

    @Test
    void updateSkill_notOwner_throws403() {
        given(skillRepository.findById(5L)).willReturn(Optional.of(skill));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillService.updateSkill(5L, new Skill(), otherUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(skillRepository, never()).save(any());
    }

    @Test
    void updateSkill_levelOutOfRange_throws400() {
        given(skillRepository.findById(5L)).willReturn(Optional.of(skill));

        Skill updates = new Skill();
        updates.setLevel(99);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillService.updateSkill(5L, updates, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void updateSkill_notFound_throws404() {
        given(skillRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillService.updateSkill(99L, new Skill(), owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}