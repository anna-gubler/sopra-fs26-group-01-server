package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.CollaborationSession;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.DashboardQuizSummaryDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionStateDTO;
import ch.uzh.ifi.hase.soprafs26.service.CollaborationSessionService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillmaps/{skillMapId}/sessions")
public class CollaborationSessionController {

    private final CollaborationSessionService sessionService;

    public CollaborationSessionController(CollaborationSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CollaborationSession> getPastSessions(@PathVariable Long skillMapId, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        return sessionService.getPastSessions(skillMapId, user);
    }

    @PostMapping("/{sessionId}/restart")
    @ResponseStatus(HttpStatus.OK)
    public CollaborationSession restartSession(@PathVariable Long skillMapId,
                                               @PathVariable Long sessionId,
                                               HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        return sessionService.restartSession(skillMapId, sessionId, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CollaborationSession startSession(@PathVariable Long skillMapId, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        return sessionService.startSession(skillMapId, user);
    }

    @GetMapping("/active")
    @ResponseStatus(HttpStatus.OK)
    public SessionStateDTO getActiveSession(@PathVariable Long skillMapId, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        return sessionService.getActiveSessionState(skillMapId, user);
    }

    @PostMapping("/active/end")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void endSession(@PathVariable Long skillMapId, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        sessionService.endSession(skillMapId, user);
    }

    @PutMapping("/active/prompted-quiz")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setPromptedQuiz(@PathVariable Long skillMapId,
                                @RequestBody Map<String, Object> body,
                                HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        Long skillId = body.get("skillId") != null ? ((Number) body.get("skillId")).longValue() : null;
        sessionService.setPromptedQuiz(skillMapId, user, skillId);
    }

    @GetMapping("/active/quiz-results")
    @ResponseStatus(HttpStatus.OK)
    public List<DashboardQuizSummaryDTO> getQuizResults(@PathVariable Long skillMapId,
                                                        HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        return sessionService.getQuizResults(skillMapId, user);
    }
}