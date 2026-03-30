package ch.uzh.ifi.hase.soprafs26.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

@DataJpaTest
class SkillMapRepositoryIntegrationTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
    private SkillMapRepository skillMapRepository;

    private SkillMap createTestSkillMap() {
        SkillMap skillMap = new SkillMap();
        skillMap.setTitle("Test Map");
        skillMap.setDescription("Test Description");
        skillMap.setIsPublic(true);
        skillMap.setNumberOfLevels(3);
        skillMap.setOwnerId(1L);
        skillMap.setInviteCode("ABC123");
        return skillMap;
    }

	@Test
    void findByOwnerId_success() {
        // given
        SkillMap skillMap = createTestSkillMap();
        entityManager.persist(skillMap);
        entityManager.flush();

        // when
        List<SkillMap> found = skillMapRepository.findByOwnerId(1L);

        // then
        assertFalse(found.isEmpty());
        assertEquals(found.get(0).getTitle(), skillMap.getTitle());
        assertEquals(found.get(0).getOwnerId(), skillMap.getOwnerId());
    }

    @Test
    void createdAt_isSetAutomatically() {
        // given
        SkillMap skillMap = createTestSkillMap();
        entityManager.persist(skillMap);
        entityManager.flush();

        // then
        assertNotNull(skillMap.getCreatedAt());
    }
}
