package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Quiz;
import ch.uzh.ifi.hase.soprafs26.entity.QuizAnswer;
import ch.uzh.ifi.hase.soprafs26.entity.QuizAttempt;
import ch.uzh.ifi.hase.soprafs26.entity.QuizQuestion;
import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.QuizAnswerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.QuizAttemptRepository;
import ch.uzh.ifi.hase.soprafs26.repository.QuizQuestionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.QuizRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapMembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SubmittedAnswerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class QuizAttemptService {
    private final Logger log = LoggerFactory.getLogger(QuizAttemptService.class);

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final SkillRepository skillRepository;
    private final SkillMapMembershipRepository skillMapMembershipRepository;

    public QuizAttemptService(
            @Qualifier("quizAttemptRepository") QuizAttemptRepository quizAttemptRepository,
            @Qualifier("quizRepository") QuizRepository quizRepository,
            @Qualifier("quizQuestionRepository") QuizQuestionRepository quizQuestionRepository,
            @Qualifier("quizAnswerRepository") QuizAnswerRepository quizAnswerRepository,
            @Qualifier("skillRepository") SkillRepository skillRepository,
            @Qualifier("skillMapMembershipRepository") SkillMapMembershipRepository skillMapMembershipRepository) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.quizRepository = quizRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.quizAnswerRepository = quizAnswerRepository;
        this.skillRepository = skillRepository;
        this.skillMapMembershipRepository = skillMapMembershipRepository;
    }

    private Quiz getQuizAndCheckAccess(Long quizId, User requester) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Quiz with ID %s not found.", quizId)));
        Skill skill = skillRepository.findById(quiz.getSkillId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found."));
        if (!skillMapMembershipRepository.existsBySkillMapIdAndUserId(skill.getSkillMap().getId(), requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no access to this skill map.");
        }
        return quiz;
    }

    // 1001 - create a new attempt
    public QuizAttempt createAttempt(Long quizId, User requester) {
        Quiz quiz = getQuizAndCheckAccess(quizId, requester);

        if (!quiz.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Quiz is not active.");
        }

        // check for an existing unsubmitted attempt
        quizAttemptRepository.findTopByUserIdAndQuizIdOrderByAttemptedAtDesc(requester.getId(), quizId)
                .ifPresent(existing -> {
                    if (existing.getPassed() == null) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "An unsubmitted attempt already exists.");
                    }
                });

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUserId(requester.getId());
        attempt.setQuizId(quizId);
        attempt = quizAttemptRepository.save(attempt);
        quizAttemptRepository.flush();
        log.debug("Created QuizAttempt for quiz {} by user {}", quizId, requester.getId());
        return attempt;
    }

    // 1002 - get latest attempt for the current user
    public QuizAttempt getLatestAttempt(Long quizId, User requester) {
        getQuizAndCheckAccess(quizId, requester);
        return quizAttemptRepository.findTopByUserIdAndQuizIdOrderByAttemptedAtDesc(requester.getId(), quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No attempt found for this quiz."));
    }

    // 1003 - get attempt by id
    public QuizAttempt getAttemptById(Long attemptId, User requester) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Attempt with ID %s not found.", attemptId)));
        getQuizAndCheckAccess(attempt.getQuizId(), requester);
        if (!attempt.getUserId().equals(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own attempts.");
        }
        return attempt;
    }

    // 1004 - submit attempt and calculate score
    public QuizAttempt submitAttempt(Long attemptId, List<SubmittedAnswerDTO> submittedAnswers, User requester) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Attempt with ID %s not found.", attemptId)));

        if (!attempt.getUserId().equals(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only submit your own attempts.");
        }
        if (attempt.getPassed() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This attempt has already been submitted.");
        }

        Quiz quiz = quizRepository.findById(attempt.getQuizId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found."));

        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderByOrderIndexAsc(quiz.getId());

        if (submittedAnswers == null || submittedAnswers.size() != questions.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Number of submitted answers must match number of questions.");
        }

        int correct = 0;
        for (SubmittedAnswerDTO submitted : submittedAnswers) {
            QuizAnswer answer = quizAnswerRepository.findById(submitted.getSelectedAnswerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            String.format("Answer with ID %s not found.", submitted.getSelectedAnswerId())));
            if (!answer.getQuizQuestionId().equals(submitted.getQuizQuestionId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Answer does not belong to the specified question.");
            }
            if (answer.getIsCorrect()) correct++;
        }

        int score = questions.isEmpty() ? 0 : (int) Math.round((double) correct / questions.size() * 100);
        int passMark = quiz.getPassMark() != null ? quiz.getPassMark() : 70;

        attempt.setScore(score);
        attempt.setPassed(score >= passMark);

        attempt = quizAttemptRepository.save(attempt);
        quizAttemptRepository.flush();
        log.debug("Submitted attempt {} — score: {}, passed: {}", attemptId, score, attempt.getPassed());
        return attempt;
    }
}