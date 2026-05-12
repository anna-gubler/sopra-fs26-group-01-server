package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class DashboardQuizSummaryDTO {

    private Long quizId;
    private Long skillId;
    private Integer totalAttempts;
    private Double averageScore;

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }

    public Integer getTotalAttempts() { return totalAttempts; }
    public void setTotalAttempts(Integer totalAttempts) { this.totalAttempts = totalAttempts; }

    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
}