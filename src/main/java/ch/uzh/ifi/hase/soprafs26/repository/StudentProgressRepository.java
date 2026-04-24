package ch.uzh.ifi.hase.soprafs26.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import ch.uzh.ifi.hase.soprafs26.entity.*;

public interface StudentProgressRepository extends JpaRepository<StudentProgress, Long> {
    Optional<StudentProgress> findByUserIdAndSkillId(Long userId, Long skillId);
    List<StudentProgress> findByUserId(Long userId);
}
