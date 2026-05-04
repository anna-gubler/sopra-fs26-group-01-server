package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class SubmittedAnswerDTO {
    private Long quizQuestionId;
    private Long selectedAnswerId;

    public Long getQuizQuestionId() { return quizQuestionId; }
    public void setQuizQuestionId(Long quizQuestionId) { this.quizQuestionId = quizQuestionId; }

    public Long getSelectedAnswerId() { return selectedAnswerId; }
    public void setSelectedAnswerId(Long selectedAnswerId) { this.selectedAnswerId = selectedAnswerId; }
}
