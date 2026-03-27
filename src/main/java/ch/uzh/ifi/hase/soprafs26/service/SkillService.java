package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.SkillRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapMembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapRepository;
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
    private final UserRepository userRepository;
    private final SkillMapMembershipRepository skillMapMembershipRepository;

    public SkillService(SkillRepository skillRepository, SkillMapRepository skillMapRepository) {
        this.skillRepository = skillRepository;
        this.skillMapRepository = skillMapRepository;
        this.userRepository = null;
        this.skillMapMembershipRepository = null;
    }

    // #52 - POST
    public Skill createSkill(Long skillMapId, Skill newSkill, String token) {
        SkillMap skillMap = skillMapRepository.findById(skillMapId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SkillMap not found"));

        User owner = userRepository.findById(skillMap.getOwnerId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found"));

        if (!owner.getToken().equals(token)) {
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
    public void deleteSkill(Long skillId, String token) {
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found"));

        Long ownerId = skill.getSkillMap().getOwnerId();
        User owner = userRepository.findById(ownerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found"));

        if (!owner.getToken().equals(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the map owner can delete skills");
        }

        skillRepository.delete(skill);
    }

    // #55 - GET all skills of a map
    public List<Skill> getSkillsByMap(Long skillMapId, String token) {
        SkillMap skillMap = skillMapRepository.findById(skillMapId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SkillMap not found"));

        User owner = userRepository.findById(skillMap.getOwnerId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found"));
        boolean isMember = skillMapMembershipRepository
            .findBySkillMapId(skillMapId)
            .stream()
            .anyMatch(m -> userRepository.findById(m.getUserId())
                .map(u -> u.getToken().equals(token))
                .orElse(false));
        if (!owner.getToken().equals(token)
                && !isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return skillRepository.findBySkillMap(skillMap);
    }

    // Issue #55 - GET /skills/{skillId}
    public Skill getSkillById(Long skillId, String token) {
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found"));

        SkillMap skillMap = skill.getSkillMap();
        
        User owner = userRepository.findById(skillMap.getOwnerId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found"));
        boolean isOwner = owner.getToken().equals(token);
        
        boolean isMember = skillMapMembershipRepository
            .findBySkillMapId(skillMap.getId())  // skillMap.getId() statt skillMapId (existiert nicht hier)
            .stream()
            .anyMatch(m -> userRepository.findById(m.getUserId())  // m hat kein getToken(), lookup nötig
                .map(u -> u.getToken().equals(token))
                .orElse(false));
                
        if (!isOwner && !isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no access to the parent skillmap");
        }

        return skill;
    }

    // Issue #54 - PATCH /skills/{skillId}
    public Skill updateSkill(Long skillId, Skill updatedSkill, String token) {
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found"));

        User owner = userRepository.findById(skill.getSkillMap().getOwnerId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found"));

        if (!owner.getToken().equals(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can update skills");
        }

        if (updatedSkill.getLevel() != null) {
            int maxLevel = skill.getSkillMap().getNumberOfLevels();
            if (updatedSkill.getLevel() < 1 || updatedSkill.getLevel() > maxLevel) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Illegal level change: level must be between 1 and " + maxLevel);
            }
            // TODO (Issue #54 / S5 dependencies): validate that the new level does not violate
            // dependency constraints — i.e. a skill cannot be moved to the same or lower level
            // than any of its prerequisite skills. Implement once DependencyService is available.
            // comment by Anna

            skill.setLevel(updatedSkill.getLevel());
        }

        if (updatedSkill.getName() != null) skill.setName(updatedSkill.getName());
        if (updatedSkill.getDescription() != null) skill.setDescription(updatedSkill.getDescription());
        if (updatedSkill.getResources() != null) skill.setResources(updatedSkill.getResources());
        if (updatedSkill.getDifficulty() != null) skill.setDifficulty(updatedSkill.getDifficulty());
        if (updatedSkill.getPositionX() != null) skill.setPositionX(updatedSkill.getPositionX());
        if (updatedSkill.getPositionY() != null) skill.setPositionY(updatedSkill.getPositionY());

        return skillRepository.save(skill);
    }
}