package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.QuizAttempt;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizAttemptGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizAttemptPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.QuizAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    public QuizAttemptController(QuizAttemptService quizAttemptService) {
        this.quizAttemptService = quizAttemptService;
    }

    // 1001 - POST /quizzes/{quizId}/attempts
    @PostMapping("/quizzes/{quizId}/attempts")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizAttemptGetDTO createAttempt(@PathVariable Long quizId, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        QuizAttempt created = quizAttemptService.createAttempt(quizId, user);
        return DTOMapper.INSTANCE.convertQuizAttemptEntityToQuizAttemptGetDTO(created);
    }

    // 1002 - GET /quizzes/{quizId}/attempts/me/latest
    @GetMapping("/quizzes/{quizId}/attempts/me/latest")
    @ResponseStatus(HttpStatus.OK)
    public QuizAttemptGetDTO getLatestAttempt(@PathVariable Long quizId, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        QuizAttempt attempt = quizAttemptService.getLatestAttempt(quizId, user);
        return DTOMapper.INSTANCE.convertQuizAttemptEntityToQuizAttemptGetDTO(attempt);
    }

    // 1003 - GET /attempts/{attemptId}
    @GetMapping("/attempts/{attemptId}")
    @ResponseStatus(HttpStatus.OK)
    public QuizAttemptGetDTO getAttemptById(@PathVariable Long attemptId, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        QuizAttempt attempt = quizAttemptService.getAttemptById(attemptId, user);
        return DTOMapper.INSTANCE.convertQuizAttemptEntityToQuizAttemptGetDTO(attempt);
    }

    // 1004 - POST /attempts/{attemptId}/submit
    @PostMapping("/attempts/{attemptId}/submit")
    @ResponseStatus(HttpStatus.OK)
    public QuizAttemptGetDTO submitAttempt(@PathVariable Long attemptId,
                                           @RequestBody QuizAttemptPostDTO quizAttemptPostDTO,
                                           HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        QuizAttempt submitted = quizAttemptService.submitAttempt(attemptId, quizAttemptPostDTO.getAnswers(), user);
        return DTOMapper.INSTANCE.convertQuizAttemptEntityToQuizAttemptGetDTO(submitted);
    }
}