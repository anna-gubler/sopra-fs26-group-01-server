package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;

@ExtendWith(MockitoExtension.class)
class StudentProgressServiceTest {

    @Mock private SkillRepository skillRepository;
    @Mock private SkillMapMembershipRepository skillMapMembershipRepository;
    @Mock private DependencyRepository dependencyRepository;
    @Mock private StudentProgressRepository progressRepository;

    @InjectMocks
    private StudentProgressService studentProgressService;

    private User user;
    private UserService userService;
    private Skill skill;
    private SkillMap skillMap;
    private SkillMapMembership membership;

    @BeforeEach
    void setup() {
        // No userService here — unit tests bypass the interceptor entirely

        user = new User();
        user.setId(99L);

        skillMap = new SkillMap();
        skillMap.setId(1L);

        skill = new Skill();
        skill.setId(10L);
        skill.setSkillMap(skillMap);

        membership = new SkillMapMembership();
        membership.setUserId(99L);
        membership.setSkillMapId(1L);
    }

    @Test
    void updateProgress_noPrerequisites_success() {
        when(skillRepository.findById(10L)).thenReturn(Optional.of(skill));
        when(skillMapMembershipRepository.findBySkillMapIdAndUserId(1L, 99L))
            .thenReturn(Optional.of(membership));
        when(dependencyRepository.findByToSkill(skill)).thenReturn(List.of());
        when(progressRepository.findByUserIdAndSkillId(99L, 10L)).thenReturn(Optional.empty());
        when(progressRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        StudentProgress result = studentProgressService.updateProgress(10L, true, user);

        assertTrue(result.getIsUnderstood());
        assertNotNull(result.getIsUnderstandingDate());
        verify(progressRepository).save(any());
    }

    @Test
    void updateProgress_allPrerequisitesMet_success() {
        Skill prereq = new Skill();
        prereq.setId(5L);

        Dependency dep = new Dependency();
        dep.setFromSkill(prereq);

        StudentProgress prereqProgress = new StudentProgress();
        prereqProgress.setIsUnderstood(true);

        when(skillRepository.findById(10L)).thenReturn(Optional.of(skill));
        when(skillMapMembershipRepository.findBySkillMapIdAndUserId(1L, 99L))
            .thenReturn(Optional.of(membership));
        when(dependencyRepository.findByToSkill(skill)).thenReturn(List.of(dep));
        when(progressRepository.findByUserIdAndSkillId(99L, 5L))
            .thenReturn(Optional.of(prereqProgress));
        when(progressRepository.findByUserIdAndSkillId(99L, 10L)).thenReturn(Optional.empty());
        when(progressRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        StudentProgress result = studentProgressService.updateProgress(10L, true, user);

        assertTrue(result.getIsUnderstood());
    }

    @Test
    void updateProgress_prerequisitesNotMet_throwsBadRequest() {
        Skill prereq = new Skill();
        prereq.setId(5L);

        Dependency dep = new Dependency();
        dep.setFromSkill(prereq);

        when(skillRepository.findById(10L)).thenReturn(Optional.of(skill));
        when(skillMapMembershipRepository.findBySkillMapIdAndUserId(1L, 99L))
            .thenReturn(Optional.of(membership));
        when(dependencyRepository.findByToSkill(skill)).thenReturn(List.of(dep));
        when(progressRepository.findByUserIdAndSkillId(99L, 5L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> studentProgressService.updateProgress(10L, true, user));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void updateProgress_skillNotFound_throwsNotFound() {
        when(skillRepository.findById(10L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> studentProgressService.updateProgress(10L, true, user));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void updateProgress_notMember_throwsForbidden() {
        when(skillRepository.findById(10L)).thenReturn(Optional.of(skill));
        when(skillMapMembershipRepository.findBySkillMapIdAndUserId(1L, 99L))
            .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> studentProgressService.updateProgress(10L, true, user));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void updateProgress_unmark_clearsUnderstandingDate() {
        when(skillRepository.findById(10L)).thenReturn(Optional.of(skill));
        when(skillMapMembershipRepository.findBySkillMapIdAndUserId(1L, 99L))
            .thenReturn(Optional.of(membership));
        when(dependencyRepository.findByToSkill(skill)).thenReturn(List.of());
        when(progressRepository.findByUserIdAndSkillId(99L, 10L)).thenReturn(Optional.empty());
        when(progressRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        StudentProgress result = studentProgressService.updateProgress(10L, false, user);

        assertFalse(result.getIsUnderstood());
        assertNull(result.getIsUnderstandingDate());
    }
}