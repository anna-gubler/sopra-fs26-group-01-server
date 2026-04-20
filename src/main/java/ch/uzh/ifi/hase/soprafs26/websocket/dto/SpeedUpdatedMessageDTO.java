package ch.uzh.ifi.hase.soprafs26.websocket.dto;

public class SpeedUpdatedMessageDTO {
    private final String type = "SPEED_UPDATED";
    private final int tooFast;
    private final int tooSlow;
    private final int totalResponses;

    public SpeedUpdatedMessageDTO(int tooFast, int tooSlow, int totalResponses) {
        this.tooFast = tooFast;
        this.tooSlow = tooSlow;
        this.totalResponses = totalResponses;
    }

    public String getType()         { return type; }
    public int getTooFast()         { return tooFast; }
    public int getTooSlow()         { return tooSlow; }
    public int getTotalResponses()  { return totalResponses; }
}