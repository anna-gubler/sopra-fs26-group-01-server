package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.SkillMapRole;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMapMembership;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapMembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapRepository;
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
class SkillMapServiceTest {

    @Mock
    private SkillMapRepository skillMapRepository;

    @Mock
    private SkillMapMembershipRepository skillMapMembershipRepository;

    // Still needed: getMembers() calls userService.getUserById() internally.
    @Mock
    private UserService userService;

    @InjectMocks
    private SkillMapService skillMapService;

    // ─── shared fixtures ──────────────────────────────────────────────────────

    private User owner;
    private User otherUser;
    private SkillMap skillMap;
    private SkillMapMembership ownerMembership;

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
        skillMap.setIsPublic(true);
        skillMap.setNumberOfLevels(3);
        skillMap.setInviteCode("INVITE1234");

        ownerMembership = new SkillMapMembership();
        ownerMembership.setId(100L);
        ownerMembership.setUserId(owner.getId());
        ownerMembership.setSkillMapId(skillMap.getId());
        ownerMembership.setRole(SkillMapRole.OWNER);
    }

    // ─── 201  getSkillMaps ────────────────────────────────────────────────────

    @Test
    void getSkillMaps_validUser_returnsMemberMaps() {
        given(skillMapMembershipRepository.findByUserId(owner.getId()))
                .willReturn(List.of(ownerMembership));
        given(skillMapRepository.findAllById(List.of(skillMap.getId())))
                .willReturn(List.of(skillMap));

        List<SkillMap> result = skillMapService.getSkillMaps(owner);

        assertEquals(1, result.size());
        assertEquals(skillMap.getId(), result.get(0).getId());
    }

    @Test
    void getSkillMaps_noMemberships_returnsEmptyList() {
        given(skillMapMembershipRepository.findByUserId(owner.getId()))
                .willReturn(List.of());
        given(skillMapRepository.findAllById(List.of()))
                .willReturn(List.of());

        List<SkillMap> result = skillMapService.getSkillMaps(owner);

        assertTrue(result.isEmpty());
    }

    // ─── 202  createSkillMap ──────────────────────────────────────────────────

    @Test
    void createSkillMap_validInput_returnsCreatedMap() {
        given(skillMapRepository.save(any(SkillMap.class))).willReturn(skillMap);
        given(skillMapRepository.existsByInviteCode(any())).willReturn(false);

        SkillMap input = new SkillMap();
        input.setTitle("Test Map");
        input.setIsPublic(true);
        input.setNumberOfLevels(3);

        SkillMap result = skillMapService.createSkillMap(input, owner);

        assertEquals(skillMap.getId(), result.getId());
        assertEquals(owner.getId(), result.getOwnerId());
        verify(skillMapMembershipRepository).save(any(SkillMapMembership.class));
    }

    @Test
    void createSkillMap_missingTitle_throws400() {
        SkillMap input = new SkillMap();
        input.setIsPublic(true);
        input.setNumberOfLevels(3);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.createSkillMap(input, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createSkillMap_missingIsPublic_throws400() {
        SkillMap input = new SkillMap();
        input.setTitle("Test Map");
        input.setNumberOfLevels(3);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.createSkillMap(input, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createSkillMap_invalidNumberOfLevels_throws400() {
        SkillMap input = new SkillMap();
        input.setTitle("Test Map");
        input.setIsPublic(true);
        input.setNumberOfLevels(0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.createSkillMap(input, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // ─── 203  getSkillMapById ─────────────────────────────────────────────────

    @Test
    void getSkillMapById_publicMap_anyUser_returnsMap() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(false);

        SkillMap result = skillMapService.getSkillMapById(10L, otherUser);

        assertEquals(skillMap.getId(), result.getId());
    }

    @Test
    void getSkillMapById_privateMap_member_returnsMap() {
        skillMap.setIsPublic(false);
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(true);

        SkillMap result = skillMapService.getSkillMapById(10L, otherUser);

        assertEquals(skillMap.getId(), result.getId());
    }

    @Test
    void getSkillMapById_privateMap_nonMember_throws403() {
        skillMap.setIsPublic(false);
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.getSkillMapById(10L, otherUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void getSkillMapById_notFound_throws404() {
        given(skillMapRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.getSkillMapById(99L, owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ─── 204  updateSkillMap ──────────────────────────────────────────────────

    @Test
    void updateSkillMap_validInput_returnsUpdatedMap() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId()))
                .willReturn(true);
        given(skillMapRepository.save(any(SkillMap.class))).willReturn(skillMap);

        SkillMap updates = new SkillMap();
        updates.setTitle("Updated Title");

        SkillMap result = skillMapService.updateSkillMap(10L, updates, owner);

        assertEquals("Updated Title", result.getTitle());
    }

    @Test
    void updateSkillMap_notOwner_throws403() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(true);

        SkillMap updates = new SkillMap();
        updates.setTitle("Hacked Title");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.updateSkillMap(10L, updates, otherUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void updateSkillMap_blankTitle_throws400() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId()))
                .willReturn(true);

        SkillMap updates = new SkillMap();
        updates.setTitle("   ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.updateSkillMap(10L, updates, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // ─── 205  deleteSkillMap ──────────────────────────────────────────────────

    @Test
    void deleteSkillMap_owner_deletesSuccessfully() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.findBySkillMapId(10L))
                .willReturn(List.of(ownerMembership));

        assertDoesNotThrow(() -> skillMapService.deleteSkillMap(10L, owner));

        verify(skillMapMembershipRepository).deleteAll(List.of(ownerMembership));
        verify(skillMapRepository).deleteById(10L);
    }

    @Test
    void deleteSkillMap_notOwner_throws403() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.deleteSkillMap(10L, otherUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(skillMapRepository, never()).deleteById(any());
    }

    @Test
    void deleteSkillMap_notFound_throws404() {
        given(skillMapRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.deleteSkillMap(99L, owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ─── 206  joinSkillMap ────────────────────────────────────────────────────

    @Test
    void joinSkillMap_validInviteCode_createsMembership() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(false);

        SkillMapMembership newMembership = new SkillMapMembership();
        newMembership.setUserId(otherUser.getId());
        newMembership.setSkillMapId(10L);
        newMembership.setRole(SkillMapRole.STUDENT);
        given(skillMapMembershipRepository.save(any(SkillMapMembership.class)))
                .willReturn(newMembership);

        SkillMapMembership result = skillMapService.joinSkillMap(10L, "INVITE1234", otherUser);

        assertEquals(SkillMapRole.STUDENT, result.getRole());
        assertEquals(otherUser.getId(), result.getUserId());
    }

    @Test
    void joinSkillMap_wrongInviteCode_throws403() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.joinSkillMap(10L, "WRONGCODE", otherUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void joinSkillMap_alreadyMember_throws409() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.joinSkillMap(10L, "INVITE1234", otherUser));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void joinSkillMap_mapNotFound_throws404() {
        given(skillMapRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.joinSkillMap(99L, "INVITE1234", otherUser));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ─── 207  getMembers ─────────────────────────────────────────────────────

    @Test
    void getMembers_validRequest_returnsMemberList() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId()))
                .willReturn(true);
        given(skillMapMembershipRepository.findBySkillMapId(10L))
                .willReturn(List.of(ownerMembership));
        given(userService.getUserById(owner.getId())).willReturn(owner);

        List<User> result = skillMapService.getMembers(10L, owner);

        assertEquals(1, result.size());
        assertEquals(owner.getId(), result.get(0).getId());
    }

    // ─── 208  removeMember ───────────────────────────────────────────────────

    @Test
    void removeMember_ownerRemovesOther_succeeds() {
        SkillMapMembership otherMembership = new SkillMapMembership();
        otherMembership.setId(101L);
        otherMembership.setUserId(otherUser.getId());
        otherMembership.setSkillMapId(10L);
        otherMembership.setRole(SkillMapRole.STUDENT);

        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.findBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(Optional.of(otherMembership));

        assertDoesNotThrow(() -> skillMapService.removeMember(10L, otherUser.getId(), owner));
        verify(skillMapMembershipRepository).deleteById(101L);
    }

    @Test
    void removeMember_memberRemovesSelf_succeeds() {
        SkillMapMembership otherMembership = new SkillMapMembership();
        otherMembership.setId(101L);
        otherMembership.setUserId(otherUser.getId());
        otherMembership.setSkillMapId(10L);
        otherMembership.setRole(SkillMapRole.STUDENT);

        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.findBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(Optional.of(otherMembership));

        assertDoesNotThrow(() -> skillMapService.removeMember(10L, otherUser.getId(), otherUser));
        verify(skillMapMembershipRepository).deleteById(101L);
    }

    @Test
    void removeMember_unauthorizedUser_throws403() {
        User thirdUser = new User();
        thirdUser.setId(3L);

        SkillMapMembership otherMembership = new SkillMapMembership();
        otherMembership.setId(101L);
        otherMembership.setUserId(otherUser.getId());
        otherMembership.setSkillMapId(10L);

        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.findBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(Optional.of(otherMembership));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.removeMember(10L, otherUser.getId(), thirdUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(skillMapMembershipRepository, never()).deleteById(any());
    }

    @Test
    void removeMember_membershipNotFound_throws404() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.findBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.removeMember(10L, otherUser.getId(), owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
