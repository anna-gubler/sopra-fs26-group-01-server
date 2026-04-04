package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class DependencyPostDTO {
    private Long fromSkillId;
    private Long toSkillId;

    public Long getFromSkillId() { return fromSkillId; }
    public void setFromSkillId(Long fromSkillId) { this.fromSkillId = fromSkillId; }

    public Long getToSkillId() { return toSkillId; }
    public void setToSkillId(Long toSkillId) { this.toSkillId = toSkillId; }
}