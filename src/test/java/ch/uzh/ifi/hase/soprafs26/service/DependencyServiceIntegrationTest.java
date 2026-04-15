package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@Transactional
class DependencyServiceIntegrationTest {

    @Autowired private DependencyService dependencyService;
    @Autowired private SkillMapRepository skillMapRepository;
    @Autowired private SkillRepository skillRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DependencyRepository dependencyRepository;

    private User owner;
    private User nonOwner;
    private SkillMap skillMap;
    private Skill fromSkill;
    private Skill toSkill;

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setUsername("testowner");
        owner.setPassword("password");
        owner.setToken("owner-token");
        owner.setStatus(UserStatus.ONLINE);
        owner.setCreationDate(LocalDateTime.now());
        owner.setSeed("testseed");
        owner.setStyle("pixel");
        owner.setBio("");
        owner = userRepository.saveAndFlush(owner);

        nonOwner = new User();
        nonOwner.setUsername("nonowner");
        nonOwner.setPassword("password");
        nonOwner.setToken("nonowner-token");
        nonOwner.setStatus(UserStatus.ONLINE);
        nonOwner.setCreationDate(LocalDateTime.now());
        nonOwner.setSeed("testseed");
        nonOwner.setStyle("pixel");
        nonOwner.setBio("");
        nonOwner = userRepository.saveAndFlush(nonOwner);

        skillMap = new SkillMap();
        skillMap.setTitle("Test Map");
        skillMap.setOwnerId(owner.getId());
        skillMap.setIsPublic(false);
        skillMap.setNumberOfLevels(3);
        skillMap = skillMapRepository.saveAndFlush(skillMap);

        fromSkill = new Skill();
        fromSkill.setName("From Skill");
        fromSkill.setLevel(1);
        fromSkill.setSkillMap(skillMap);
        fromSkill = skillRepository.saveAndFlush(fromSkill);

        toSkill = new Skill();
        toSkill.setName("To Skill");
        toSkill.setLevel(2);
        toSkill.setSkillMap(skillMap);
        toSkill = skillRepository.saveAndFlush(toSkill);
    }

    @Test
    void createDependency_valid_persistsToDatabase() {
        Dependency created = dependencyService.createDependency(
            skillMap.getId(), fromSkill.getId(), toSkill.getId(), owner);

        assertNotNull(created.getId());
        assertEquals(fromSkill.getId(), created.getFromSkill().getId());
        assertEquals(toSkill.getId(), created.getToSkill().getId());
    }

    @Test
    void getDependenciesByMap_afterCreate_returnsCorrectList() {
        dependencyService.createDependency(
            skillMap.getId(), fromSkill.getId(), toSkill.getId(), owner);

        List<Dependency> result = dependencyService.getDependenciesByMap(
            skillMap.getId(), owner);

        assertEquals(1, result.size());
        assertEquals(fromSkill.getId(), result.get(0).getFromSkill().getId());
    }

    @Test
    void createDependency_duplicate_throws409() {
        dependencyService.createDependency(
            skillMap.getId(), fromSkill.getId(), toSkill.getId(), owner);

        assertThrows(ResponseStatusException.class, () ->
            dependencyService.createDependency(
                skillMap.getId(), fromSkill.getId(), toSkill.getId(), owner));
    }

    @Test
    void createDependency_sameLevelSkills_throws400() {
        toSkill.setLevel(1);
        skillRepository.saveAndFlush(toSkill);

        assertThrows(ResponseStatusException.class, () ->
            dependencyService.createDependency(
                skillMap.getId(), fromSkill.getId(), toSkill.getId(), owner));
    }

    @Test
    void deleteDependency_valid_removesFromDatabase() {
        Dependency created = dependencyService.createDependency(
            skillMap.getId(), fromSkill.getId(), toSkill.getId(), owner);

        dependencyService.deleteDependency(created.getId(), owner);

        assertTrue(dependencyRepository.findById(created.getId()).isEmpty());
    }

    @Test
    void createDependency_notOwner_throws403() {
        assertThrows(ResponseStatusException.class, () ->
            dependencyService.createDependency(
                skillMap.getId(), fromSkill.getId(), toSkill.getId(), nonOwner));
    }

    @Test
    void getDependenciesByMap_notMember_throws403() {
        assertThrows(ResponseStatusException.class, () ->
            dependencyService.getDependenciesByMap(skillMap.getId(), nonOwner));
    }
}