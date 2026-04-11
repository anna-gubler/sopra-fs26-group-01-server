package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.CollaborationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollaborationSessionRepository extends JpaRepository<CollaborationSession, Long> {

    Optional<CollaborationSession> findBySkillMapIdAndIsActiveTrue(Long skillMapId);

    boolean existsBySkillMapIdAndIsActiveTrue(Long skillMapId);
}