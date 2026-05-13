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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizAnswerServiceTest {

    @Mock private QuizAnswerRepository quizAnswerRepository;
    @Mock private QuizQuestionRepository quizQuestionRepository;
    @Mock private QuizRepository quizRepository;
    @Mock private SkillRepository skillRepository;
    @Mock private SkillMapMembershipRepository skillMapMembershipRepository;

    @InjectMocks private QuizAnswerService quizAnswerService;

    private User owner;
    private User student;
    private SkillMap skillMap;
    private Skill skill;
    private Quiz quiz;
    private QuizQuestion question;
    private QuizAnswer answer;

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

        question = new QuizQuestion();
        question.setQuizId(30L);

        answer = new QuizAnswer();
        answer.setQuizQuestionId(40L);
        answer.setAnswerText("Java is a programming language.");
        answer.setIsCorrect(true);
    }

    // ─── 909 getAnswersByQuestionId ───────────────────────────────────────────

    @Test
    void getAnswersByQuestionId_member_returnsAnswers() {
        given(quizQuestionRepository.findById(40L)).willReturn(Optional.of(question));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);
        given(quizAnswerRepository.findByQuizQuestionId(40L)).willReturn(List.of(answer));

        List<QuizAnswer> result = quizAnswerService.getAnswersByQuestionId(40L, owner);

        assertEquals(1, result.size());
        assertEquals(answer.getId(), result.get(0).getId());
    }

    @Test
    void getAnswersByQuestionId_notMember_throws403() {
        given(quizQuestionRepository.findById(40L)).willReturn(Optional.of(question));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, student.getId())).willReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizAnswerService.getAnswersByQuestionId(40L, student));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // ─── 910 createAnswer ─────────────────────────────────────────────────────

    @Test
    void createAnswer_validInput_returnsCreatedAnswer() {
        given(quizQuestionRepository.findById(40L)).willReturn(Optional.of(question));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);
        given(quizAnswerRepository.save(any(QuizAnswer.class))).willReturn(answer);

        QuizAnswer input = new QuizAnswer();
        input.setAnswerText("Java is a programming language.");
        input.setIsCorrect(true);

        QuizAnswer result = quizAnswerService.createAnswer(40L, input, owner);

        assertEquals(answer.getId(), result.getId());
        verify(quizAnswerRepository).save(any(QuizAnswer.class));
    }

    @Test
    void createAnswer_notOwner_throws403() {
        given(quizQuestionRepository.findById(40L)).willReturn(Optional.of(question));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, student.getId())).willReturn(true);

        QuizAnswer input = new QuizAnswer();
        input.setAnswerText("Some answer");
        input.setIsCorrect(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizAnswerService.createAnswer(40L, input, student));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void createAnswer_missingIsCorrect_throws400() {
        given(quizQuestionRepository.findById(40L)).willReturn(Optional.of(question));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);

        QuizAnswer input = new QuizAnswer();
        input.setAnswerText("Some answer");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizAnswerService.createAnswer(40L, input, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // ─── 911 updateAnswer ─────────────────────────────────────────────────────

    @Test
    void updateAnswer_validInput_returnsUpdatedAnswer() {
        given(quizAnswerRepository.findById(50L)).willReturn(Optional.of(answer));
        given(quizQuestionRepository.findById(40L)).willReturn(Optional.of(question));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);
        given(quizAnswerRepository.save(any(QuizAnswer.class))).willReturn(answer);

        QuizAnswer updates = new QuizAnswer();
        updates.setAnswerText("Updated answer");

        QuizAnswer result = quizAnswerService.updateAnswer(50L, updates, owner);

        assertNotNull(result);
        verify(quizAnswerRepository).save(any(QuizAnswer.class));
    }

    @Test
    void updateAnswer_notFound_throws404() {
        given(quizAnswerRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizAnswerService.updateAnswer(99L, new QuizAnswer(), owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ─── 912 deleteAnswer ─────────────────────────────────────────────────────

    @Test
    void deleteAnswer_owner_deletesSuccessfully() {
        given(quizAnswerRepository.findById(50L)).willReturn(Optional.of(answer));
        given(quizQuestionRepository.findById(40L)).willReturn(Optional.of(question));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);

        assertDoesNotThrow(() -> quizAnswerService.deleteAnswer(50L, owner));
        verify(quizAnswerRepository).deleteById(50L);
    }

    @Test
    void deleteAnswer_notFound_throws404() {
        given(quizAnswerRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizAnswerService.deleteAnswer(99L, owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}