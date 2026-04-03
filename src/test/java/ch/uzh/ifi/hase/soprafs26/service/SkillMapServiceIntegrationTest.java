package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.SkillMapRole;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMapMembership;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapMembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

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
    
    private User owner;

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setUsername("testowner");
        owner.setPassword("password");
        owner.setToken("owner-token");
        owner.setStatus(UserStatus.ONLINE);
        owner.setSeed("seed123");           
        owner.setStyle("avataaars");        
        owner.setCreationDate(LocalDateTime.now());
        userRepository.save(owner);
        userRepository.flush();
    }

    @Test
    void createSkillMap_persistedCorrectlyWithOwnerMembership() {
        SkillMap input = new SkillMap();
        input.setTitle("Integration Map");
        input.setIsPublic(true);
        input.setNumberOfLevels(3);

        SkillMap result = skillMapService.createSkillMap(input, "owner-token");

        // map is persisted
        assertNotNull(result.getId());
        assertEquals("Integration Map", result.getTitle());
        assertTrue(skillMapRepository.findById(result.getId()).isPresent());

        // owner membership is auto-created
        assertTrue(skillMapMembershipRepository
                .existsBySkillMapIdAndUserId(result.getId(), owner.getId()));
    }

    @Test
    void deleteSkillMap_removedFromDatabaseWithMemberships() {
        SkillMap input = new SkillMap();
        input.setTitle("To Delete");
        input.setIsPublic(true);
        input.setNumberOfLevels(2);
        SkillMap saved = skillMapService.createSkillMap(input, "owner-token");
        Long mapId = saved.getId();

        skillMapService.deleteSkillMap(mapId, "owner-token");

        assertFalse(skillMapRepository.findById(mapId).isPresent());
        assertTrue(skillMapMembershipRepository.findBySkillMapId(mapId).isEmpty());
    }

    @Test
    void joinSkillMap_membershipPersistedWithStudentRole() {
        // create map as owner
        SkillMap input = new SkillMap();
        input.setTitle("Joinable Map");
        input.setIsPublic(true);
        input.setNumberOfLevels(2);
        SkillMap saved = skillMapService.createSkillMap(input, owner.getToken());

        // create a second user
        User newStudent = new User();
        newStudent.setUsername("student");
        newStudent.setPassword("password");
        User student = userService.createUser(newStudent);

        SkillMapMembership membership = skillMapService.joinSkillMap(
                saved.getId(), saved.getInviteCode(), student.getToken());

        assertNotNull(membership.getId());
        assertEquals(SkillMapRole.STUDENT, membership.getRole());
        assertTrue(skillMapMembershipRepository
                .existsBySkillMapIdAndUserId(saved.getId(), student.getId()));
    }
}