package ch.uzh.ifi.hase.soprafs26.websocket.dto;

import java.time.LocalDateTime;

public class SessionEndedMessageDTO {
    private final String type = "SESSION_ENDED";
    private final long sessionId;
    private final LocalDateTime endedAt;

    public SessionEndedMessageDTO(long sessionId, LocalDateTime endedAt) {
        this.sessionId = sessionId;
        this.endedAt = endedAt;
    }

    public String getType()     { return type; }
    public long getSessionId()         { return sessionId; }
    public LocalDateTime getEndedAt()  { return endedAt; }
}