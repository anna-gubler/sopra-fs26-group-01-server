package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.websocket.WebSocketBroadcastService;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UnderstandingRatingService {

    private final UnderstandingRatingRepository understandingRatingRepository;
    private final CollaborationSessionRepository collaborationSessionRepository;
    private final SkillMapRepository skillMapRepository;
    private final SkillMapMembershipRepository skillMapMembershipRepository;
    private final SkillRepository skillRepository;
    private final WebSocketBroadcastService broadcastService;

    public UnderstandingRatingService(
            @Qualifier("understandingRatingRepository") UnderstandingRatingRepository understandingRatingRepository,
            @Qualifier("collaborationSessionRepository") CollaborationSessionRepository collaborationSessionRepository,
            @Qualifier("skillMapRepository") SkillMapRepository skillMapRepository,
            @Qualifier("skillMapMembershipRepository") SkillMapMembershipRepository skillMapMembershipRepository,
            @Qualifier("skillRepository") SkillRepository skillRepository,
            WebSocketBroadcastService broadcastService) {
        this.understandingRatingRepository = understandingRatingRepository;
        this.collaborationSessionRepository = collaborationSessionRepository;
        this.skillMapRepository = skillMapRepository;
        this.skillMapMembershipRepository = skillMapMembershipRepository;
        this.skillRepository = skillRepository;
        this.broadcastService = broadcastService;
    }

    // 701 - PUT /sessions/{sessionId}/skills/{skillId}/rating
    public UnderstandingRating submitRating(Long sessionId, Long skillId, User user, Integer rating) {
        if (rating < 0 || rating > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 0 and 100");
        }

        CollaborationSession session = collaborationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session is not existant"));

        if (!session.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Session is not active");
        }

        skillRepository.findById(skillId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found"));

        if (!skillMapMembershipRepository.existsBySkillMapIdAndUserId(session.getSkillMapId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a member of this skill map");
        }

        UnderstandingRating ratingEntry = understandingRatingRepository
                .findBySessionIdAndSkillIdAndUserId(sessionId, skillId, user.getId())
                .orElse(new UnderstandingRating());

        boolean isNew = ratingEntry.getId() == null;
        ratingEntry.setSessionId(sessionId);
        ratingEntry.setSkillId(skillId);
        ratingEntry.setUserId(user.getId());
        ratingEntry.setRating(rating);

        if (isNew) {
            ratingEntry.setSubmittedAt(LocalDateTime.now());
        } else {
            ratingEntry.setUpdatedAt(LocalDateTime.now());
        }

        ratingEntry = understandingRatingRepository.save(ratingEntry);

        double average = computeAverage(sessionId, skillId);
        broadcastService.broadcastRatingUpdate(sessionId, skillId, average);

        return ratingEntry;
    }

    // 702 - GET /sessions/{sessionId}/skills/{skillId}/ratings (owner only)
    public List<UnderstandingRating> getRatingsBySkill(Long sessionId, Long skillId, User user) {
        CollaborationSession session = collaborationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        skillRepository.findById(skillId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found"));

        SkillMap skillMap = skillMapRepository.findById(session.getSkillMapId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill map not found"));

        if (!skillMap.getOwnerId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can inspect ratings");
        }

        return understandingRatingRepository.findBySessionIdAndSkillId(sessionId, skillId);
    }

    // 703 - GET /sessions/{sessionId}/ratings (owner only)
    public List<UnderstandingRating> getRatingsBySession(Long sessionId, User user) {
        CollaborationSession session = collaborationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        SkillMap skillMap = skillMapRepository.findById(session.getSkillMapId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill map not found"));

        if (!skillMap.getOwnerId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can inspect ratings");
        }

        return understandingRatingRepository.findBySessionId(sessionId);
    }

    private double computeAverage(Long sessionId, Long skillId) {
        return understandingRatingRepository.findBySessionIdAndSkillId(sessionId, skillId)
                .stream()
                .mapToInt(UnderstandingRating::getRating)
                .average()
                .orElse(0.0);
    }
}