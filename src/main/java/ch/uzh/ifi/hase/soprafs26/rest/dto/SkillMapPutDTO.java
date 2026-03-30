package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class SkillMapPutDTO {

    private String title;
    private String description;
    private Boolean isPublic;
    //Comment from Anna: 
    //if we want to make numberOfLevels immutable, remove it here
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
