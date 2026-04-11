package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "collaboration_session")
public class CollaborationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long skillMapId;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @Column(nullable = false)
    private boolean isActive;

    // getters and setters
    public Long getId()                        { return id; }
    public Long getSkillMapId()                { return skillMapId; }
    public void setSkillMapId(Long skillMapId) { this.skillMapId = skillMapId; }
    public LocalDateTime getStartedAt()        { return startedAt; }
    public void setStartedAt(LocalDateTime t)  { this.startedAt = t; }
    public LocalDateTime getEndedAt()          { return endedAt; }
    public void setEndedAt(LocalDateTime t)    { this.endedAt = t; }
    public boolean isActive()                  { return isActive; }
    public void setActive(boolean active)      { this.isActive = active; }
}