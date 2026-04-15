package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.LiveQuestion;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.LiveQuestionService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class LiveQuestionController {

    private final LiveQuestionService liveQuestionService;
    private final UserService userService;

    @Autowired
    public LiveQuestionController(LiveQuestionService liveQuestionService, UserService userService) {
        this.liveQuestionService = liveQuestionService;
        this.userService = userService;
    }

    // 801: GET /sessions/{sessionId}/questions
    @GetMapping("/sessions/{sessionId}/questions")
    @ResponseStatus(HttpStatus.OK)
    public List<LiveQuestion> getQuestions(@PathVariable Long sessionId) {
        return liveQuestionService.getQuestionsBySession(sessionId);
    }

    // 802: POST /sessions/{sessionId}/questions
    @PostMapping("/sessions/{sessionId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public LiveQuestion postQuestion(@PathVariable Long sessionId,
                                     @RequestBody Map<String, Object> body) {
        Long skillId = body.get("skillId") != null ? Long.valueOf(body.get("skillId").toString()) : null;
        String skillName = (String) body.get("skillName");
        String text = (String) body.get("text");
        return liveQuestionService.postQuestion(sessionId, skillId, skillName, text);
    }

    // 803: DELETE /questions/{questionId}
    @DeleteMapping("/questions/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuestion(@PathVariable Long questionId) {
        liveQuestionService.deleteQuestion(questionId);
    }

    // 804: POST /questions/{questionId}/upvotes
    @PostMapping("/questions/{questionId}/upvotes")
    @ResponseStatus(HttpStatus.CREATED)
    public void upvoteQuestion(@PathVariable Long questionId,
                               @RequestHeader("Authorization") String token) {
        User user = userService.getUserByToken(token);
        liveQuestionService.upvoteQuestion(questionId, user.getId());
    }

    // 805: DELETE /questions/{questionId}/upvotes/me
    @DeleteMapping("/questions/{questionId}/upvotes/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUpvote(@PathVariable Long questionId,
                             @RequestHeader("Authorization") String token) {
        User user = userService.getUserByToken(token);
        liveQuestionService.removeUpvote(questionId, user.getId());
    }

    // 806: POST /questions/{questionId}/mark-addressed
    @PostMapping("/questions/{questionId}/mark-addressed")
    @ResponseStatus(HttpStatus.OK)
    public LiveQuestion markAddressed(@PathVariable Long questionId,
                                    @RequestHeader("Authorization") String token) {
        User user = userService.getUserByToken(token);
        return liveQuestionService.markAddressed(questionId, user.getId());
    }
}