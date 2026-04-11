package ch.uzh.ifi.hase.soprafs26.websocket.dto;

import java.time.LocalDateTime;

public class SessionStartedMessageDTO {
    private final String type = "SESSION_STARTED";
    private final long sessionId;
    private final LocalDateTime startedAt;

    public SessionStartedMessageDTO(long sessionId, LocalDateTime startedAt) {
        this.sessionId = sessionId;
        this.startedAt = startedAt;
    }

    public String getType()      { return type; }
    public long getSessionId()          { return sessionId; }
    public LocalDateTime getStartedAt() { return startedAt; }
}