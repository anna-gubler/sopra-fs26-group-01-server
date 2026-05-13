package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class QuizQuestionPostDTO {
    private String quizQuestionText;
    private Integer orderIndex;

    public String getQuizQuestionText() { return quizQuestionText; }
    public void setQuizQuestionText(String quizQuestionText) { this.quizQuestionText = quizQuestionText; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
}