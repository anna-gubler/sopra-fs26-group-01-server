package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.UnderstandingRating;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.UnderstandingRatingService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class UnderstandingRatingController {

    private final UnderstandingRatingService ratingService;
    private final UserService userService;

    public UnderstandingRatingController(UnderstandingRatingService ratingService,
            UserService userService) {
        this.ratingService = ratingService;
        this.userService = userService;
    }

    // 701 - PUT /sessions/{sessionId}/skills/{skillId}/rating
    @PutMapping("/sessions/{sessionId}/skills/{skillId}/rating")
    @ResponseStatus(HttpStatus.OK)
    public UnderstandingRating submitRating(@PathVariable Long sessionId,
            @PathVariable Long skillId,
            @RequestBody Map<String, Integer> body,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring("Bearer ".length()).trim();
        User user = userService.getUserByToken(token);
        Integer rating = body.get("rating");
        return ratingService.submitRating(sessionId, skillId, user, rating);
    }

    // 702 - GET /sessions/{sessionId}/skills/{skillId}/ratings
    @GetMapping("/sessions/{sessionId}/skills/{skillId}/ratings")
    @ResponseStatus(HttpStatus.OK)
    public List<UnderstandingRating> getRatingsBySkill(@PathVariable Long sessionId,
            @PathVariable Long skillId,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring("Bearer ".length()).trim();
        User user = userService.getUserByToken(token);
        return ratingService.getRatingsBySkill(sessionId, skillId, user);
    }

    // 703 - GET /sessions/{sessionId}/ratings
    @GetMapping("/sessions/{sessionId}/ratings")
    @ResponseStatus(HttpStatus.OK)
    public List<UnderstandingRating> getRatingsBySession(@PathVariable Long sessionId,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring("Bearer ".length()).trim();
        User user = userService.getUserByToken(token);
        return ratingService.getRatingsBySession(sessionId, user);
    }
}