package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class QuizExportDTO {
    private Boolean isActive;
    private Integer cooldownHours;
    private Integer passMark;
    private List<QuizQuestionExportDTO> questions;

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getCooldownHours() { return cooldownHours; }
    public void setCooldownHours(Integer cooldownHours) { this.cooldownHours = cooldownHours; }

    public Integer getPassMark() { return passMark; }
    public void setPassMark(Integer passMark) { this.passMark = passMark; }

    public List<QuizQuestionExportDTO> getQuestions() { return questions; }
    public void setQuestions(List<QuizQuestionExportDTO> questions) { this.questions = questions; }
}