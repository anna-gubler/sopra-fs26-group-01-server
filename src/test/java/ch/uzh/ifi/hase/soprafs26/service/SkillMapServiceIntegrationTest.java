package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.SkillMapRole;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMapMembership;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SkillMapServiceIntegrationTest {

    @Autowired
    private SkillMapService skillMapService;

    @Autowired
    private SkillMapRepository skillMapRepository;

    @Autowired
    private SkillMapMembershipRepository skillMapMembershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private DependencyRepository dependencyRepository;

    @Autowired
    private SkillRepository skillRepository;

    private User owner;
    private User student;
    private SkillMap skillMap;

    @BeforeEach
    void setup() {
        skillMapMembershipRepository.deleteAll();
        dependencyRepository.deleteAll();
        skillRepository.deleteAll();
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
        skillMap = skillMapService.createSkillMap(mapInput, owner);
    }

    // createSkillMap
    @Test
    void createSkillMap_persistedCorrectlyWithOwnerMembership() {
        SkillMap input = new SkillMap();
        input.setTitle("Integration Map");
        input.setIsPublic(true);
        input.setNumberOfLevels(3);

        SkillMap result = skillMapService.createSkillMap(input, owner);

        assertNotNull(result.getId());
        assertEquals("Integration Map", result.getTitle());
        assertTrue(skillMapRepository.findById(result.getId()).isPresent());
        assertTrue(skillMapMembershipRepository
                .existsBySkillMapIdAndUserId(result.getId(), owner.getId()));
    }

    // deleteSkillMap
    @Test
    void deleteSkillMap_removedFromDatabaseWithMemberships() {
        SkillMap input = new SkillMap();
        input.setTitle("To Delete");
        input.setIsPublic(true);
        input.setNumberOfLevels(2);
        SkillMap saved = skillMapService.createSkillMap(input, owner);
        Long mapId = saved.getId();

        skillMapService.deleteSkillMap(mapId, owner);

        assertFalse(skillMapRepository.findById(mapId).isPresent());
        assertTrue(skillMapMembershipRepository.findBySkillMapId(mapId).isEmpty());
    }

    // joinSkillMap
    @Test
    void joinSkillMap_withValidInviteCode_createsMembershipWithStudentRole() {
        SkillMapMembership membership = skillMapService.joinSkillMap(
                skillMap.getInviteCode(), student);

        assertEquals(student.getId(), membership.getUserId());
        assertEquals(skillMap.getId(), membership.getSkillMapId());
        assertEquals(SkillMapRole.STUDENT, membership.getRole());
    }

    @Test
    void joinSkillMap_withValidInviteCode_skillMapAppearsInStudentsMapList() {
        skillMapService.joinSkillMap(skillMap.getInviteCode(), student);

        List<SkillMap> maps = skillMapService.getSkillMaps(student);

        assertEquals(1, maps.size());
    }

    @Test
    void joinSkillMap_withWrongInviteCode_Forbidden() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                skillMapService.joinSkillMap("WRONGCODE1", student));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void joinSkillMap_whenStudentAlreadyMember_throwsConflict() {
        skillMapService.joinSkillMap(skillMap.getInviteCode(), student);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                skillMapService.joinSkillMap(skillMap.getInviteCode(), student));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void joinSkillMap_whenOwnerTriesToJoinOwnMap_throwsConflict() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                skillMapService.joinSkillMap(skillMap.getInviteCode(), owner));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    // getSkillMaps
    @Test
    void getSkillMaps_whenStudentHasNotJoinedAnyMap_returnsEmptyList() {
        List<SkillMap> maps = skillMapService.getSkillMaps(student);

        assertTrue(maps.isEmpty());
    }

    @Test
    void getSkillMaps_whenCalledByOwner_returnsOwnedMap() {
        List<SkillMap> maps = skillMapService.getSkillMaps(owner);

        assertEquals(1, maps.size());
        assertEquals(skillMap.getId(), maps.get(0).getId());
    }

    // inviteCode generation
    @Test
    void createSkillMap_inviteCode_isAutoGenerated() {
        assertNotNull(skillMap.getInviteCode());
    }

    @Test
    void createSkillMap_inviteCode_isFourCharsUppercase() {
        String code = skillMap.getInviteCode();
        assertEquals(4, code.length());
        assertEquals(code.toUpperCase(), code);
    }

    @Test
    void createSkillMap_twoMaps_haveDistinctInviteCodes() {
        SkillMap mapInput = new SkillMap();
        mapInput.setTitle("Second Map");
        mapInput.setIsPublic(false);
        mapInput.setNumberOfLevels(2);
        SkillMap second = skillMapService.createSkillMap(mapInput, owner);

        assertNotEquals(skillMap.getInviteCode(), second.getInviteCode());
    }

    // private map access control
    @Test
    void getSkillMapById_privateMap_nonMember_throwsForbidden() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                skillMapService.getSkillMapById(skillMap.getId(), student));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void getSkillMapById_privateMap_member_succeeds() {
        skillMapService.joinSkillMap(skillMap.getInviteCode(), student);

        SkillMap result = skillMapService.getSkillMapById(skillMap.getId(), student);

        assertEquals(skillMap.getId(), result.getId());
    }

    @Test
    void getSkillMapById_publicMap_nonMember_succeeds() {
        SkillMap mapInput = new SkillMap();
        mapInput.setTitle("Public Map");
        mapInput.setIsPublic(true);
        mapInput.setNumberOfLevels(2);
        SkillMap publicMap = skillMapService.createSkillMap(mapInput, owner);

        SkillMap result = skillMapService.getSkillMapById(publicMap.getId(), student);

        assertEquals(publicMap.getId(), result.getId());
    }

    // privacy toggle
    @Test
    void updateSkillMap_privateToPublic_nonMemberCanAccess() {
        SkillMap updates = new SkillMap();
        updates.setIsPublic(true);
        skillMapService.updateSkillMap(skillMap.getId(), updates, owner);

        SkillMap result = skillMapService.getSkillMapById(skillMap.getId(), student);

        assertEquals(skillMap.getId(), result.getId());
    }

    @Test
    void updateSkillMap_publicToPrivate_nonMemberForbidden() {
        SkillMap mapInput = new SkillMap();
        mapInput.setTitle("Public Map");
        mapInput.setIsPublic(true);
        mapInput.setNumberOfLevels(2);
        SkillMap publicMap = skillMapService.createSkillMap(mapInput, owner);

        SkillMap updates = new SkillMap();
        updates.setIsPublic(false);
        skillMapService.updateSkillMap(publicMap.getId(), updates, owner);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                skillMapService.getSkillMapById(publicMap.getId(), student));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }
}
