package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.SpeedFeedback;

public class SpeedFeedbackPutDTO {
    private SpeedFeedback feedback;

    public SpeedFeedback getFeedback() { return feedback; }
    public void setFeedback(SpeedFeedback feedback) { this.feedback = feedback; }
}

