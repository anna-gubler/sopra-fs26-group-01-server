package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.CollaborationSession;
import ch.uzh.ifi.hase.soprafs26.repository.CollaborationSessionRepository;
import ch.uzh.ifi.hase.soprafs26.websocket.WebSocketBroadcastService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class CollaborationSessionService {

    private final CollaborationSessionRepository sessionRepository;
    private final WebSocketBroadcastService broadcastService;

    public CollaborationSessionService(CollaborationSessionRepository sessionRepository,
                                       WebSocketBroadcastService broadcastService) {
        this.sessionRepository = sessionRepository;
        this.broadcastService = broadcastService;
    }

    public CollaborationSession startSession(Long skillMapId) {
        // enforce one active session per map
        if (sessionRepository.existsBySkillMapIdAndIsActiveTrue(skillMapId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A session is already active for this skill map");
        }

        CollaborationSession session = new CollaborationSession();
        session.setSkillMapId(skillMapId);
        session.setStartedAt(LocalDateTime.now());
        session.setActive(true);
        session = sessionRepository.save(session);

        // broadcast after save so session ID is available
        broadcastService.broadcastSessionStarted(skillMapId, session.getId(), session.getStartedAt());

        return session;
    }

    public void endSession(Long skillMapId) {
        CollaborationSession session = sessionRepository
                .findBySkillMapIdAndIsActiveTrue(skillMapId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No active session found for this skill map"));

        session.setActive(false);
        session.setEndedAt(LocalDateTime.now());
        session = sessionRepository.save(session);

        broadcastService.broadcastSessionEnded(skillMapId, session.getId(), session.getEndedAt());
    }

    public CollaborationSession getActiveSession(Long skillMapId) {
        return sessionRepository
                .findBySkillMapIdAndIsActiveTrue(skillMapId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No active session"));
    }
}