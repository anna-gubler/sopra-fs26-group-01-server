package ch.uzh.ifi.hase.soprafs26.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;


@Repository
public interface SkillMapRepository extends JpaRepository<SkillMap,Long> {
    List<SkillMap> findByOwnerId(Long ownerId);
    boolean existsByInviteCode(String inviteCode);
}
