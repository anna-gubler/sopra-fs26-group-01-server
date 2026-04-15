package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.LiveQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LiveQuestionRepository extends JpaRepository<LiveQuestion, Long> {

    List<LiveQuestion> findBySessionId(Long sessionId);

    void deleteBySessionId(Long sessionId);
}