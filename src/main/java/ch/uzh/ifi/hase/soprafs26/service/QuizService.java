package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Quiz;
import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.entity.User;
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

@Service
@Transactional
public class QuizService {
    private final Logger log = LoggerFactory.getLogger(QuizService.class);

    private final QuizRepository quizRepository;
    private final SkillRepository skillRepository;
    private final SkillMapMembershipRepository skillMapMembershipRepository;

    public QuizService(
            @Qualifier("quizRepository") QuizRepository quizRepository,
            @Qualifier("skillRepository") SkillRepository skillRepository,
            @Qualifier("skillMapMembershipRepository") SkillMapMembershipRepository skillMapMembershipRepository) {
        this.quizRepository = quizRepository;
        this.skillRepository = skillRepository;
        this.skillMapMembershipRepository = skillMapMembershipRepository;
    }

    private Skill getSkillAndCheckAccess(Long skillId, User requester) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Skill with ID %s not found.", skillId)));
        Long skillMapId = skill.getSkillMap().getId();
        if (!skillMapMembershipRepository.existsBySkillMapIdAndUserId(skillMapId, requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no access to this skill map.");
        }
        return skill;
    }

    private void checkIsOwner(Skill skill, User requester) {
        Long ownerId = skill.getSkillMap().getOwnerId();
        if (!ownerId.equals(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can perform this action.");
        }
    }

    // 901 - get quiz for a skill
    public Quiz getQuizBySkillId(Long skillId, User requester) {
        getSkillAndCheckAccess(skillId, requester);
        return quizRepository.findBySkillId(skillId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("No quiz found for skill with ID %s.", skillId)));
    }

    // 902 - create quiz for a skill; only owner can do this
    public Quiz createQuiz(Long skillId, Quiz newQuiz, User requester) {
        Skill skill = getSkillAndCheckAccess(skillId, requester);
        checkIsOwner(skill, requester);

        if (quizRepository.findBySkillId(skillId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A quiz already exists for this skill.");
        }
        if (newQuiz.getPassMark() != null && (newQuiz.getPassMark() < 0 || newQuiz.getPassMark() > 100)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "passMark must be between 0 and 100.");
        }

        newQuiz.setSkillId(skillId);
        newQuiz = quizRepository.save(newQuiz);
        quizRepository.flush();
        log.debug("Created Quiz for skill {}: {}", skillId, newQuiz);
        return newQuiz;
    }

    // 903 - partial update; only owner can do this
    public Quiz updateQuiz(Long quizId, Quiz updates, User requester) {
        Quiz existing = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Quiz with ID %s not found.", quizId)));

        Skill skill = getSkillAndCheckAccess(existing.getSkillId(), requester);
        checkIsOwner(skill, requester);

        if (updates.getIsActive() != null) existing.setIsActive(updates.getIsActive());
        if (updates.getCooldownHours() != null) existing.setCooldownHours(updates.getCooldownHours());
        if (updates.getPassMark() != null) {
            if (updates.getPassMark() < 0 || updates.getPassMark() > 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "passMark must be between 0 and 100.");
            }
            existing.setPassMark(updates.getPassMark());
        }

        existing = quizRepository.save(existing);
        quizRepository.flush();
        log.debug("Updated Quiz {}: {}", quizId, existing);
        return existing;
    }

    // 904 - delete quiz; only owner can do this
    public void deleteQuiz(Long quizId, User requester) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Quiz with ID %s not found.", quizId)));

        Skill skill = getSkillAndCheckAccess(quiz.getSkillId(), requester);
        checkIsOwner(skill, requester);

        quizRepository.deleteById(quizId);
        quizRepository.flush();
        log.debug("Deleted Quiz {}", quizId);
    }
}