package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class QuizPostDTO {
    private Boolean isActive;
    private Integer cooldownHours;
    private Integer passMark;

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getCooldownHours() { return cooldownHours; }
    public void setCooldownHours(Integer cooldownHours) { this.cooldownHours = cooldownHours; }

    public Integer getPassMark() { return passMark; }
    public void setPassMark(Integer passMark) { this.passMark = passMark; }
}
