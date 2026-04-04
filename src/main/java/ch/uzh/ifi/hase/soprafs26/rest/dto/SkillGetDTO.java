package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class SkillGetDTO {
    private Long id;
    private String name;
    private String description;
    private String resources;
    private String difficulty;
    private Integer level;
    private Float positionX;
    private Boolean isLocked;
    private Long skillMapId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getResources() { return resources; }
    public void setResources(String resources) { this.resources = resources; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public Float getPositionX() { return positionX; }
    public void setPositionX(Float positionX) { this.positionX = positionX; }

    public Boolean getIsLocked() { return isLocked; }
    public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }

    public Long getSkillMapId() { return skillMapId; }
    public void setSkillMapId(Long skillMapId) { this.skillMapId = skillMapId; }
}
