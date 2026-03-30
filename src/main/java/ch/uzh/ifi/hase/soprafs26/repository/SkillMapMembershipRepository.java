package ch.uzh.ifi.hase.soprafs26.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.SkillMapMembership;

@Repository
public interface SkillMapMembershipRepository extends JpaRepository<SkillMapMembership, Long> {
    List<SkillMapMembership> findBySkillMapId(Long skillMapId);
    List<SkillMapMembership> findByUserId(Long userId);
    Optional<SkillMapMembership> findBySkillMapIdAndUserId(Long skillMapId, Long userId);
    boolean existsBySkillMapIdAndUserId(Long skillMapId, Long userId);
}
