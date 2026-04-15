package ch.uzh.ifi.hase.soprafs26.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.List;


@DataJpaTest
class DependencyRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DependencyRepository dependencyRepository;

    // helper: persistierte Skill-Instanz erzeugen
    private Skill createAndPersistSkill(SkillMap skillMap, String name, int level) {
        Skill skill = new Skill();
        skill.setName(name);
        skill.setLevel(level);
        skill.setSkillMap(skillMap);
        return entityManager.persist(skill);
    }

    private SkillMap createAndPersistSkillMap() {
        // erst User persistieren um eine gültige ownerId zu bekommen
        User owner = new User();
        owner.setUsername("testowner");
        owner.setPassword("password");
        owner.setToken("test-token");
        owner.setStatus(UserStatus.ONLINE);
        owner.setCreationDate(LocalDateTime.now());
        owner.setSeed("testseed");
        owner.setStyle("pixel");
        owner.setBio("");
        owner = entityManager.persistFlushFind(owner);

        SkillMap skillMap = new SkillMap();
        skillMap.setTitle("Test Map");
        skillMap.setIsPublic(false);
        skillMap.setNumberOfLevels(3);
        skillMap.setOwnerId(owner.getId());
        return entityManager.persist(skillMap);
    }

    @Test
    void findByFromSkill_success() {
        // given
        SkillMap skillMap = createAndPersistSkillMap();
        Skill from = createAndPersistSkill(skillMap, "Skill A", 1);
        Skill to = createAndPersistSkill(skillMap, "Skill B", 2);

        Dependency dependency = new Dependency();
        dependency.setFromSkill(from);
        dependency.setToSkill(to);
        entityManager.persist(dependency);
        entityManager.flush();

        // when
        List<Dependency> found = dependencyRepository.findByFromSkill(from);

        // then
        assertFalse(found.isEmpty());
        assertEquals(from.getId(), found.get(0).getFromSkill().getId());
        assertEquals(to.getId(), found.get(0).getToSkill().getId());
    }

    @Test
    void findByFromSkillOrToSkill_returnsAllRelated() {
        // given
        SkillMap skillMap = createAndPersistSkillMap();
        Skill skillA = createAndPersistSkill(skillMap, "Skill A", 1);
        Skill skillB = createAndPersistSkill(skillMap, "Skill B", 2);
        Skill skillC = createAndPersistSkill(skillMap, "Skill C", 3);

        Dependency dep1 = new Dependency();
        dep1.setFromSkill(skillA);
        dep1.setToSkill(skillB);
        entityManager.persist(dep1);

        Dependency dep2 = new Dependency();
        dep2.setFromSkill(skillB);
        dep2.setToSkill(skillC);
        entityManager.persist(dep2);

        entityManager.flush();

        // when: all dependencies around skillB
        List<Dependency> found = dependencyRepository.findByFromSkillOrToSkill(skillB, skillB);

        // then: dep1 (toSkill=B) and dep2 (fromSkill=B) are found and can be deleted
        assertEquals(2, found.size());
    }

    @Test
    void createdAt_isSetAutomatically() {
        // given
        SkillMap skillMap = createAndPersistSkillMap();
        Skill from = createAndPersistSkill(skillMap, "Skill A", 1);
        Skill to = createAndPersistSkill(skillMap, "Skill B", 2);

        Dependency dependency = new Dependency();
        dependency.setFromSkill(from);
        dependency.setToSkill(to);
        entityManager.persist(dependency);
        entityManager.flush();

        // then
        assertNotNull(dependency.getCreatedAt());
    }
}
