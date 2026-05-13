package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class StudentProgressDTO {
    private Long skillId;
    private Long userId;
    private Boolean isUnderstood;

    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Boolean getIsUnderstood() { return isUnderstood; }
    public void setIsUnderstood(Boolean isUnderstood) { this.isUnderstood = isUnderstood; }
}
