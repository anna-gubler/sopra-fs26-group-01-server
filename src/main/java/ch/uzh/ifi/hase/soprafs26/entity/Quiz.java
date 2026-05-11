package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "quiz")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long skillId;

    @Column(nullable = false)
    private Boolean isActive = false;

    private Integer cooldownHours;
    private Integer passMark;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void setId(Long id) { this.id = id; }
    public Long getId() { return id; }

    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getCooldownHours() { return cooldownHours; }
    public void setCooldownHours(Integer cooldownHours) { this.cooldownHours = cooldownHours; }

    public Integer getPassMark() { return passMark; }
    public void setPassMark(Integer passMark) { this.passMark = passMark; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

}