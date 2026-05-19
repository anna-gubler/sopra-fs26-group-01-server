package ch.uzh.ifi.hase.soprafs26.entity;

import ch.uzh.ifi.hase.soprafs26.constant.SkillMapRole;
import ch.uzh.ifi.hase.soprafs26.rest.dto.DependencyExportDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillExportDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapGraphDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class EntityGetterSetterTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

    // ── UpvoteRecord ─────────────────────────────────────────────────────────

    @Test
    void upvoteRecord_gettersAndSetters() {
        UpvoteRecord record = new UpvoteRecord();
        record.setId(1L);
        record.setUserId(2L);
        record.setQuestionId(3L);
        record.setVotedAt(NOW);
        record.setUpdatedAt(NOW);

        assertEquals(1L, record.getId());
        assertEquals(2L, record.getUserId());
        assertEquals(3L, record.getQuestionId());
        assertEquals(NOW, record.getVotedAt());
        assertEquals(NOW, record.getUpdatedAt());
    }

    // ── StudentProgress ──────────────────────────────────────────────────────

    @Test
    void studentProgress_gettersAndSetters() {
        StudentProgress sp = new StudentProgress();
        sp.setId(10L);
        sp.setSkillId(20L);
        sp.setUserId(30L);
        sp.setIsUnderstood(true);
        sp.setCreatedAt(NOW);
        sp.setUpdatedAt(NOW);

        assertEquals(10L, sp.getId());
        assertEquals(20L, sp.getSkillId());
        assertEquals(30L, sp.getUserId());
        assertTrue(sp.getIsUnderstood());
        assertEquals(NOW, sp.getCreatedAt());
        assertEquals(NOW, sp.getUpdatedAt());
    }

    // ── Dependency ───────────────────────────────────────────────────────────

    @Test
    void dependency_gettersAndSetters() {
        Skill from = new Skill();
        from.setId(1L);
        Skill to = new Skill();
        to.setId(2L);

        Dependency dep = new Dependency();
        dep.setId(5L);
        dep.setFromSkill(from);
        dep.setToSkill(to);
        dep.setCreatedAt(NOW);
        dep.setUpdatedAt(NOW);

        assertEquals(5L, dep.getId());
        assertEquals(from, dep.getFromSkill());
        assertEquals(to, dep.getToSkill());
        assertEquals(NOW, dep.getCreatedAt());
        assertEquals(NOW, dep.getUpdatedAt());
    }

    // ── User (hasSeenDashboard / Map fields) ─────────────────────────────────

    @Test
    void user_hasSeenFields() {
        User user = new User();
        user.setHasSeenDashboard(true);
        user.setHasSeenMapAsMember(true);
        user.setHasSeenMapAsOwner(true);

        assertTrue(user.isHasSeenDashboard());
        assertTrue(user.isHasSeenMapAsMember());
        assertTrue(user.isHasSeenMapAsOwner());
    }

    // ── Skill (missing getters/setters) ──────────────────────────────────────

    @Test
    void skill_gettersAndSetters() {
        SkillMap skillMap = new SkillMap();
        skillMap.setId(42L);

        Skill skill = new Skill();
        skill.setResources("res.pdf");
        skill.setNotes("my notes");
        skill.setCreatedAt(NOW);
        skill.setUpdatedAt(NOW);
        skill.setSkillMap(skillMap);

        assertEquals("res.pdf", skill.getResources());
        assertEquals("my notes", skill.getNotes());
        assertEquals(NOW, skill.getCreatedAt());
        assertEquals(NOW, skill.getUpdatedAt());
        assertEquals(skillMap, skill.getSkillMap());
    }

    // ── SkillMap (setCreatedAt/setUpdatedAt) ─────────────────────────────────

    @Test
    void skillMap_createdAtUpdatedAt() {
        SkillMap map = new SkillMap();
        map.setCreatedAt(NOW);
        map.setUpdatedAt(NOW);

        assertEquals(NOW, map.getCreatedAt());
        assertEquals(NOW, map.getUpdatedAt());
    }

    // ── SkillMapMembership (setJoinedAt) ─────────────────────────────────────

    @Test
    void skillMapMembership_joinedAt() {
        SkillMapMembership m = new SkillMapMembership();
        m.setJoinedAt(NOW);

        assertEquals(NOW, m.getJoinedAt());
    }

    // ── QuizAttempt (setCooldownUntil) ───────────────────────────────────────

    @Test
    void quizAttempt_setCooldownUntil() {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setCooldownUntil(NOW);

        assertEquals(NOW, attempt.getCooldownUntil());
    }

    // ── UserPutDTO ────────────────────────────────────────────────────────────

    @Test
    void userPutDTO_gettersAndSetters() {
        UserPutDTO dto = new UserPutDTO();
        dto.setId(1L);
        dto.setUsername("user1");
        dto.setBio("bio text");
        dto.setPassword("pass123");
        dto.setToken("tok-abc");

        assertEquals(1L, dto.getId());
        assertEquals("user1", dto.getUsername());
        assertEquals("bio text", dto.getBio());
        assertEquals("pass123", dto.getPassword());
        assertEquals("tok-abc", dto.getToken());
    }

    // ── SkillMapGraphDTO ──────────────────────────────────────────────────────

    @Test
    void skillMapGraphDTO_gettersAndSetters() {
        SkillMapGraphDTO dto = new SkillMapGraphDTO();
        dto.setSkillMapId(10L);
        dto.setTitle("Graph Title");
        dto.setSkills(java.util.List.of());
        dto.setDependencies(java.util.List.of());

        assertEquals(10L, dto.getSkillMapId());
        assertEquals("Graph Title", dto.getTitle());
        assertNotNull(dto.getSkills());
        assertNotNull(dto.getDependencies());
        assertNotNull(dto.getProgress());
    }

    // ── DependencyExportDTO ───────────────────────────────────────────────────

    @Test
    void dependencyExportDTO_gettersAndSetters() {
        DependencyExportDTO dto = new DependencyExportDTO();
        dto.setId(5L);
        dto.setFromExportId("from-1");
        dto.setToExportId("to-2");

        assertEquals(5L, dto.getId());
        assertEquals("from-1", dto.getFromExportId());
        assertEquals("to-2", dto.getToExportId());
    }

    // ── SkillPutDTO ───────────────────────────────────────────────────────────

    @Test
    void skillPutDTO_gettersAndSetters() {
        SkillPutDTO dto = new SkillPutDTO();
        dto.setName("name");
        dto.setDescription("desc");
        dto.setResources("res");
        dto.setDifficulty("EASY");
        dto.setLevel(1);
        dto.setPositionX(1.0f);
        dto.setNotes("notes");

        assertEquals("name", dto.getName());
        assertEquals("desc", dto.getDescription());
        assertEquals("res", dto.getResources());
        assertEquals("EASY", dto.getDifficulty());
        assertEquals(1, dto.getLevel());
        assertEquals(1.0f, dto.getPositionX());
        assertEquals("notes", dto.getNotes());
    }

    // ── SkillPostDTO ──────────────────────────────────────────────────────────

    @Test
    void skillPostDTO_gettersAndSetters() {
        SkillPostDTO dto = new SkillPostDTO();
        dto.setName("name");
        dto.setDescription("desc");
        dto.setResources("res");
        dto.setDifficulty("HARD");
        dto.setLevel(2);
        dto.setPositionX(2.0f);
        dto.setNotes("notes2");

        assertEquals("name", dto.getName());
        assertEquals("desc", dto.getDescription());
        assertEquals("res", dto.getResources());
        assertEquals("HARD", dto.getDifficulty());
        assertEquals(2, dto.getLevel());
        assertEquals(2.0f, dto.getPositionX());
        assertEquals("notes2", dto.getNotes());
    }

    // ── SkillExportDTO (missing setters) ─────────────────────────────────────

    @Test
    void skillExportDTO_skillMapIdAndQuiz() {
        SkillExportDTO dto = new SkillExportDTO();
        dto.setSkillMapId(99L);
        dto.setQuiz(null);

        assertEquals(99L, dto.getSkillMapId());
        assertNull(dto.getQuiz());
    }

    // ── SkillMapMembership (role) ─────────────────────────────────────────────

    @Test
    void skillMapMembership_roleGetterSetter() {
        SkillMapMembership m = new SkillMapMembership();
        m.setRole(SkillMapRole.OWNER);

        assertEquals(SkillMapRole.OWNER, m.getRole());
    }
}
