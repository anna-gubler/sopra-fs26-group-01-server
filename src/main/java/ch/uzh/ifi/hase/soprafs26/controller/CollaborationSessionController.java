package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.CollaborationSession;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.CollaborationSessionService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillmaps/{skillMapId}/sessions")
public class CollaborationSessionController {

    private final CollaborationSessionService sessionService;
    private final UserService userService;

    public CollaborationSessionController(CollaborationSessionService sessionService,
                                          UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CollaborationSession startSession(@PathVariable Long skillMapId,
                                             @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring("Bearer ".length()).trim();
        User user = userService.getUserByToken(token);
        return sessionService.startSession(skillMapId, user);
    }

    @GetMapping("/active")
    @ResponseStatus(HttpStatus.OK)
    public CollaborationSession getActiveSession(@PathVariable Long skillMapId) {
        return sessionService.getActiveSession(skillMapId);
    }

    @PostMapping("/active/end")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void endSession(@PathVariable Long skillMapId,
                           @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring("Bearer ".length()).trim();
        User user = userService.getUserByToken(token);
        sessionService.endSession(skillMapId, user);
    }
}