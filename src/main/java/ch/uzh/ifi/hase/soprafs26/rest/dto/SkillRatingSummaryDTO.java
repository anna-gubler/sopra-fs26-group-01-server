package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class SkillRatingSummaryDTO {
    private Long skillId;
    private double averageRating;
    private int totalRatings;
    private Integer myRating;

    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public int getTotalRatings() { return totalRatings; }
    public void setTotalRatings(int totalRatings) { this.totalRatings = totalRatings; }

    public Integer getMyRating() { return myRating; }
    public void setMyRating(Integer myRating) { this.myRating = myRating; }
}
