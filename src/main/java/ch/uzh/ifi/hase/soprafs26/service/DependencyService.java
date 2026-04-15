package ch.uzh.ifi.hase.soprafs26.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;


@Service
@Transactional
public class DependencyService {

    private final SkillMapRepository skillMapRepository;
    private final SkillMapMembershipRepository skillMapMembershipRepository;
    private final SkillRepository skillRepository;
    private final DependencyRepository dependencyRepository;

    public DependencyService(
            @Qualifier("skillMapRepository") SkillMapRepository skillMapRepository,
            @Qualifier("skillRepository") SkillRepository skillRepository,
            @Qualifier("skillMapMembershipRepository") SkillMapMembershipRepository skillMapMembershipRepository,
            @Qualifier("dependencyRepository") DependencyRepository dependencyRepository) {
        this.skillMapRepository = skillMapRepository;
        this.skillMapMembershipRepository = skillMapMembershipRepository;
        this.skillRepository = skillRepository;
        this.dependencyRepository = dependencyRepository;
    }

    // 401 Get a list of all dependencies of one skillmap
    public List<Dependency> getDependenciesByMap(Long skillMapId, User user) {
        SkillMap skillMap = skillMapRepository.findById(skillMapId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SkillMap not found"));

        boolean isOwner = user.getId().equals(skillMap.getOwnerId());
        boolean isMember = skillMapMembershipRepository.existsBySkillMapIdAndUserId(skillMapId, user.getId());

        if (!isOwner && !isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no access to this skillmap");
        }

        List<Long> skillIds = skillRepository.findBySkillMap(skillMap)
            .stream()
            .map(Skill::getId)
            .collect(Collectors.toList());

        return dependencyRepository.findByFromSkill_IdIn(skillIds);
    }

    public List<Dependency> getFromDependenciesBySkill(Long skillId, User user) {
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found"));

        boolean isOwner = user.getId().equals(skill.getSkillMap().getOwnerId());
        boolean isMember = skillMapMembershipRepository.existsBySkillMapIdAndUserId(skill.getSkillMap().getId(), user.getId());

        if (!isOwner && !isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no access to this skillmap");
        }

        return dependencyRepository.findByFromSkill(skill);
    }

    public List<Dependency> getToDependenciesBySkill(Long skillId, User user) {
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found"));

        boolean isOwner = user.getId().equals(skill.getSkillMap().getOwnerId());
        boolean isMember = skillMapMembershipRepository.existsBySkillMapIdAndUserId(skill.getSkillMap().getId(), user.getId());

        if (!isOwner && !isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no access to this skillmap");
        }

        return dependencyRepository.findByToSkill(skill);
    }

    public Dependency getById(Long dependencyId, User user) {
        Dependency dependency = dependencyRepository.findById(dependencyId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dependency not found"));

        Long skillMapId = dependency.getFromSkill().getSkillMap().getId();
        boolean isOwner = user.getId().equals(dependency.getFromSkill().getSkillMap().getOwnerId());
        boolean isMember = skillMapMembershipRepository.existsBySkillMapIdAndUserId(skillMapId, user.getId());

        if (!isOwner && !isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no access to this skillmap");
        }

        return dependency;
    }

    // 402 Create a new dependency
    public Dependency createDependency(Long skillMapId, Long fromSkillId, Long toSkillId, User user) {
        SkillMap skillMap = skillMapRepository.findById(skillMapId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SkillMap not found"));

        // 402.3
        if (!user.getId().equals(skillMap.getOwnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the map owner can add dependencies");
        }

        // 402.4
        Skill fromSkill = skillRepository.findById(fromSkillId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "From-Skill not found"));
        Skill toSkill = skillRepository.findById(toSkillId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "To-Skill not found"));

        // 402.2 level change dependency
        if (fromSkill.getLevel() >= toSkill.getLevel()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target skill must be on a higher level");
        }

        // 402.2 recursive dependency
        if (fromSkill.getId().equals(toSkill.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create recursive dependency");
        }

        // 402.5 Dependency already exists
        boolean alreadyExists = dependencyRepository.findByFromSkill(fromSkill)
            .stream()
            .anyMatch(d -> d.getToSkill().getId().equals(toSkillId));
        if (alreadyExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Dependency already exists");
        }

        Dependency newDependency = new Dependency();
        newDependency.setFromSkill(fromSkill);
        newDependency.setToSkill(toSkill);
        return dependencyRepository.save(newDependency);
    }

    // 403 Delete a dependency
    public void deleteDependency(Long dependencyId, User user) {
        Dependency dependency = dependencyRepository.findById(dependencyId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dependency not found"));

        if (!user.getId().equals(dependency.getFromSkill().getSkillMap().getOwnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the map owner can delete skills");
        }

        dependencyRepository.delete(dependency);
    }
}
