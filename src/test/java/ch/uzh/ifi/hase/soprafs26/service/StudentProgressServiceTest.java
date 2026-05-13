package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.StudentProgress;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapMembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillRepository;
import ch.uzh.ifi.hase.soprafs26.repository.StudentProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StudentProgressServiceTest {

    @Mock
    private SkillRepository skillRepository;
    @Mock
    private SkillMapMembershipRepository skillMapMembershipRepository;
    @Mock
    private StudentProgressRepository studentProgressRepository;

    @InjectMocks
    private StudentProgressService studentProgressService;

    private User owner;
    private User member;
    private User stranger;
    private SkillMap skillMap;
    private Skill skill;

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setId(1L);

        member = new User();
        member.setId(2L);

        stranger = new User();
        stranger.setId(3L);

        skillMap = new SkillMap();
        skillMap.setId(10L);
        skillMap.setOwnerId(owner.getId());

        skill = new Skill();
        skill.setId(5L);
        skill.setSkillMap(skillMap);
    }

    @Test
    void setProgress_owner_createsNewRecord() {
        given(skillRepository.findById(5L)).willReturn(Optional.of(skill));
        given(studentProgressRepository.findByUserIdAndSkillId(1L, 5L)).willReturn(Optional.empty());

        StudentProgress saved = new StudentProgress();
        saved.setUserId(1L);
        saved.setSkillId(5L);
        saved.setIsUnderstood(true);
        given(studentProgressRepository.save(any(StudentProgress.class))).willReturn(saved);

        StudentProgress result = studentProgressService.setProgress(5L, owner, true);

        assertTrue(result.getIsUnderstood());
        assertEquals(1L, result.getUserId());
        assertEquals(5L, result.getSkillId());
        verify(studentProgressRepository).save(any(StudentProgress.class));
    }

    @Test
    void setProgress_member_updatesExistingRecord() {
        given(skillRepository.findById(5L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, 2L)).willReturn(true);

        StudentProgress existing = new StudentProgress();
        existing.setUserId(2L);
        existing.setSkillId(5L);
        existing.setIsUnderstood(false);
        given(studentProgressRepository.findByUserIdAndSkillId(2L, 5L)).willReturn(Optional.of(existing));

        StudentProgress saved = new StudentProgress();
        saved.setUserId(2L);
        saved.setSkillId(5L);
        saved.setIsUnderstood(true);
        given(studentProgressRepository.save(existing)).willReturn(saved);

        StudentProgress result = studentProgressService.setProgress(5L, member, true);

        assertTrue(result.getIsUnderstood());
        verify(studentProgressRepository).save(existing);
    }

    @Test
    void setProgress_toggleToFalse_updatesRecord() {
        given(skillRepository.findById(5L)).willReturn(Optional.of(skill));

        StudentProgress existing = new StudentProgress();
        existing.setUserId(1L);
        existing.setSkillId(5L);
        existing.setIsUnderstood(true);
        given(studentProgressRepository.findByUserIdAndSkillId(1L, 5L)).willReturn(Optional.of(existing));

        StudentProgress saved = new StudentProgress();
        saved.setUserId(1L);
        saved.setSkillId(5L);
        saved.setIsUnderstood(false);
        given(studentProgressRepository.save(existing)).willReturn(saved);

        StudentProgress result = studentProgressService.setProgress(5L, owner, false);

        assertFalse(result.getIsUnderstood());
    }

    @Test
    void setProgress_skillNotFound_throws404() {
        given(skillRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> studentProgressService.setProgress(99L, owner, true));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void setProgress_stranger_throws403() {
        given(skillRepository.findById(5L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, 3L)).willReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> studentProgressService.setProgress(5L, stranger, true));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }
}
