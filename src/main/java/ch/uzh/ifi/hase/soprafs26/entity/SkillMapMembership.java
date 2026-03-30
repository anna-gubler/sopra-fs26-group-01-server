package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import ch.uzh.ifi.hase.soprafs26.constant.SkillMapRole;

@Entity
@Table(name = "skillmap_membership")
public class SkillMapMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long skillMapId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillMapRole role;

    @CreationTimestamp
    private LocalDateTime joinedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getSkillMapId() { return skillMapId; }
    public void setSkillMapId(Long skillMapId) { this.skillMapId = skillMapId; }
    public SkillMapRole getRole() { return role; }
    public void setRole(SkillMapRole role) { this.role = role; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
