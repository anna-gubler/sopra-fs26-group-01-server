package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;

import java.time.LocalDateTime;
import java.util.List;

@AutoConfigureMockMvc(addFilters = false)
@WebAppConfiguration
@SpringBootTest
class DependencyServiceIntegrationTest {

    @Autowired private DependencyService dependencyService;
    @Autowired private SkillMapRepository skillMapRepository;
    @Autowired private SkillRepository skillRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DependencyRepository dependencyRepository;
    @Autowired private SkillMapMembershipRepository skillMapMembershipRepository;

    private User owner;
    private SkillMap skillMap;
    private Skill fromSkill;
    private Skill toSkill;

    @BeforeEach
    void setup() {
        dependencyRepository.deleteAll();
        skillRepository.deleteAll();
        skillMapMembershipRepository.deleteAll();
        skillMapRepository.deleteAll();
        userRepository.deleteAll();

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
            skillMap.getId(), fromSkill.getId(), toSkill.getId(), "owner-token");

        assertNotNull(created.getId());
        assertEquals(fromSkill.getId(), created.getFromSkill().getId());
        assertEquals(toSkill.getId(), created.getToSkill().getId());
    }

    @Test
    void getDependenciesByMap_afterCreate_returnsCorrectList() {
        dependencyService.createDependency(
            skillMap.getId(), fromSkill.getId(), toSkill.getId(), "owner-token");

        List<Dependency> result = dependencyService.getDependenciesByMap(
            skillMap.getId(), "owner-token");

        assertEquals(1, result.size());
        assertEquals(fromSkill.getId(), result.get(0).getFromSkill().getId());
    }

    @Test
    void createDependency_duplicate_throws409() {
        dependencyService.createDependency(
            skillMap.getId(), fromSkill.getId(), toSkill.getId(), "owner-token");

        assertThrows(ResponseStatusException.class, () ->
            dependencyService.createDependency(
                skillMap.getId(), fromSkill.getId(), toSkill.getId(), "owner-token"));
    }

    @Test
    void createDependency_sameLevelSkills_throws400() {
        toSkill.setLevel(1);
        skillRepository.saveAndFlush(toSkill);

        assertThrows(ResponseStatusException.class, () ->
            dependencyService.createDependency(
                skillMap.getId(), fromSkill.getId(), toSkill.getId(), "owner-token"));
    }

    @Test
    void deleteDependency_valid_removesFromDatabase() {
        Dependency created = dependencyService.createDependency(
            skillMap.getId(), fromSkill.getId(), toSkill.getId(), "owner-token");

        dependencyService.deleteSkill(created.getId(), "owner-token");

        assertTrue(dependencyRepository.findById(created.getId()).isEmpty());
    }

    @Test
    void createDependency_notOwner_throws403() {
        assertThrows(ResponseStatusException.class, () ->
            dependencyService.createDependency(
                skillMap.getId(), fromSkill.getId(), toSkill.getId(), "wrong-token"));
    }

    @Test
    void getDependenciesByMap_notMember_throws403() {
        assertThrows(ResponseStatusException.class, () ->
            dependencyService.getDependenciesByMap(skillMap.getId(), "wrong-token"));
    }
}