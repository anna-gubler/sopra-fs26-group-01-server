package ch.uzh.ifi.hase.soprafs26.websocket.dto;

public class RatingUpdatedMessageDTO {
    private final String type = "RATING_UPDATED";
    private final long skillId;
    private final double averageRating;

    public RatingUpdatedMessageDTO(long skillId, double averageRating) {
        this.skillId = skillId;
        this.averageRating = averageRating;
    }

    public String getType() { return type; }
    public long getSkillId() { return skillId; }
    public double getAverageRating() { return averageRating; }
}