package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SubmittedAnswerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class QuizServiceIntegrationTest {

    @Autowired private QuizService quizService;
    @Autowired private QuizQuestionService quizQuestionService;
    @Autowired private QuizAnswerService quizAnswerService;
    @Autowired private QuizAttemptService quizAttemptService;

    @Autowired private UserRepository userRepository;
    @Autowired private UserService userService;
    @Autowired private SkillMapRepository skillMapRepository;
    @Autowired private SkillMapMembershipRepository skillMapMembershipRepository;
    @Autowired private SkillRepository skillRepository;
    @Autowired private QuizRepository quizRepository;
    @Autowired private QuizQuestionRepository quizQuestionRepository;
    @Autowired private QuizAnswerRepository quizAnswerRepository;
    @Autowired private QuizAttemptRepository quizAttemptRepository;

    private User owner;
    private User student;
    private SkillMap skillMap;
    private Skill skill;

    @BeforeEach
    void setup() {
        quizAttemptRepository.deleteAll();
        quizAnswerRepository.deleteAll();
        quizQuestionRepository.deleteAll();
        quizRepository.deleteAll();
        skillMapMembershipRepository.deleteAll();
        skillRepository.deleteAll();
        skillMapRepository.deleteAll();
        userRepository.deleteAll();

        User ownerInput = new User();
        ownerInput.setUsername("quizowner");
        ownerInput.setPassword("password123");
        owner = userService.createUser(ownerInput);

        User studentInput = new User();
        studentInput.setUsername("quizstudent");
        studentInput.setPassword("password456");
        student = userService.createUser(studentInput);

        SkillMap mapInput = new SkillMap();
        mapInput.setTitle("Quiz Test Map");
        mapInput.setIsPublic(false);
        mapInput.setNumberOfLevels(3);
        skillMap = new SkillMap();
        skillMap.setTitle("Quiz Test Map");
        skillMap.setIsPublic(true);
        skillMap.setNumberOfLevels(3);
        skillMap.setOwnerId(owner.getId());
        skillMap.setInviteCode("QTEST");
        skillMap = skillMapRepository.save(skillMap);

        SkillMapMembership ownerMembership = new SkillMapMembership();
        ownerMembership.setUserId(owner.getId());
        ownerMembership.setSkillMapId(skillMap.getId());
        ownerMembership.setRole(ch.uzh.ifi.hase.soprafs26.constant.SkillMapRole.OWNER);
        skillMapMembershipRepository.save(ownerMembership);

        SkillMapMembership studentMembership = new SkillMapMembership();
        studentMembership.setUserId(student.getId());
        studentMembership.setSkillMapId(skillMap.getId());
        studentMembership.setRole(ch.uzh.ifi.hase.soprafs26.constant.SkillMapRole.STUDENT);
        skillMapMembershipRepository.save(studentMembership);

        skill = new Skill();
        skill.setName("Java Basics");
        skill.setSkillMap(skillMap);
        skill.setLevel(1);
        skill.setIsLocked(false);
        skill.setDifficulty("EASY");
        skill = skillRepository.save(skill);
    }

    // ─── createQuiz ───────────────────────────────────────────────────────────

    @Test
    void createQuiz_persistedCorrectly() {
        Quiz input = new Quiz();
        input.setIsActive(true);
        input.setPassMark(70);

        Quiz result = quizService.createQuiz(skill.getId(), input, owner);

        assertNotNull(result.getId());
        assertTrue(quizRepository.findById(result.getId()).isPresent());
        assertEquals(skill.getId(), result.getSkillId());
    }

    @Test
    void createQuiz_alreadyExists_throws409() {
        Quiz input = new Quiz();
        input.setIsActive(true);
        quizService.createQuiz(skill.getId(), input, owner);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizService.createQuiz(skill.getId(), new Quiz(), owner));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void createQuiz_notOwner_throws403() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizService.createQuiz(skill.getId(), new Quiz(), student));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // ─── updateQuiz ───────────────────────────────────────────────────────────

    @Test
    void updateQuiz_changesPersistedCorrectly() {
        Quiz quiz = new Quiz();
        quiz.setIsActive(true);
        quiz.setPassMark(70);
        quiz = quizService.createQuiz(skill.getId(), quiz, owner);

        Quiz updates = new Quiz();
        updates.setPassMark(80);
        quizService.updateQuiz(quiz.getId(), updates, owner);

        Quiz updated = quizRepository.findById(quiz.getId()).orElseThrow();
        assertEquals(80, updated.getPassMark());
    }

    // ─── deleteQuiz ───────────────────────────────────────────────────────────

    @Test
    void deleteQuiz_removedFromDatabase() {
        Quiz quiz = new Quiz();
        quiz.setIsActive(true);
        quiz = quizService.createQuiz(skill.getId(), quiz, owner);
        Long quizId = quiz.getId();

        quizService.deleteQuiz(quizId, owner);

        assertFalse(quizRepository.findById(quizId).isPresent());
    }

    // ─── createQuestion + createAnswer + submitAttempt (full flow) ────────────

    @Test
    void fullQuizFlow_createQuizQuestionsAnswersAndSubmit() {
        // Quiz erstellen
        Quiz quiz = new Quiz();
        quiz.setIsActive(true);
        quiz.setPassMark(70);
        quiz = quizService.createQuiz(skill.getId(), quiz, owner);

        // Frage erstellen
        QuizQuestion questionInput = new QuizQuestion();
        questionInput.setQuizQuestionText("What is Java?");
        questionInput.setOrderIndex(0);
        QuizQuestion question = quizQuestionService.createQuestion(quiz.getId(), questionInput, owner);

        // Antworten erstellen
        QuizAnswer correctInput = new QuizAnswer();
        correctInput.setAnswerText("A programming language");
        correctInput.setIsCorrect(true);
        QuizAnswer correct = quizAnswerService.createAnswer(question.getId(), correctInput, owner);

        QuizAnswer wrongInput = new QuizAnswer();
        wrongInput.setAnswerText("A type of coffee");
        wrongInput.setIsCorrect(false);
        quizAnswerService.createAnswer(question.getId(), wrongInput, owner);

        // Attempt starten
        QuizAttempt attempt = quizAttemptService.createAttempt(quiz.getId(), student);
        assertNotNull(attempt.getId());
        assertNull(attempt.getPassed());

        // Submit mit korrekter Antwort
        SubmittedAnswerDTO submitted = new SubmittedAnswerDTO();
        submitted.setQuizQuestionId(question.getId());
        submitted.setSelectedAnswerId(correct.getId());

        QuizAttempt result = quizAttemptService.submitAttempt(attempt.getId(), List.of(submitted), student);

        assertEquals(100, result.getScore());
        assertTrue(result.getPassed());
    }

    @Test
    void submitAttempt_alreadySubmitted_throws409() {
        Quiz quiz = new Quiz();
        quiz.setIsActive(true);
        quiz.setPassMark(70);
        quiz = quizService.createQuiz(skill.getId(), quiz, owner);

        QuizQuestion questionInput = new QuizQuestion();
        questionInput.setQuizQuestionText("What is Java?");
        QuizQuestion question = quizQuestionService.createQuestion(quiz.getId(), questionInput, owner);

        QuizAnswer answerInput = new QuizAnswer();
        answerInput.setAnswerText("A language");
        answerInput.setIsCorrect(true);
        QuizAnswer answer = quizAnswerService.createAnswer(question.getId(), answerInput, owner);

        QuizAttempt attempt = quizAttemptService.createAttempt(quiz.getId(), student);

        SubmittedAnswerDTO submitted = new SubmittedAnswerDTO();
        submitted.setQuizQuestionId(question.getId());
        submitted.setSelectedAnswerId(answer.getId());

        quizAttemptService.submitAttempt(attempt.getId(), List.of(submitted), student);

        // nochmal submitten
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizAttemptService.submitAttempt(attempt.getId(), List.of(submitted), student));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void getLatestAttempt_afterSubmit_returnsCorrectAttempt() {
        Quiz quiz = new Quiz();
        quiz.setIsActive(true);
        quiz.setPassMark(70);
        quiz = quizService.createQuiz(skill.getId(), quiz, owner);

        QuizQuestion questionInput = new QuizQuestion();
        questionInput.setQuizQuestionText("What is Java?");
        QuizQuestion question = quizQuestionService.createQuestion(quiz.getId(), questionInput, owner);

        QuizAnswer answerInput = new QuizAnswer();
        answerInput.setAnswerText("A language");
        answerInput.setIsCorrect(true);
        QuizAnswer answer = quizAnswerService.createAnswer(question.getId(), answerInput, owner);

        QuizAttempt attempt = quizAttemptService.createAttempt(quiz.getId(), student);

        SubmittedAnswerDTO submitted = new SubmittedAnswerDTO();
        submitted.setQuizQuestionId(question.getId());
        submitted.setSelectedAnswerId(answer.getId());
        quizAttemptService.submitAttempt(attempt.getId(), List.of(submitted), student);

        QuizAttempt latest = quizAttemptService.getLatestAttempt(quiz.getId(), student);

        assertEquals(attempt.getId(), latest.getId());
        assertTrue(latest.getPassed());
    }
}