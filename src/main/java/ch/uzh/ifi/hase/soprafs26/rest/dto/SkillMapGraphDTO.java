package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class SkillMapGraphDTO {
    private Long skillMapId;
    private String title;
    private List<SkillGetDTO> skills = List.of();
    private List<DependencyGetDTO> dependencies = List.of();
    // TODO: replace with List<StudentProgressDTO> once StudentProgress entity is implemented
    private List<?> progress = List.of();

    public Long getSkillMapId() { return skillMapId; }
    public void setSkillMapId(Long skillMapId) { this.skillMapId = skillMapId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<SkillGetDTO> getSkills() { return skills; }
    public void setSkills(List<SkillGetDTO> skills) { this.skills = skills; }
    public List<DependencyGetDTO> getDependencies() { return dependencies; }
    public void setDependencies(List<DependencyGetDTO> dependencies) { this.dependencies = dependencies; }
    public List<?> getProgress() { return progress; }
}
