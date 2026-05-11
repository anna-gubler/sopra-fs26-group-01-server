package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Quiz;
import ch.uzh.ifi.hase.soprafs26.entity.QuizAnswer;
import ch.uzh.ifi.hase.soprafs26.entity.QuizQuestion;
import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.QuizAnswerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.QuizQuestionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.QuizRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapMembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class QuizAnswerService {
    private final Logger log = LoggerFactory.getLogger(QuizAnswerService.class);

    private final QuizAnswerRepository quizAnswerRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizRepository quizRepository;
    private final SkillRepository skillRepository;
    private final SkillMapMembershipRepository skillMapMembershipRepository;

    public QuizAnswerService(
            @Qualifier("quizAnswerRepository") QuizAnswerRepository quizAnswerRepository,
            @Qualifier("quizQuestionRepository") QuizQuestionRepository quizQuestionRepository,
            @Qualifier("quizRepository") QuizRepository quizRepository,
            @Qualifier("skillRepository") SkillRepository skillRepository,
            @Qualifier("skillMapMembershipRepository") SkillMapMembershipRepository skillMapMembershipRepository) {
        this.quizAnswerRepository = quizAnswerRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.quizRepository = quizRepository;
        this.skillRepository = skillRepository;
        this.skillMapMembershipRepository = skillMapMembershipRepository;
    }

    private void checkAccess(Long quizQuestionId, User requester) {
        QuizQuestion question = quizQuestionRepository.findById(quizQuestionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("QuizQuestion with ID %s not found.", quizQuestionId)));
        Quiz quiz = quizRepository.findById(question.getQuizId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found."));
        Skill skill = skillRepository.findById(quiz.getSkillId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found."));
        if (!skillMapMembershipRepository.existsBySkillMapIdAndUserId(skill.getSkillMap().getId(), requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no access to this skill map.");
        }
    }

    private void checkIsOwner(Long quizQuestionId, User requester) {
        QuizQuestion question = quizQuestionRepository.findById(quizQuestionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QuizQuestion not found."));
        Quiz quiz = quizRepository.findById(question.getQuizId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found."));
        Skill skill = skillRepository.findById(quiz.getSkillId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found."));
        if (!skill.getSkillMap().getOwnerId().equals(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can perform this action.");
        }
    }

    // 909 - get all answers for a question
    public List<QuizAnswer> getAnswersByQuestionId(Long quizQuestionId, User requester) {
        checkAccess(quizQuestionId, requester);
        return quizAnswerRepository.findByQuizQuestionId(quizQuestionId);
    }

    // 910 - create answer; only owner
    public QuizAnswer createAnswer(Long quizQuestionId, QuizAnswer newAnswer, User requester) {
        checkAccess(quizQuestionId, requester);
        checkIsOwner(quizQuestionId, requester);

        if (newAnswer.getAnswerText() == null || newAnswer.getAnswerText().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "answerText is required.");
        }
        if (newAnswer.getIsCorrect() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "isCorrect is required.");
        }

        newAnswer.setQuizQuestionId(quizQuestionId);
        newAnswer = quizAnswerRepository.save(newAnswer);
        quizAnswerRepository.flush();
        log.debug("Created QuizAnswer for question {}: {}", quizQuestionId, newAnswer);
        return newAnswer;
    }

    // 911 - partial update; only owner
    public QuizAnswer updateAnswer(Long answerId, QuizAnswer updates, User requester) {
        QuizAnswer existing = quizAnswerRepository.findById(answerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("QuizAnswer with ID %s not found.", answerId)));

        checkAccess(existing.getQuizQuestionId(), requester);
        checkIsOwner(existing.getQuizQuestionId(), requester);

        if (updates.getAnswerText() != null) {
            if (updates.getAnswerText().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "answerText cannot be blank.");
            }
            existing.setAnswerText(updates.getAnswerText());
        }
        if (updates.getIsCorrect() != null) existing.setIsCorrect(updates.getIsCorrect());

        existing = quizAnswerRepository.save(existing);
        quizAnswerRepository.flush();
        log.debug("Updated QuizAnswer {}: {}", answerId, existing);
        return existing;
    }

    // 912 - delete answer; only owner
    public void deleteAnswer(Long answerId, User requester) {
        QuizAnswer answer = quizAnswerRepository.findById(answerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("QuizAnswer with ID %s not found.", answerId)));

        checkAccess(answer.getQuizQuestionId(), requester);
        checkIsOwner(answer.getQuizQuestionId(), requester);

        quizAnswerRepository.deleteById(answerId);
        quizAnswerRepository.flush();
        log.debug("Deleted QuizAnswer {}", answerId);
    }
}