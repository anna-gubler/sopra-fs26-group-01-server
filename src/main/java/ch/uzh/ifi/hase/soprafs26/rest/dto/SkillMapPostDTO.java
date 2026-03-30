package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class SkillMapPostDTO {
    private String title;
    private String description;
    private Boolean isPublic;
    private Integer numberOfLevels;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
    public Integer getNumberOfLevels() { return numberOfLevels; }
    public void setNumberOfLevels(Integer numberOfLevels) { this.numberOfLevels = numberOfLevels; }
}
