package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDateTime;

import ch.uzh.ifi.hase.soprafs26.constant.SkillMapRole;

public class SkillMapMembershipGetDTO {
    private Long id;
    private Long userId;
    private Long skillMapId;
    private SkillMapRole role;
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
