package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class QuizQuestionExportDTO {
    private String quizQuestionText;
    private Integer orderIndex;
    private List<QuizAnswerExportDTO> answers;

    public String getQuizQuestionText() { return quizQuestionText; }
    public void setQuizQuestionText(String t) { this.quizQuestionText = t; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer i) { this.orderIndex = i; }

    public List<QuizAnswerExportDTO> getAnswers() { return answers; }
    public void setAnswers(List<QuizAnswerExportDTO> answers) { this.answers = answers; }
}