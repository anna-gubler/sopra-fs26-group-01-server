package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest
@Transactional
class SkillServiceIntegrationTest {

    @Autowired
    private DependencyRepository dependencyRepository;

    @Autowired
    private SkillService skillService;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private SkillMapRepository skillMapRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private SkillMap skillMap;

    @BeforeEach
    void setup() {
        dependencyRepository.deleteAll();
        skillRepository.deleteAll();
        skillMapRepository.deleteAll(); 
        userRepository.deleteAll(); 

        owner = new User();
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        owner.setUsername("testowner-" + uniqueSuffix);
        owner.setPassword("password");
        owner.setToken(UUID.randomUUID().toString());
        owner.setStatus(UserStatus.ONLINE);  
        owner.setSeed("seed123");             
        owner.setStyle("avataaars");          
        owner.setCreationDate(LocalDateTime.now());
        userRepository.save(owner);
        userRepository.flush();

        skillMap = new SkillMap();
        skillMap.setTitle("Test Map");
        skillMap.setIsPublic(true);
        skillMap.setNumberOfLevels(3);
        skillMap.setOwnerId(owner.getId());
        skillMap.setInviteCode("TESTCODE01");
        skillMapRepository.save(skillMap);
        skillMapRepository.flush();
    }

    @Test
    void createSkill_persistedCorrectly() {
        Skill input = new Skill();
        input.setName("Loops");
        input.setLevel(1);

        Skill result = skillService.createSkill(skillMap.getId(), input, owner);

        assertNotNull(result.getId());
        assertEquals("Loops", result.getName());
        assertEquals(skillMap.getId(), result.getSkillMap().getId());
        assertTrue(skillRepository.findById(result.getId()).isPresent());
    }

    @Test
    void deleteSkill_removedFromDatabase() {
        Skill input = new Skill();
        input.setName("ToDelete");
        input.setLevel(1);
        Skill saved = skillService.createSkill(skillMap.getId(), input, owner);

        skillService.deleteSkill(saved.getId(), owner);

        assertFalse(skillRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void createSkill_nonOwner_notPersisted() {
        User nonOwner = new User();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        nonOwner.setUsername("nonowner-" + suffix);
        nonOwner.setPassword("password");
        nonOwner.setToken(UUID.randomUUID().toString());
        nonOwner.setStatus(UserStatus.ONLINE);
        nonOwner.setSeed("seed123");
        nonOwner.setStyle("avataaars");
        nonOwner.setCreationDate(LocalDateTime.now());
        userRepository.save(nonOwner);
        userRepository.flush();

        Skill input = new Skill();
        input.setName("ShouldNotExist");
        input.setLevel(1);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillService.createSkill(skillMap.getId(), input, nonOwner));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals(0, skillRepository.findBySkillMap(skillMap).size());
    }
}