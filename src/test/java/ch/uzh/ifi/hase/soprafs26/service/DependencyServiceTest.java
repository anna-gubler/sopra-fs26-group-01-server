package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;

@ExtendWith(MockitoExtension.class)
class DependencyServiceTest {

    @Mock private SkillMapRepository skillMapRepository;
    @Mock private SkillMapMembershipRepository skillMapMembershipRepository;
    @Mock private SkillRepository skillRepository;
    @Mock private DependencyRepository dependencyRepository;

    @InjectMocks
    private DependencyService dependencyService;

    private User owner;
    private User otherUser;
    private SkillMap skillMap;
    private Skill fromSkill;
    private Skill toSkill;
    private Dependency dependency;

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setId(1L);

        otherUser = new User();
        otherUser.setId(2L);

        skillMap = new SkillMap();
        skillMap.setId(10L);
        skillMap.setOwnerId(1L);

        fromSkill = new Skill();
        fromSkill.setId(100L);
        fromSkill.setLevel(1);
        fromSkill.setSkillMap(skillMap);

        toSkill = new Skill();
        toSkill.setId(200L);
        toSkill.setLevel(2);
        toSkill.setSkillMap(skillMap);

        dependency = new Dependency();
        dependency.setId(1000L);
        dependency.setFromSkill(fromSkill);
        dependency.setToSkill(toSkill);
    }

    // getDependenciesByMap
    @Test
    void getDependenciesByMap_validOwner_returnsList() {
        when(skillMapRepository.findById(10L)).thenReturn(Optional.of(skillMap));
        when(skillRepository.findBySkillMap(skillMap)).thenReturn(List.of(fromSkill, toSkill));
        when(dependencyRepository.findByFromSkill_IdIn(List.of(100L, 200L))).thenReturn(List.of(dependency));

        List<Dependency> result = dependencyService.getDependenciesByMap(10L, owner);

        assertEquals(1, result.size());
        assertEquals(dependency, result.get(0));
    }

    @Test
    void getDependenciesByMap_skillMapNotFound_throws404() {
        when(skillMapRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
            () -> dependencyService.getDependenciesByMap(99L, owner));
    }

    @Test
    void getDependenciesByMap_notOwnerNotMember_throws403() {
        when(skillMapRepository.findById(10L)).thenReturn(Optional.of(skillMap));
        when(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, otherUser.getId())).thenReturn(false);

        assertThrows(ResponseStatusException.class,
            () -> dependencyService.getDependenciesByMap(10L, otherUser));
    }

    // createDependency
    @Test
    void createDependency_valid_returnsSaved() {
        when(skillMapRepository.findById(10L)).thenReturn(Optional.of(skillMap));
        when(skillRepository.findById(100L)).thenReturn(Optional.of(fromSkill));
        when(skillRepository.findById(200L)).thenReturn(Optional.of(toSkill));
        when(dependencyRepository.findByFromSkill(fromSkill)).thenReturn(List.of());
        when(dependencyRepository.save(any())).thenReturn(dependency);

        Dependency result = dependencyService.createDependency(10L, 100L, 200L, owner);

        assertNotNull(result);
        assertEquals(fromSkill, result.getFromSkill());
        assertEquals(toSkill, result.getToSkill());
    }

    @Test
    void createDependency_notOwner_throws403() {
        when(skillMapRepository.findById(10L)).thenReturn(Optional.of(skillMap));

        assertThrows(ResponseStatusException.class,
            () -> dependencyService.createDependency(10L, 100L, 200L, otherUser));
    }

    @Test
    void createDependency_fromSkillSameLevelAsToSkill_throws400() {
        toSkill.setLevel(1); // same level as fromSkill

        when(skillMapRepository.findById(10L)).thenReturn(Optional.of(skillMap));
        when(skillRepository.findById(100L)).thenReturn(Optional.of(fromSkill));
        when(skillRepository.findById(200L)).thenReturn(Optional.of(toSkill));

        assertThrows(ResponseStatusException.class,
            () -> dependencyService.createDependency(10L, 100L, 200L, owner));
    }

    @Test
    void createDependency_recursive_throws400() {
        when(skillMapRepository.findById(10L)).thenReturn(Optional.of(skillMap));
        when(skillRepository.findById(100L)).thenReturn(Optional.of(fromSkill));

        assertThrows(ResponseStatusException.class,
            () -> dependencyService.createDependency(10L, 100L, 100L, owner));
    }

    @Test
    void createDependency_alreadyExists_throws409() {
        when(skillMapRepository.findById(10L)).thenReturn(Optional.of(skillMap));
        when(skillRepository.findById(100L)).thenReturn(Optional.of(fromSkill));
        when(skillRepository.findById(200L)).thenReturn(Optional.of(toSkill));
        when(dependencyRepository.findByFromSkill(fromSkill)).thenReturn(List.of(dependency));

        assertThrows(ResponseStatusException.class,
            () -> dependencyService.createDependency(10L, 100L, 200L, owner));
    }

    @Test
    void createDependency_skillNotFound_throws404() {
        when(skillMapRepository.findById(10L)).thenReturn(Optional.of(skillMap));
        when(skillRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
            () -> dependencyService.createDependency(10L, 100L, 200L, owner));
    }

    // deleteDependency
    @Test
    void deleteDependency_validOwner_deletesSuccessfully() {
        when(dependencyRepository.findById(1000L)).thenReturn(Optional.of(dependency));

        assertDoesNotThrow(() -> dependencyService.deleteDependency(1000L, owner));
        verify(dependencyRepository, times(1)).delete(dependency);
    }

    @Test
    void deleteDependency_notFound_throws404() {
        when(dependencyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
            () -> dependencyService.deleteDependency(99L, owner));
    }

    @Test
    void deleteDependency_notOwner_throws403() {
        when(dependencyRepository.findById(1000L)).thenReturn(Optional.of(dependency));

        assertThrows(ResponseStatusException.class,
            () -> dependencyService.deleteDependency(1000L, otherUser));
    }
}
