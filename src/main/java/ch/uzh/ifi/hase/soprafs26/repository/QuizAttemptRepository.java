package ch.uzh.ifi.hase.soprafs26.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.QuizAttempt;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserId(Long userId);
    List<QuizAttempt> findByQuizId(Long quizId);
    Optional<QuizAttempt> findTopByUserIdAndQuizIdOrderByAttemptedAtDesc(Long userId, Long quizId);

    @Query("SELECT qa.quizId, COUNT(qa), AVG(qa.score) " +
       "FROM QuizAttempt qa " +
       "WHERE qa.quizId IN :quizIds AND qa.attemptedAt >= :startedAt " +
       "GROUP BY qa.quizId")
    List<Object[]> aggregateByQuizIdsAndStartedAt(
        @Param("quizIds") List<Long> quizIds,
        @Param("startedAt") LocalDateTime startedAt
    );
}
