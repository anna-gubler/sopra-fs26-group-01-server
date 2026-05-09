package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.CollaborationSession;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.repository.CollaborationSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapRepository;
import ch.uzh.ifi.hase.soprafs26.websocket.WebSocketBroadcastService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CurrentUnderstandingService {

    private final ConcurrentHashMap<Long, ConcurrentHashMap<Long, Integer>> understandingVotes = new ConcurrentHashMap<>();

    private final WebSocketBroadcastService webSocketBroadcastService;
    private final CollaborationSessionRepository collaborationSessionRepository;
    private final SkillMapRepository skillMapRepository;

    public CurrentUnderstandingService(CollaborationSessionRepository collaborationSessionRepository,
            SkillMapRepository skillMapRepository,
            WebSocketBroadcastService webSocketBroadcastService) {
        this.collaborationSessionRepository = collaborationSessionRepository;
        this.skillMapRepository = skillMapRepository;
        this.webSocketBroadcastService = webSocketBroadcastService;
    }

    public void requestFeedback(Long sessionId, Long userId) {
        CollaborationSession session = collaborationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
        if (!session.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Session is not active");
        }
        SkillMap skillMap = skillMapRepository.findById(session.getSkillMapId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill map not found"));
        if (!skillMap.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can request understanding");
        }
        understandingVotes.put(sessionId, new ConcurrentHashMap<>());
        webSocketBroadcastService.broadcastUnderstandingRequested(session.getSkillMapId());
    }

    public void submitRating(Long sessionId, Long userId, Integer rating) {
        CollaborationSession session = collaborationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
        if (!session.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Session is not active");
        }
        understandingVotes
                .computeIfAbsent(sessionId, id -> new ConcurrentHashMap<>())
                .put(userId, rating);
        broadcastUnderstandingUpdate(sessionId, session.getSkillMapId());
    }

    public void clearSession(Long sessionId) {
        understandingVotes.remove(sessionId);
    }

    private void broadcastUnderstandingUpdate(Long sessionId, Long skillMapId) {
        Map<Long, Integer> votes = understandingVotes.getOrDefault(sessionId, new ConcurrentHashMap<>());
        int totalResponses = votes.size();
        double averageRating = totalResponses == 0 ? 0.0
                : votes.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);
        webSocketBroadcastService.broadcastUnderstandingUpdated(skillMapId, averageRating, totalResponses);
    }
}
