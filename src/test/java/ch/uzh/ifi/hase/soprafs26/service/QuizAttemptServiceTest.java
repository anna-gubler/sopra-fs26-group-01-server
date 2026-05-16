package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SubmittedAnswerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizAttemptServiceTest {

    @Mock private QuizAttemptRepository quizAttemptRepository;
    @Mock private QuizRepository quizRepository;
    @Mock private QuizQuestionRepository quizQuestionRepository;
    @Mock private QuizAnswerRepository quizAnswerRepository;
    @Mock private SkillRepository skillRepository;
    @Mock private SkillMapMembershipRepository skillMapMembershipRepository;

    @InjectMocks private QuizAttemptService quizAttemptService;

    private User owner;
    private User student;
    private SkillMap skillMap;
    private Skill skill;
    private Quiz quiz;
    private QuizQuestion question;
    private QuizAnswer correctAnswer;
    private QuizAnswer wrongAnswer;
    private QuizAttempt attempt;

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setId(1L);

        student = new User();
        student.setId(2L);

        skillMap = new SkillMap();
        skillMap.setId(10L);
        skillMap.setOwnerId(1L);

        skill = new Skill();
        skill.setId(20L);
        skill.setSkillMap(skillMap);

        quiz = new Quiz();
        quiz.setId(30L);
        quiz.setSkillId(20L);
        quiz.setIsActive(true);
        quiz.setPassMark(70);

        question = new QuizQuestion();
        question.setId(40L);
        question.setQuizId(30L);
        question.setQuizQuestionText("What is Java?");

        correctAnswer = new QuizAnswer();
        correctAnswer.setId(50L);
        correctAnswer.setQuizQuestionId(40L);
        correctAnswer.setIsCorrect(true);

        wrongAnswer = new QuizAnswer();
        wrongAnswer.setId(51L);
        wrongAnswer.setQuizQuestionId(40L);
        wrongAnswer.setIsCorrect(false);

        attempt = new QuizAttempt();
        attempt.setUserId(2L);
        attempt.setQuizId(30L);
    }

    // ─── 1001 createAttempt ───────────────────────────────────────────────────

    @Test
    void createAttempt_validRequest_returnsAttempt() {
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, student.getId())).willReturn(true);
        given(quizAttemptRepository.findTopByUserIdAndQuizIdOrderByAttemptedAtDesc(student.getId(), 30L))
                .willReturn(Optional.empty());
        given(quizAttemptRepository.save(any(QuizAttempt.class))).willReturn(attempt);

        QuizAttempt result = quizAttemptService.createAttempt(30L, student);

        assertEquals(attempt.getId(), result.getId());
        verify(quizAttemptRepository).save(any(QuizAttempt.class));
    }

    @Test
    void createAttempt_quizNotActive_throws403() {
        quiz.setIsActive(false);
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, student.getId())).willReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizAttemptService.createAttempt(30L, student));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void createAttempt_unsubmittedAttemptExists_throws409() {
        QuizAttempt unsubmitted = new QuizAttempt();
        unsubmitted.setPassed(null);

        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, student.getId())).willReturn(true);
        given(quizAttemptRepository.findTopByUserIdAndQuizIdOrderByAttemptedAtDesc(student.getId(), 30L))
                .willReturn(Optional.of(unsubmitted));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizAttemptService.createAttempt(30L, student));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void createAttempt_notMember_throws403() {
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, student.getId())).willReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizAttemptService.createAttempt(30L, student));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // ─── 1002 getLatestAttempt ────────────────────────────────────────────────

    @Test
    void getLatestAttempt_exists_returnsAttempt() {
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, student.getId())).willReturn(true);
        given(quizAttemptRepository.findTopByUserIdAndQuizIdOrderByAttemptedAtDesc(student.getId(), 30L))
                .willReturn(Optional.of(attempt));

        QuizAttempt result = quizAttemptService.getLatestAttempt(30L, student);

        assertEquals(attempt.getId(), result.getId());
    }

    @Test
    void getLatestAttempt_noAttempt_throws404() {
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, student.getId())).willReturn(true);
        given(quizAttemptRepository.findTopByUserIdAndQuizIdOrderByAttemptedAtDesc(student.getId(), 30L))
                .willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizAttemptService.getLatestAttempt(30L, student));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ─── 1004 submitAttempt ───────────────────────────────────────────────────

    @Test
    void submitAttempt_allCorrect_setsPassedTrue() {
        given(quizAttemptRepository.findById(60L)).willReturn(Optional.of(attempt));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(quizQuestionRepository.findByQuizIdOrderByOrderIndexAsc(30L)).willReturn(List.of(question));
        given(quizAnswerRepository.findById(50L)).willReturn(Optional.of(correctAnswer));
        given(quizAttemptRepository.save(any(QuizAttempt.class))).willAnswer(inv -> inv.getArgument(0));

        SubmittedAnswerDTO submitted = new SubmittedAnswerDTO();
        submitted.setQuizQuestionId(question.getId());
        submitted.setSelectedAnswerId(correctAnswer.getId());

        QuizAttempt result = quizAttemptService.submitAttempt(60L, List.of(submitted), student);

        assertEquals(100, result.getScore());
        assertTrue(result.getPassed());
    }

    @Test
    void submitAttempt_allWrong_setsPassedFalse() {
        given(quizAttemptRepository.findById(60L)).willReturn(Optional.of(attempt));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(quizQuestionRepository.findByQuizIdOrderByOrderIndexAsc(30L)).willReturn(List.of(question));
        given(quizAnswerRepository.findById(51L)).willReturn(Optional.of(wrongAnswer));
        given(quizAttemptRepository.save(any(QuizAttempt.class))).willAnswer(inv -> inv.getArgument(0));

        SubmittedAnswerDTO submitted = new SubmittedAnswerDTO();
        submitted.setQuizQuestionId(question.getId());
        submitted.setSelectedAnswerId(wrongAnswer.getId());

        QuizAttempt result = quizAttemptService.submitAttempt(60L, List.of(submitted), student);

        assertEquals(0, result.getScore());
        assertFalse(result.getPassed());
    }

    @Test
    void submitAttempt_alreadySubmitted_throws409() {
        attempt.setPassed(true);
        given(quizAttemptRepository.findById(60L)).willReturn(Optional.of(attempt));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizAttemptService.submitAttempt(60L, List.of(), student));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void submitAttempt_wrongNumberOfAnswers_throws400() {
        given(quizAttemptRepository.findById(60L)).willReturn(Optional.of(attempt));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(quizQuestionRepository.findByQuizIdOrderByOrderIndexAsc(30L)).willReturn(List.of(question));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizAttemptService.submitAttempt(60L, List.of(), student));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void submitAttempt_answerNotBelongToQuestion_throws400() {
        QuizAnswer foreignAnswer = new QuizAnswer();
        foreignAnswer.setQuizQuestionId(99L); // andere Frage
        foreignAnswer.setIsCorrect(true);

        given(quizAttemptRepository.findById(60L)).willReturn(Optional.of(attempt));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));

        SubmittedAnswerDTO submitted = new SubmittedAnswerDTO();
        submitted.setQuizQuestionId(question.getId());
        submitted.setSelectedAnswerId(52L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizAttemptService.submitAttempt(60L, List.of(submitted), student));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void submitAttempt_notOwnAttempt_throws403() {
        attempt.setUserId(owner.getId());
        given(quizAttemptRepository.findById(60L)).willReturn(Optional.of(attempt));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizAttemptService.submitAttempt(60L, List.of(), student));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void submitAttempt_partiallyCorrect_calculatesScoreCorrectly() {
        QuizQuestion question2 = new QuizQuestion();
        question2.setId(41L);
        question2.setQuizId(30L);

        QuizAnswer correctAnswer2 = new QuizAnswer();
        correctAnswer2.setId(52L);
        correctAnswer2.setQuizQuestionId(41L);
        correctAnswer2.setIsCorrect(false);

        given(quizAttemptRepository.findById(60L)).willReturn(Optional.of(attempt));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(quizQuestionRepository.findByQuizIdOrderByOrderIndexAsc(30L))
                .willReturn(List.of(question, question2));
        given(quizAnswerRepository.findById(50L)).willReturn(Optional.of(correctAnswer));  // correct
        given(quizAnswerRepository.findById(52L)).willReturn(Optional.of(correctAnswer2)); // wrong
        given(quizAttemptRepository.save(any(QuizAttempt.class))).willAnswer(inv -> inv.getArgument(0));

        SubmittedAnswerDTO s1 = new SubmittedAnswerDTO();
        s1.setQuizQuestionId(question.getId());
        s1.setSelectedAnswerId(correctAnswer.getId());

        SubmittedAnswerDTO s2 = new SubmittedAnswerDTO();
        s2.setQuizQuestionId(question2.getId());
        s2.setSelectedAnswerId(correctAnswer2.getId());

        QuizAttempt result = quizAttemptService.submitAttempt(60L, List.of(s1, s2), student);

        assertEquals(50, result.getScore()); // 1 out of 2 correct = 50%
        assertFalse(result.getPassed());     // 50 < 70 passMark
    }

    @Test
    void submitAttempt_noQuestions_returnsScoreZero() {
        given(quizAttemptRepository.findById(60L)).willReturn(Optional.of(attempt));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(quizQuestionRepository.findByQuizIdOrderByOrderIndexAsc(30L)).willReturn(List.of());
        given(quizAttemptRepository.save(any(QuizAttempt.class))).willAnswer(inv -> inv.getArgument(0));

        QuizAttempt result = quizAttemptService.submitAttempt(60L, List.of(), student);

        assertEquals(0, result.getScore());
        assertFalse(result.getPassed());
    }

    //1003 getAttemptById

    @Test
    void getAttemptById_validRequest_returnsAttempt() {
        attempt.setUserId(student.getId());
        given(quizAttemptRepository.findById(60L)).willReturn(Optional.of(attempt));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, student.getId())).willReturn(true);

        QuizAttempt result = quizAttemptService.getAttemptById(60L, student);

        assertEquals(attempt.getId(), result.getId());
    }

    @Test
    void getAttemptById_notFound_throws404() {
        given(quizAttemptRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizAttemptService.getAttemptById(99L, student));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getAttemptById_notOwnAttempt_throws403() {
        attempt.setUserId(owner.getId());
        given(quizAttemptRepository.findById(60L)).willReturn(Optional.of(attempt));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, student.getId())).willReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizAttemptService.getAttemptById(60L, student));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }
}