package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.SpeedFeedback;
import ch.uzh.ifi.hase.soprafs26.entity.CollaborationSession;
import ch.uzh.ifi.hase.soprafs26.repository.CollaborationSessionRepository;
import ch.uzh.ifi.hase.soprafs26.websocket.WebSocketBroadcastService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SpeedFeedbackService {

    private final ConcurrentHashMap<Long, ConcurrentHashMap<Long, SpeedFeedback>> speedVotes = new ConcurrentHashMap<>();

    private final WebSocketBroadcastService webSocketBroadcastService;
    private final CollaborationSessionRepository collaborationSessionRepository;

    public SpeedFeedbackService(CollaborationSessionRepository collaborationSessionRepository,
            WebSocketBroadcastService webSocketBroadcastService) {
        this.collaborationSessionRepository = collaborationSessionRepository;
        this.webSocketBroadcastService = webSocketBroadcastService;
    }

    public void submitFeedback(Long sessionId, Long userId, SpeedFeedback feedback) {
        CollaborationSession session = collaborationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
        if (!session.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Session is not active");
        }
        speedVotes
                .computeIfAbsent(sessionId, id -> new ConcurrentHashMap<>())
                .put(userId, feedback);
        broadcastSpeedUpdate(sessionId);
    }

    public void clearSession(Long sessionId) {
        speedVotes.remove(sessionId);
    }

    private void broadcastSpeedUpdate(Long sessionId) {
        Long skillMapId = collaborationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"))
                .getSkillMapId();

        Map<Long, SpeedFeedback> votes = speedVotes.getOrDefault(sessionId, new ConcurrentHashMap<>());

        int tooFast = (int) votes.values().stream().filter(v -> v == SpeedFeedback.TOO_FAST).count();
        int tooSlow = (int) votes.values().stream().filter(v -> v == SpeedFeedback.TOO_SLOW).count();
        int totalResponses = votes.size();

        webSocketBroadcastService.broadcastSpeedUpdated(skillMapId, tooFast, tooSlow, totalResponses);
    }
}