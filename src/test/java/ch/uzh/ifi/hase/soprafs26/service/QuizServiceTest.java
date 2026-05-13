package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock private QuizRepository quizRepository;
    @Mock private SkillRepository skillRepository;
    @Mock private SkillMapMembershipRepository skillMapMembershipRepository;

    @InjectMocks private QuizService quizService;

    private User owner;
    private User student;
    private SkillMap skillMap;
    private Skill skill;
    private Quiz quiz;

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setId(1L);

        student = new User();
        student.setId(2L);

        skillMap = new SkillMap();
        skillMap.setId(10L);
        skillMap.setOwnerId(owner.getId());

        skill = new Skill();
        skill.setId(20L);
        skill.setSkillMap(skillMap);

        quiz = new Quiz();
        quiz.setSkillId(skill.getId());
        quiz.setIsActive(true);
        quiz.setPassMark(70);
    }

    // ─── 901 getQuizBySkillId ─────────────────────────────────────────────────

    @Test
    void getQuizBySkillId_member_returnsQuiz() {
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);
        given(quizRepository.findBySkillId(20L)).willReturn(Optional.of(quiz));

        Quiz result = quizService.getQuizBySkillId(20L, owner);

        assertEquals(quiz.getId(), result.getId());
    }

    @Test
    void getQuizBySkillId_skillNotFound_throws404() {
        given(skillRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizService.getQuizBySkillId(99L, owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getQuizBySkillId_notMember_throws403() {
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, student.getId())).willReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizService.getQuizBySkillId(20L, student));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void getQuizBySkillId_noQuizExists_throws404() {
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);
        given(quizRepository.findBySkillId(20L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizService.getQuizBySkillId(20L, owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ─── 902 createQuiz ───────────────────────────────────────────────────────

    @Test
    void createQuiz_validInput_returnsCreatedQuiz() {
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);
        given(quizRepository.findBySkillId(20L)).willReturn(Optional.empty());
        given(quizRepository.save(any(Quiz.class))).willReturn(quiz);

        Quiz input = new Quiz();
        input.setIsActive(true);
        input.setPassMark(70);

        Quiz result = quizService.createQuiz(20L, input, owner);

        assertEquals(quiz.getId(), result.getId());
        verify(quizRepository).save(any(Quiz.class));
    }

    @Test
    void createQuiz_notOwner_throws403() {
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, student.getId())).willReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizService.createQuiz(20L, new Quiz(), student));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void createQuiz_alreadyExists_throws409() {
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);
        given(quizRepository.findBySkillId(20L)).willReturn(Optional.of(quiz));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizService.createQuiz(20L, new Quiz(), owner));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void createQuiz_invalidPassMark_throws400() {
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);
        given(quizRepository.findBySkillId(20L)).willReturn(Optional.empty());

        Quiz input = new Quiz();
        input.setPassMark(150);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizService.createQuiz(20L, input, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // ─── 903 updateQuiz ───────────────────────────────────────────────────────

    @Test
    void updateQuiz_validInput_returnsUpdatedQuiz() {
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);
        given(quizRepository.save(any(Quiz.class))).willReturn(quiz);

        Quiz updates = new Quiz();
        updates.setIsActive(false);

        Quiz result = quizService.updateQuiz(30L, updates, owner);

        assertNotNull(result);
        verify(quizRepository).save(any(Quiz.class));
    }

    @Test
    void updateQuiz_notOwner_throws403() {
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, student.getId())).willReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizService.updateQuiz(30L, new Quiz(), student));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void updateQuiz_notFound_throws404() {
        given(quizRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizService.updateQuiz(99L, new Quiz(), owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ─── 904 deleteQuiz ───────────────────────────────────────────────────────

    @Test
    void deleteQuiz_owner_deletesSuccessfully() {
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);

        assertDoesNotThrow(() -> quizService.deleteQuiz(30L, owner));
        verify(quizRepository).deleteById(30L);
    }

    @Test
    void deleteQuiz_notOwner_throws403() {
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, student.getId())).willReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizService.deleteQuiz(30L, student));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(quizRepository, never()).deleteById(any());
    }

    @Test
    void deleteQuiz_notFound_throws404() {
        given(quizRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizService.deleteQuiz(99L, owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}