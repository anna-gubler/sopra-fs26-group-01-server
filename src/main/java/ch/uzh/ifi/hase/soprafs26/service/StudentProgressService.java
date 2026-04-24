package ch.uzh.ifi.hase.soprafs26.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.entity.StudentProgress;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.DependencyRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapMembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillRepository;
import ch.uzh.ifi.hase.soprafs26.repository.StudentProgressRepository;

@Service
@Transactional
public class StudentProgressService {

    private final SkillRepository skillRepository;
    private final SkillMapMembershipRepository skillMapMembershipRepository;
    private final DependencyRepository dependencyRepository;
    private final StudentProgressRepository progressRepository;

    @Autowired
    public StudentProgressService(
            SkillRepository skillRepository,
            SkillMapMembershipRepository skillMapMembershipRepository,
            DependencyRepository dependencyRepository,
            StudentProgressRepository progressRepository) {
        this.skillRepository = skillRepository;
        this.skillMapMembershipRepository = skillMapMembershipRepository;
        this.dependencyRepository = dependencyRepository;
        this.progressRepository = progressRepository;
    }

    public StudentProgress updateProgress(Long skillId, Boolean isUnderstood, User user) {
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Long userId = user.getId();
        
        skillMapMembershipRepository.findBySkillMapIdAndUserId(skill.getSkillMap().getId(), userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

        // 403
        skillMapMembershipRepository.findBySkillMapIdAndUserId(skill.getSkillMap().getId(), userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        // Locked-Check
        boolean allPrereqsMet = dependencyRepository.findByToSkill(skill).stream()
            .allMatch(dep -> progressRepository
                .findByUserIdAndSkillId(userId, dep.getFromSkill().getId())
                .map(StudentProgress::getIsUnderstood)
                .orElse(false));

        if (!allPrereqsMet && isUnderstood) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prerequisites not met");
        }

        // Upsert
        StudentProgress progress = progressRepository
            .findByUserIdAndSkillId(userId, skillId)
            .orElse(new StudentProgress());

        progress.setUserId(userId);
        progress.setSkillId(skillId);
        progress.setIsUnderstood(isUnderstood);
        progress.setIsUnderstandingDate(isUnderstood ? LocalDateTime.now() : null);
        progress.setUpdatedAt(LocalDateTime.now());
        return progressRepository.save(progress);
    }
}