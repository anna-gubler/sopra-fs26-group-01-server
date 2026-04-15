package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.UpvoteRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UpvoteRecordRepository extends JpaRepository<UpvoteRecord, Long> {

    boolean existsByQuestionIdAndUserId(Long questionId, Long userId);

    Optional<UpvoteRecord> findByQuestionIdAndUserId(Long questionId, Long userId);

    void deleteByQuestionId(Long questionId);
}