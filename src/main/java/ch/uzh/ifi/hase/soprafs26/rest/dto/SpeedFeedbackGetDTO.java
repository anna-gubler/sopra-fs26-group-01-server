package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class SpeedFeedbackGetDTO {

    private final int tooFast;
    private final int tooSlow;
    private final int totalResponses;

    public SpeedFeedbackGetDTO(int tooFast, int tooSlow, int totalResponses) {
        this.tooFast = tooFast;
        this.tooSlow = tooSlow;
        this.totalResponses = totalResponses;
    }

    public int getTooFast()        { return tooFast; }
    public int getTooSlow()        { return tooSlow; }
    public int getTotalResponses() { return totalResponses; }
}
