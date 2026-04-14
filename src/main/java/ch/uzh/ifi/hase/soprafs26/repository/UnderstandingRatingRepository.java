package ch.uzh.ifi.hase.soprafs26.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.UnderstandingRating;

@Repository
public interface UnderstandingRatingRepository extends JpaRepository<UnderstandingRating, Long>{
    Optional<UnderstandingRating> findBySessionIdAndSkillIdAndUserId(Long sessionId, Long skillId, Long userId);
    List<UnderstandingRating> findBySessionIdAndSkillId(Long sessionId, Long skillId);
    List<UnderstandingRating> findBySessionId(Long sessionId);
}
