package ch.uzh.ifi.hase.soprafs26.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ch.uzh.ifi.hase.soprafs26.entity.Skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

@DataJpaTest
class SkillRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SkillRepository skillRepository;

    private Skill createTestSkill() {
        Skill skill = new Skill();
        skill.setName("Test Skill");
        skill.setDescription("Test Description");
        skill.setLevel(1);
        skill.setMapId(1L);
        skill.setIsLocked(false);
        return skill;
    }

    @Test
    void findByMapId_success() {
        // given
        Skill skill = createTestSkill();
        entityManager.persist(skill);
        entityManager.flush();

        // when
        List<Skill> found = skillRepository.findByMapId(1L);

        // then
        assertFalse(found.isEmpty());
        assertEquals(found.get(0).getName(), skill.getName());
        assertEquals(found.get(0).getMapId(), skill.getMapId());
    }

    @Test
    void findByMapIdAndLevel_success() {
        // given
        Skill skill = createTestSkill();
        entityManager.persist(skill);
        entityManager.flush();

        // when
        List<Skill> found = skillRepository.findByMapIdAndLevel(1L, 1);

        // then
        assertFalse(found.isEmpty());
        assertEquals(found.get(0).getLevel(), skill.getLevel());
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