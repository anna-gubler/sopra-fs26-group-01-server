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
    void joinSkillMap_withWrongInviteCode_notFound() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                skillMapService.joinSkillMap("WRONGCODE1", student));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void joinSkillMap_whenStudentAlreadyMember_throwsConflict() {
        skillMapService.joinSkillMap(skillMap.getInviteCode(), student);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                skillMapService.joinSkillMap(skillMap.getInviteCode(), student));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void joinSkillMap_withNonExistentSkillMapId_throwsNotFound() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                skillMapService.joinSkillMap("anycode", student));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
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
}
