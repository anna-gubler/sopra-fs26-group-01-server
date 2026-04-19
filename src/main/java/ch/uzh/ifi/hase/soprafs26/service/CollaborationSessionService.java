package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.CollaborationSession;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.CollaborationSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapMembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapRepository;
import ch.uzh.ifi.hase.soprafs26.websocket.WebSocketBroadcastService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class CollaborationSessionService {

    private final SkillMapRepository skillMapRepository;
    private final CollaborationSessionRepository sessionRepository;
    private final WebSocketBroadcastService broadcastService;
    private final SkillMapMembershipRepository membershipRepository;
    private final SpeedFeedbackService speedFeedbackService;

    public CollaborationSessionService(CollaborationSessionRepository sessionRepository,
            WebSocketBroadcastService broadcastService, SkillMapRepository skillMapRepository,
            SkillMapMembershipRepository membershipRepository,
            SpeedFeedbackService speedFeedbackService) {
        this.sessionRepository = sessionRepository;
        this.broadcastService = broadcastService;
        this.skillMapRepository = skillMapRepository;
        this.membershipRepository = membershipRepository;
        this.speedFeedbackService = speedFeedbackService;
    }

    public CollaborationSession startSession(Long skillMapId, User user) {
        SkillMap skillMap = skillMapRepository.findById(skillMapId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill map not found"));

        if (!skillMap.getOwnerId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can start a session");
        }

        if (sessionRepository.existsBySkillMapIdAndIsActiveTrue(skillMapId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A session is already active");
        }

        CollaborationSession session = new CollaborationSession();
        session.setSkillMapId(skillMapId);
        session.setStartedAt(LocalDateTime.now());
        session.setActive(true);
        session = sessionRepository.save(session);

        broadcastService.broadcastSessionStarted(skillMapId, session.getId(), session.getStartedAt());
        return session;
    }

    public void endSession(Long skillMapId, User user) {
        SkillMap skillMap = skillMapRepository.findById(skillMapId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill map not found"));

        if (!skillMap.getOwnerId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can end a session");
        }

        CollaborationSession session = sessionRepository.findBySkillMapIdAndIsActiveTrue(skillMapId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active session found"));

        session.setActive(false);
        session.setEndedAt(LocalDateTime.now());
        session = sessionRepository.save(session);
        speedFeedbackService.clearSession(session.getId());

        //design decision to NOT delete the questions after session end
        // liveQuestionService.deleteAllQuestionsForSession(session.getId()); 

        broadcastService.broadcastSessionEnded(skillMapId, session.getId(), session.getEndedAt());
    }

    public CollaborationSession getActiveSession(Long skillMapId, User user) {
        if (!membershipRepository.existsBySkillMapIdAndUserId(skillMapId, user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a member of this skill map");
        }
        return sessionRepository.findBySkillMapIdAndIsActiveTrue(skillMapId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active session found"));
    }

    public CollaborationSession getSessionById(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
    }
}