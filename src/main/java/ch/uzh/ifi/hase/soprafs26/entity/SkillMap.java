package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "skillmap")
public class SkillMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(nullable = false)
    private Boolean isPublic;

    @Column(unique = true)
    private String inviteCode;

    @Column(nullable = false)
    private Integer numberOfLevels;

    @Column(nullable = false)
    private Long ownerId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Boolean getIsPublic() { return isPublic; }
    public String getInviteCode() { return inviteCode; }
    public Integer getNumberOfLevels() { return numberOfLevels; }
    public Long getOwnerId() { return ownerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    public void setNumberOfLevels(Integer numberOfLevels) { this.numberOfLevels = numberOfLevels; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

}