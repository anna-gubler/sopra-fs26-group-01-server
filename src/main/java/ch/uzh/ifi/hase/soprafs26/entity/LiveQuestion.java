package ch.uzh.ifi.hase.soprafs26.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "live_question")
public class LiveQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sessionId;

    private Long skillId;

    private String skillName;

    private String text;

    private Integer upvoteCount;

    private Boolean isAddressed;

    @Column(nullable = false)
    private LocalDateTime postedAt;

    private LocalDateTime updatedAt;

    //getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Integer getUpvoteCount() { return upvoteCount; }
    public void setUpvoteCount(Integer upvoteCount) { this.upvoteCount = upvoteCount; }

    public Boolean getIsAddressed() { return isAddressed; }
    public void setIsAddressed(Boolean isAddressed) { this.isAddressed = isAddressed; }

    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
