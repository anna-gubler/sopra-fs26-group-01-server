package ch.uzh.ifi.hase.soprafs26.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class StudentProgress {
    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long skillId;

    @Column(nullable = false)
    private Boolean isUnderstood;

    @CreationTimestamp
    private LocalDateTime isUnderstandingDate;

    @UpdateTimestamp
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