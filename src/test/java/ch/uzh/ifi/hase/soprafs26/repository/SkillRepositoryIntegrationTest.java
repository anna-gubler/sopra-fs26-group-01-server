package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.*;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class SkillRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SkillRepository skillRepository;

    private SkillMap skillMap;

    @BeforeEach
    void setup() {
        skillMap = new SkillMap();
        skillMap.setTitle("Test Map");
        skillMap.setIsPublic(true);
        skillMap.setNumberOfLevels(3);
        skillMap.setOwnerId(1L);
        skillMap.setInviteCode("TESTCODE01");
        entityManager.persist(skillMap);
        entityManager.flush();
    }

    private Skill createTestSkill() {
        Skill skill = new Skill();
        skill.setName("Test Skill");
        skill.setDescription("Test Description");
        skill.setLevel(1);
        skill.setSkillMap(skillMap);
        skill.setIsLocked(false);
        return skill;
    }

    @Test
    void findBySkillMap_success() {
        // given
        Skill skill = createTestSkill();
        entityManager.persist(skill);
        entityManager.flush();

        // when
        List<Skill> found = skillRepository.findBySkillMap(skillMap);

        // then
        assertFalse(found.isEmpty());
        assertEquals(skill.getName(), found.get(0).getName());
        assertEquals(skillMap.getId(), found.get(0).getSkillMap().getId());
    }

    @Test
    void findBySkillMapAndLevel_success() {
        // given
        Skill skill = createTestSkill();
        entityManager.persist(skill);
        entityManager.flush();

        // when
        List<Skill> found = skillRepository.findBySkillMapAndLevel(skillMap, 1);

        // then
        assertFalse(found.isEmpty());
        assertEquals(skill.getLevel(), found.get(0).getLevel());
    }

    @Test
    void createdAt_isSetAutomatically() {
        // given
        Skill skill = createTestSkill();
        entityManager.persist(skill);
        entityManager.flush();

        // then
        assertNotNull(skill.getCreatedAt());
    }
}