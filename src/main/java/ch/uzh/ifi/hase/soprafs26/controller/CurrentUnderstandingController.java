package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CurrentUnderstandingPutDTO;
import ch.uzh.ifi.hase.soprafs26.service.CurrentUnderstandingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sessions/{sessionId}")
public class CurrentUnderstandingController {

    private final CurrentUnderstandingService currentUnderstandingService;

    public CurrentUnderstandingController(CurrentUnderstandingService currentUnderstandingService) {
        this.currentUnderstandingService = currentUnderstandingService;
    }

    @PostMapping("/understanding/request")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestUnderstanding(@PathVariable Long sessionId,
                                     HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        currentUnderstandingService.requestFeedback(sessionId, user.getId());
    }

    @PutMapping("/understanding")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submitUnderstanding(@PathVariable Long sessionId,
                                    @RequestBody CurrentUnderstandingPutDTO body,
                                    HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        currentUnderstandingService.submitRating(sessionId, user.getId(), body.getRating());
    }
}
