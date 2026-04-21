package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SpeedFeedbackGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SpeedFeedbackPutDTO;
import ch.uzh.ifi.hase.soprafs26.service.SpeedFeedbackService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sessions/{sessionId}")
public class SpeedFeedbackController {

    private final SpeedFeedbackService speedFeedbackService;

    public SpeedFeedbackController(SpeedFeedbackService speedFeedbackService) {
        this.speedFeedbackService = speedFeedbackService;
    }

    @GetMapping("/speed")
    @ResponseStatus(HttpStatus.OK)
    public SpeedFeedbackGetDTO getSpeedCounts(@PathVariable Long sessionId) {
        return speedFeedbackService.getCounts(sessionId);
    }

    @PutMapping("/speed")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submitSpeedFeedback(@PathVariable Long sessionId,
                                    @RequestBody SpeedFeedbackPutDTO body,
                                    HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        speedFeedbackService.submitFeedback(sessionId, user.getId(), body.getFeedback());
    }
}
