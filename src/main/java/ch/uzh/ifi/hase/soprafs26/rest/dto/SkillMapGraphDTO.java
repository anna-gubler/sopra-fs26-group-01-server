package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class SkillMapGraphDTO {
    private Long skillMapId;
    private String title;
    // TODO: replace with List<SkillDTO> once Skill entity is implemented
    private List<?> skills = List.of();
    // TODO: replace with List<DependencyDTO> once Dependency entity is implemented
    private List<?> dependencies = List.of();
    // TODO: replace with List<StudentProgressDTO> once StudentProgress entity is implemented
    private List<?> progress = List.of();

    public Long getSkillMapId() { return skillMapId; }
    public void setSkillMapId(Long skillMapId) { this.skillMapId = skillMapId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<?> getSkills() { return skills; }
    public List<?> getDependencies() { return dependencies; }
    public List<?> getProgress() { return progress; }
}
