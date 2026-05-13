package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.entity.StudentProgress;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapMembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillRepository;
import ch.uzh.ifi.hase.soprafs26.repository.StudentProgressRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class StudentProgressService {

    private final SkillRepository skillRepository;
    private final SkillMapMembershipRepository skillMapMembershipRepository;
    private final StudentProgressRepository studentProgressRepository;

    public StudentProgressService(SkillRepository skillRepository,
                                  SkillMapMembershipRepository skillMapMembershipRepository,
                                  StudentProgressRepository studentProgressRepository) {
        this.skillRepository = skillRepository;
        this.skillMapMembershipRepository = skillMapMembershipRepository;
        this.studentProgressRepository = studentProgressRepository;
    }

    public StudentProgress setProgress(Long skillId, User currentUser, Boolean isUnderstood) {
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found"));

        Long skillMapId = skill.getSkillMap().getId();
        Long userId = currentUser.getId();

        boolean isOwner = userId.equals(skill.getSkillMap().getOwnerId());
        boolean isMember = skillMapMembershipRepository.existsBySkillMapIdAndUserId(skillMapId, userId);

        if (!isOwner && !isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        StudentProgress progress = studentProgressRepository
            .findByUserIdAndSkillId(userId, skillId)
            .orElseGet(() -> {
                StudentProgress p = new StudentProgress();
                p.setUserId(userId);
                p.setSkillId(skillId);
                return p;
            });

        progress.setIsUnderstood(isUnderstood);
        return studentProgressRepository.save(progress);
    }
}
