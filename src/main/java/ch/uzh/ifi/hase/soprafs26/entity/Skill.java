package ch.uzh.ifi.hase.soprafs26.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.*;

@Entity
@Table(name = "skill")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer level;

    private Float positionX;

    private String resources;

    private String difficulty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_map_id", nullable = false)
    private SkillMap skillMap;

    private Boolean isLocked;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getLevel() { return level; }
    public Float getPositionX() { return positionX; }
    public String getResources() { return resources; }
    public String getDifficulty() { return difficulty; }
    public SkillMap getSkillMap() { return skillMap; }
    public Boolean getIsLocked() { return isLocked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setLevel(Integer level) { this.level = level; }
    public void setPositionX(Float positionX) { this.positionX = positionX; }
    public void setResources(String resources) { this.resources = resources; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setSkillMap(SkillMap skillMap) { this.skillMap = skillMap; }
    public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}


