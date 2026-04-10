package ch.uzh.ifi.hase.soprafs26.websocket.dto;

import java.time.LocalDateTime;

import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;

public class SessionStartedMessageDTO {
    private final SessionStatus type = SessionStatus.STARTED;
    private final long sessionId;
    private final LocalDateTime startedAt;

    public SessionStartedMessageDTO(long sessionId, LocalDateTime startedAt) {
        this.sessionId = sessionId;
        this.startedAt = startedAt;
    }

    public SessionStatus getType()      { return type; }
    public long getSessionId()          { return sessionId; }
    public LocalDateTime getStartedAt() { return startedAt; }
}