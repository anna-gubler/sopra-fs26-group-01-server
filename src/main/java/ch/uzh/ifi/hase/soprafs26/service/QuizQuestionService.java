package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Quiz;
import ch.uzh.ifi.hase.soprafs26.entity.QuizQuestion;
import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.entity.User;
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
public class QuizQuestionService {
    private final Logger log = LoggerFactory.getLogger(QuizQuestionService.class);

    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizRepository quizRepository;
    private final SkillRepository skillRepository;
    private final SkillMapMembershipRepository skillMapMembershipRepository;

    public QuizQuestionService(
            @Qualifier("quizQuestionRepository") QuizQuestionRepository quizQuestionRepository,
            @Qualifier("quizRepository") QuizRepository quizRepository,
            @Qualifier("skillRepository") SkillRepository skillRepository,
            @Qualifier("skillMapMembershipRepository") SkillMapMembershipRepository skillMapMembershipRepository) {
        this.quizQuestionRepository = quizQuestionRepository;
        this.quizRepository = quizRepository;
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

    private void checkIsOwner(Quiz quiz, User requester) {
        Skill skill = skillRepository.findById(quiz.getSkillId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found."));
        if (!skill.getSkillMap().getOwnerId().equals(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can perform this action.");
        }
    }

    // 905 - get all questions for a quiz
    public List<QuizQuestion> getQuestionsByQuizId(Long quizId, User requester) {
        getQuizAndCheckAccess(quizId, requester);
        return quizQuestionRepository.findByQuizIdOrderByOrderIndexAsc(quizId);
    }

    // 906 - create question; only owner
    public QuizQuestion createQuestion(Long quizId, QuizQuestion newQuestion, User requester) {
        Quiz quiz = getQuizAndCheckAccess(quizId, requester);
        checkIsOwner(quiz, requester);

        if (newQuestion.getQuizQuestionText() == null || newQuestion.getQuizQuestionText().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "questionText is required.");
        }

        newQuestion.setQuizId(quizId);
        newQuestion = quizQuestionRepository.save(newQuestion);
        quizQuestionRepository.flush();
        log.debug("Created QuizQuestion for quiz {}: {}", quizId, newQuestion);
        return newQuestion;
    }

    // 907 - partial update; only owner
    public QuizQuestion updateQuestion(Long quizQuestionId, QuizQuestion updates, User requester) {
        QuizQuestion existing = quizQuestionRepository.findById(quizQuestionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("QuizQuestion with ID %s not found.", quizQuestionId)));

        Quiz quiz = getQuizAndCheckAccess(existing.getQuizId(), requester);
        checkIsOwner(quiz, requester);

        if (updates.getQuizQuestionText() != null) {
            if (updates.getQuizQuestionText().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "questionText cannot be blank.");
            }
            existing.setQuizQuestionText(updates.getQuizQuestionText());
        }
        if (updates.getOrderIndex() != null) existing.setOrderIndex(updates.getOrderIndex());

        existing = quizQuestionRepository.save(existing);
        quizQuestionRepository.flush();
        log.debug("Updated QuizQuestion {}: {}", quizQuestionId, existing);
        return existing;
    }

    // 908 - delete question; only owner
    public void deleteQuestion(Long quizQuestionId, User requester) {
        QuizQuestion question = quizQuestionRepository.findById(quizQuestionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("QuizQuestion with ID %s not found.", quizQuestionId)));

        Quiz quiz = getQuizAndCheckAccess(question.getQuizId(), requester);
        checkIsOwner(quiz, requester);

        quizQuestionRepository.deleteById(quizQuestionId);
        quizQuestionRepository.flush();
        log.debug("Deleted QuizQuestion {}", quizQuestionId);
    }
}
