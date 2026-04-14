package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.UnderstandingRating;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.UnderstandingRatingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class UnderstandingRatingController {

    private final UnderstandingRatingService ratingService;

    public UnderstandingRatingController(UnderstandingRatingService ratingService) {
        this.ratingService = ratingService;
    }

    // 701 - PUT /sessions/{sessionId}/skills/{skillId}/rating
    @PutMapping("/sessions/{sessionId}/skills/{skillId}/rating")
    @ResponseStatus(HttpStatus.OK)
    public UnderstandingRating submitRating(@PathVariable Long sessionId,
            @PathVariable Long skillId,
            @RequestBody Map<String, Integer> body,
            HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        Integer rating = body.get("rating");
        return ratingService.submitRating(sessionId, skillId, user, rating);
    }

    // 702 - GET /sessions/{sessionId}/skills/{skillId}/ratings
    @GetMapping("/sessions/{sessionId}/skills/{skillId}/ratings")
    @ResponseStatus(HttpStatus.OK)
    public List<UnderstandingRating> getRatingsBySkill(@PathVariable Long sessionId,
            @PathVariable Long skillId,
            HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        return ratingService.getRatingsBySkill(sessionId, skillId, user);
    }

    // 703 - GET /sessions/{sessionId}/ratings
    @GetMapping("/sessions/{sessionId}/ratings")
    @ResponseStatus(HttpStatus.OK)
    public List<UnderstandingRating> getRatingsBySession(@PathVariable Long sessionId,
            HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        return ratingService.getRatingsBySession(sessionId, user);
    }
}