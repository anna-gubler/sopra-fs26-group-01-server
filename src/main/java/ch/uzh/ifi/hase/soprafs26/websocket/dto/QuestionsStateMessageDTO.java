package ch.uzh.ifi.hase.soprafs26.websocket.dto;

import ch.uzh.ifi.hase.soprafs26.entity.LiveQuestion;

import java.util.List;

public class QuestionsStateMessageDTO {

    private final String type = "QUESTIONS_STATE";
    private final long sessionId;
    private final List<LiveQuestion> questions;

    public QuestionsStateMessageDTO(long sessionId, List<LiveQuestion> questions) {
        this.sessionId = sessionId;
        this.questions = questions;
    }

    public String getType() { return type; }
    public long getSessionId() { return sessionId; }
    public List<LiveQuestion> getQuestions() { return questions; }
}