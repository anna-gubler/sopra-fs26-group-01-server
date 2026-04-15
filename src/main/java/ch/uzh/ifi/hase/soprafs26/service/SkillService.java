package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapRepository skillMapRepository;
    private final SkillMapMembershipRepository skillMapMembershipRepository;
    private final DependencyRepository dependencyRepository;

    public SkillService(SkillRepository skillRepository, SkillMapRepository skillMapRepository,
                        SkillMapMembershipRepository skillMapMembershipRepository,
                        DependencyRepository dependencyRepository) {
        this.skillRepository = skillRepository;
        this.skillMapRepository = skillMapRepository;
        this.skillMapMembershipRepository = skillMapMembershipRepository;
        this.dependencyRepository = dependencyRepository;
    }

    // #52 - POST
    public Skill createSkill(Long skillMapId, Skill newSkill, User user) {
        SkillMap skillMap = skillMapRepository.findById(skillMapId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SkillMap not found"));

        if (!user.getId().equals(skillMap.getOwnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the map owner can add skills");
        }
        if (newSkill.getName() == null || newSkill.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Skill name must not be empty");
        }
        if (newSkill.getLevel() == null || newSkill.getLevel() < 1 || newSkill.getLevel() > skillMap.getNumberOfLevels()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Skill level out of range for this map");
        }

        newSkill.setSkillMap(skillMap);
        newSkill.setIsLocked(false);
        return skillRepository.save(newSkill);
    }

    // #53 - DELETE
    public void deleteSkill(Long skillId, User user) {
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found"));

        if (!user.getId().equals(skill.getSkillMap().getOwnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the map owner can delete skills");
        }

        skillRepository.delete(skill);
    }

    // #55 - GET all skills of a map
    public List<Skill> getSkillsByMap(Long skillMapId, User user) {
        SkillMap skillMap = skillMapRepository.findById(skillMapId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SkillMap not found"));

        boolean isOwner = user.getId().equals(skillMap.getOwnerId());
        boolean isMember = skillMapMembershipRepository.existsBySkillMapIdAndUserId(skillMapId, user.getId());

        if (!isOwner && !isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return skillRepository.findBySkillMap(skillMap);
    }

    // Issue #55 - GET /skills/{skillId}
    public Skill getSkillById(Long skillId, User user) {
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found"));

        SkillMap skillMap = skill.getSkillMap();
        boolean isOwner = user.getId().equals(skillMap.getOwnerId());
        boolean isMember = skillMapMembershipRepository.existsBySkillMapIdAndUserId(skillMap.getId(), user.getId());

        if (!isOwner && !isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no access to the parent skillmap");
        }

        return skill;
    }

    // Issue #54 - PATCH /skills/{skillId}
    public Skill updateSkill(Long skillId, Skill updatedSkill, User user) {
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found"));

        if (!user.getId().equals(skill.getSkillMap().getOwnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can update skills");
        }

        if (updatedSkill.getLevel() != null) {
            int maxLevel = skill.getSkillMap().getNumberOfLevels();
            if (updatedSkill.getLevel() < 1 || updatedSkill.getLevel() > maxLevel) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Illegal level change: level must be between 1 and " + maxLevel);
            }

            int newLevel = updatedSkill.getLevel();

            // prerequisite skills (fromSkill) must be on a lower level
            dependencyRepository.findByToSkill(skill).forEach(dep -> {
                if (dep.getFromSkill().getLevel() >= newLevel) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "New level violates dependency: prerequisite skill '"
                        + dep.getFromSkill().getName() + "' must be on a lower level");
                }
            });

            // dependent skills (toSkill) must be on a higher level
            dependencyRepository.findByFromSkill(skill).forEach(dep -> {
                if (dep.getToSkill().getLevel() <= newLevel) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "New level violates dependency: dependent skill '"
                        + dep.getToSkill().getName() + "' must be on a higher level");
                }
            });

            skill.setLevel(newLevel);
        }

        if (updatedSkill.getName() != null) skill.setName(updatedSkill.getName());
        if (updatedSkill.getDescription() != null) skill.setDescription(updatedSkill.getDescription());
        if (updatedSkill.getResources() != null) skill.setResources(updatedSkill.getResources());
        if (updatedSkill.getDifficulty() != null) skill.setDifficulty(updatedSkill.getDifficulty());
        if (updatedSkill.getPositionX() != null) skill.setPositionX(updatedSkill.getPositionX());

        return skillRepository.save(skill);
    }

    // S8 - GET /skillmaps/{skillMapId}/skills/{skillId}
    public Skill getSkillByIdAndMap(Long skillMapId, Long skillId, User user) {
        Skill skill = getSkillById(skillId, user); // reuse existing auth check

        if (!skill.getSkillMap().getId().equals(skillMapId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill does not belong to this SkillMap");
        }

        return skill;
    }
}
