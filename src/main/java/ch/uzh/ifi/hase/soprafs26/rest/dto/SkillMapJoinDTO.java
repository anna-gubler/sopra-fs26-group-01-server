package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class SkillMapJoinDTO {
    private String inviteCode;
    //private Long skillMapId; //shouldn't be here because only Invite code is needed by front-end (comment by Anna)

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    // public Long getSkillMapId() { return skillMapId; }
    // public void setSkillMapId(Long skillMapId) { this.skillMapId = skillMapId; }
}
