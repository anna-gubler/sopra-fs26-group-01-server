package ch.uzh.ifi.hase.soprafs26.websocket.dto;

import java.time.LocalDateTime;

import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;

public class SessionEndedMessageDTO {
    private final SessionStatus type = SessionStatus.ENDED;
    private final long sessionId;
    private final LocalDateTime endedAt;

    public SessionEndedMessageDTO(long sessionId, LocalDateTime endedAt) {
        this.sessionId = sessionId;
        this.endedAt = endedAt;
    }

    public SessionStatus getType()     { return type; }
    public long getSessionId()         { return sessionId; }
    public LocalDateTime getEndedAt()  { return endedAt; }
}