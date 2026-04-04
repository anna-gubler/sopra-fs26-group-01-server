package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class DependencyGetDTO {
    private Long id;
    private Long fromSkillId;
    private Long toSkillId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFromSkillId() { return fromSkillId; }
    public void setFromSkillId(Long fromSkillId) { this.fromSkillId = fromSkillId; }

    public Long getToSkillId() { return toSkillId; }
    public void setToSkillId(Long toSkillId) { this.toSkillId = toSkillId; }
}