package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.SkillMapRole;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMapMembership;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapMembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
public class SkillMapServiceIntegrationTest {

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Qualifier("skillMapRepository")
    @Autowired
    private SkillMapRepository skillMapRepository;

    @Qualifier("skillMapMembershipRepository")
    @Autowired
    private SkillMapMembershipRepository skillMapMembershipRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SkillMapService skillMapService;

    private User owner;
    private User student;
    private SkillMap skillMap;

    @BeforeEach
    public void setup() {
        // dependency order: memberships -> skillmaps -> users
        skillMapMembershipRepository.deleteAll();
        skillMapRepository.deleteAll();
        userRepository.deleteAll();

        User ownerInput = new User();
        ownerInput.setUsername("owner");
        ownerInput.setPassword("password123");
        owner = userService.createUser(ownerInput);

        User studentInput = new User();
        studentInput.setUsername("student");
        studentInput.setPassword("password456");
        student = userService.createUser(studentInput);

        SkillMap mapInput = new SkillMap();
        mapInput.setTitle("Test Map");
        mapInput.setIsPublic(false);
        mapInput.setNumberOfLevels(3);
        skillMap = skillMapService.createSkillMap(mapInput, owner.getToken());
    }

    // joinSkillMap
    // Test: valid invite code creates a membership with STUDENT role for the joining user
    @Test
    public void joinSkillMap_withValidInviteCode_createsMembershipWithStudentRole() {
        SkillMapMembership membership = skillMapService.joinSkillMap(
                skillMap.getId(), skillMap.getInviteCode(), student.getToken());

        assertNotNull(membership.getId());
        assertEquals(student.getId(), membership.getUserId());
        assertEquals(skillMap.getId(), membership.getSkillMapId());
        assertEquals(SkillMapRole.STUDENT, membership.getRole());
    }

    // Test: after joining with a valid code, the skill map appears in the student's map list
    @Test
    public void joinSkillMap_withValidInviteCode_skillMapAppearsInStudentsMapList() {
        skillMapService.joinSkillMap(skillMap.getId(), skillMap.getInviteCode(), student.getToken());

        List<SkillMap> maps = skillMapService.getSkillMaps(student.getToken());

        assertEquals(1, maps.size());
        assertEquals(skillMap.getId(), maps.get(0).getId());
    }

    // Test: a wrong invite code is rejected with 403 Forbidden
    @Test
    public void joinSkillMap_withWrongInviteCode_throwsForbidden() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                skillMapService.joinSkillMap(skillMap.getId(), "WRONGCODE1", student.getToken()));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // Test: joining a map the user is already a member of is rejected with 409 Conflict
    @Test
    public void joinSkillMap_whenStudentAlreadyMember_throwsConflict() {
        skillMapService.joinSkillMap(skillMap.getId(), skillMap.getInviteCode(), student.getToken());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                skillMapService.joinSkillMap(skillMap.getId(), skillMap.getInviteCode(), student.getToken()));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    // Test: joining a skill map that does not exist is rejected with 404 Not Found
    @Test
    public void joinSkillMap_withNonExistentSkillMapId_throwsNotFound() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                skillMapService.joinSkillMap(999999L, "anycode", student.getToken()));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // Test: the owner cannot join their own map since they are already a member as OWNER
    @Test
    public void joinSkillMap_whenOwnerTriesToJoinOwnMap_throwsConflict() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                skillMapService.joinSkillMap(skillMap.getId(), skillMap.getInviteCode(), owner.getToken()));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    // getSkillMaps
    // Test: a student who has not joined any map receives an empty list
    @Test
    public void getSkillMaps_whenStudentHasNotJoinedAnyMap_returnsEmptyList() {
        List<SkillMap> maps = skillMapService.getSkillMaps(student.getToken());

        assertTrue(maps.isEmpty());
    }

    // Test: the owner sees their own map in the list because createSkillMap auto-adds them as a member
    @Test
    public void getSkillMaps_whenCalledByOwner_returnsOwnedMap() {
        List<SkillMap> maps = skillMapService.getSkillMaps(owner.getToken());

        assertEquals(1, maps.size());
        assertEquals(skillMap.getId(), maps.get(0).getId());
    }
}
