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
class QuizQuestionServiceTest {

    @Mock private QuizQuestionRepository quizQuestionRepository;
    @Mock private QuizRepository quizRepository;
    @Mock private SkillRepository skillRepository;
    @Mock private SkillMapMembershipRepository skillMapMembershipRepository;

    @InjectMocks private QuizQuestionService quizQuestionService;

    private User owner;
    private User student;
    private SkillMap skillMap;
    private Skill skill;
    private Quiz quiz;
    private QuizQuestion question;

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
        question.setQuizQuestionText("What is Java?");
    }

    // ─── 905 getQuestionsByQuizId ─────────────────────────────────────────────

    @Test
    void getQuestionsByQuizId_member_returnsQuestions() {
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);
        given(quizQuestionRepository.findByQuizIdOrderByOrderIndexAsc(30L)).willReturn(List.of(question));

        List<QuizQuestion> result = quizQuestionService.getQuestionsByQuizId(30L, owner);

        assertEquals(1, result.size());
        assertEquals(question.getId(), result.get(0).getId());
    }

    @Test
    void getQuestionsByQuizId_notMember_throws403() {
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, student.getId())).willReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizQuestionService.getQuestionsByQuizId(30L, student));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // ─── 906 createQuestion ───────────────────────────────────────────────────

    @Test
    void createQuestion_validInput_returnsCreatedQuestion() {
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);
        given(quizQuestionRepository.save(any(QuizQuestion.class))).willReturn(question);

        QuizQuestion input = new QuizQuestion();
        input.setQuizQuestionText("What is Java?");

        QuizQuestion result = quizQuestionService.createQuestion(30L, input, owner);

        assertEquals(question.getId(), result.getId());
        verify(quizQuestionRepository).save(any(QuizQuestion.class));
    }

    @Test
    void createQuestion_notOwner_throws403() {
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, student.getId())).willReturn(true);

        QuizQuestion input = new QuizQuestion();
        input.setQuizQuestionText("What is Java?");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizQuestionService.createQuestion(30L, input, student));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void createQuestion_blankText_throws400() {
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);

        QuizQuestion input = new QuizQuestion();
        input.setQuizQuestionText("  ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizQuestionService.createQuestion(30L, input, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createQuestion_nullText_throws400() {
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);

        QuizQuestion input = new QuizQuestion();
        input.setQuizQuestionText(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizQuestionService.createQuestion(30L, input, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(quizQuestionRepository, never()).save(any());
    }

    // ─── 907 updateQuestion ───────────────────────────────────────────────────

    @Test
    void updateQuestion_notOwner_throws403() {
        given(quizQuestionRepository.findById(40L)).willReturn(Optional.of(question));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, student.getId())).willReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizQuestionService.updateQuestion(40L, new QuizQuestion(), student));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void updateQuestion_validInput_returnsUpdatedQuestion() {
        given(quizQuestionRepository.findById(40L)).willReturn(Optional.of(question));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);
        given(quizQuestionRepository.save(any(QuizQuestion.class))).willReturn(question);

        QuizQuestion updates = new QuizQuestion();
        updates.setQuizQuestionText("Updated question?");

        QuizQuestion result = quizQuestionService.updateQuestion(40L, updates, owner);

        assertNotNull(result);
        verify(quizQuestionRepository).save(any(QuizQuestion.class));
    }

    @Test
    void updateQuestion_notFound_throws404() {
        given(quizQuestionRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizQuestionService.updateQuestion(99L, new QuizQuestion(), owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void updateQuestion_blankText_throws400() {
        given(quizQuestionRepository.findById(40L)).willReturn(Optional.of(question));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);

        QuizQuestion updates = new QuizQuestion();
        updates.setQuizQuestionText("   ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizQuestionService.updateQuestion(40L, updates, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(quizQuestionRepository, never()).save(any());
    }

    @Test
    void updateQuestion_orderIndex_updatesCorrectly() {
        given(quizQuestionRepository.findById(40L)).willReturn(Optional.of(question));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);
        given(quizQuestionRepository.save(any(QuizQuestion.class))).willAnswer(inv -> inv.getArgument(0));

        QuizQuestion updates = new QuizQuestion();
        updates.setOrderIndex(5);

        QuizQuestion result = quizQuestionService.updateQuestion(40L, updates, owner);

        assertEquals(5, result.getOrderIndex());
        verify(quizQuestionRepository).save(question);
    }

    // ─── 908 deleteQuestion ───────────────────────────────────────────────────

    @Test
    void deleteQuestion_notOwner_throws403() {
        given(quizQuestionRepository.findById(40L)).willReturn(Optional.of(question));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, student.getId())).willReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizQuestionService.deleteQuestion(40L, student));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void deleteQuestion_owner_deletesSuccessfully() {
        given(quizQuestionRepository.findById(40L)).willReturn(Optional.of(question));
        given(quizRepository.findById(30L)).willReturn(Optional.of(quiz));
        given(skillRepository.findById(20L)).willReturn(Optional.of(skill));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);

        assertDoesNotThrow(() -> quizQuestionService.deleteQuestion(40L, owner));
        verify(quizQuestionRepository).deleteById(40L);
    }

    @Test
    void deleteQuestion_notFound_throws404() {
        given(quizQuestionRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizQuestionService.deleteQuestion(99L, owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}