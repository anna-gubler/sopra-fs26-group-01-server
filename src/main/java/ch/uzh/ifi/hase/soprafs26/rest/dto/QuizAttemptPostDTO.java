package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class QuizAttemptPostDTO {
    private List<SubmittedAnswerDTO> answers;

    public List<SubmittedAnswerDTO> getAnswers() { return answers; }
    public void setAnswers(List<SubmittedAnswerDTO> answers) { this.answers = answers; }
}
