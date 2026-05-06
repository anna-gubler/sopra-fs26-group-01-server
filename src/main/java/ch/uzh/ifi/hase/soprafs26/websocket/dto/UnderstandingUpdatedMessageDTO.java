package ch.uzh.ifi.hase.soprafs26.websocket.dto;

public class UnderstandingUpdatedMessageDTO {
    private final String type = "UNDERSTANDING_UPDATED";
    private final double averageRating;
    private final int totalResponses;

    public UnderstandingUpdatedMessageDTO(double averageRating, int totalResponses) {
        this.averageRating = averageRating;
        this.totalResponses = totalResponses;
    }

    public String getType()         { return type; }
    public double getAverageRating() { return averageRating; }
    public int getTotalResponses()   { return totalResponses; }
}
