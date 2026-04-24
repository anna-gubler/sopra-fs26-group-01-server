package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDateTime;

public class StudentProgressGetDTO {
    private Long id;
    private Long userId;
    private Long skillId;
    private Boolean isUnderstood;
    private LocalDateTime isUnderstandingDate;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }

    public Boolean getIsUnderstood() { return isUnderstood; }
    public void setIsUnderstood(Boolean isUnderstood) { this.isUnderstood = isUnderstood; }

    public LocalDateTime getIsUnderstandingDate() { return isUnderstandingDate; }
    public void setIsUnderstandingDate(LocalDateTime isUnderstandingDate) { this.isUnderstandingDate = isUnderstandingDate; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
