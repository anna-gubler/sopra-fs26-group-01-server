package ch.uzh.ifi.hase.soprafs26.websocket.dto;

public class QuestionUpdatedMessageDTO {
    private final String type = "QUESTION_UPDATED";
    private final long questionId;
    private final long sessionId;
    private final String text;
    private final int upvoteCount;
    private final boolean isAddressed;

    public QuestionUpdatedMessageDTO(long questionId, long sessionId, String text, int upvoteCount, boolean isAddressed) {
        this.questionId = questionId;
        this.sessionId = sessionId;
        this.text = text;
        this.upvoteCount = upvoteCount;
        this.isAddressed = isAddressed;
    }

    public String getType()        { return type; }
    public long getQuestionId()    { return questionId; }
    public long getSessionId()     { return sessionId; }
    public String getText()        { return text; }
    public int getUpvoteCount()    { return upvoteCount; }
    public boolean isAddressed()   { return isAddressed; }
}