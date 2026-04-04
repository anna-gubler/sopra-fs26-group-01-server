package ch.uzh.ifi.hase.soprafs26.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.*;

@Repository
public interface DependencyRepository extends JpaRepository<Dependency, Long> {
    List<Dependency> findByFromSkill(Skill fromSkill);
    List<Dependency> findByToSkill(Skill toSkill);
    // Comment: used for if a skill is deleted
    List<Dependency> findByFromSkillOrToSkill(Skill fromSkill, Skill toSkill);
}