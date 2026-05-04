package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDateTime;

public class QuizGetDTO {
    private Long id;
    private Long skillId;
    private Boolean isActive;
    private Integer cooldownHours;
    private Integer passMark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getCooldownHours() { return cooldownHours; }
    public void setCooldownHours(Integer cooldownHours) { this.cooldownHours = cooldownHours; }

    public Integer getPassMark() { return passMark; }
    public void setPassMark(Integer passMark) { this.passMark = passMark; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
