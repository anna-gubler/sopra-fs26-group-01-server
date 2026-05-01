package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class SkillMapExportDTO {
    private String title;
    private List<SkillExportDTO> skills = List.of();
    private List<DependencyExportDTO> dependencies = List.of();
    private String description;
    private Integer numberOfLevels;
    private Boolean isPublic;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<SkillExportDTO> getSkills() { return skills; }
    public void setSkills(List<SkillExportDTO> skills) { this.skills = skills; }
    public List<DependencyExportDTO> getDependencies() { return dependencies; }
    public void setDependencies(List<DependencyExportDTO> dependencies) { this.dependencies = dependencies; }
    public Integer getNumberOfLevels() { return numberOfLevels; }
    public void setNumberOfLevels(Integer numberOfLevels) { this.numberOfLevels = numberOfLevels; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
}
