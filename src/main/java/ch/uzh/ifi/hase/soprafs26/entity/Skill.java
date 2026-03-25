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

    private Float positionY;

    private String resources;

    private String difficulty;

    @Column(nullable = false)
    private Long mapId;

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
    public Float getPositionY() { return positionY; }
    public String getResources() { return resources; }
    public String getDifficulty() { return difficulty; }
    public Long getMapId() { return mapId; }
    public Boolean getIsLocked() { return isLocked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setLevel(Integer level) { this.level = level; }
    public void setPositionX(Float positionX) { this.positionX = positionX; }
    public void setPositionY(Float positionY) { this.positionY = positionY; }
    public void setResources(String resources) { this.resources = resources; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setMapId(Long mapId) { this.mapId = mapId; }
    public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}


