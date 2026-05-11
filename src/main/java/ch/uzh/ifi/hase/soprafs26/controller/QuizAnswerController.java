package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.QuizAnswer;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizAnswerGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizAnswerPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.QuizAnswerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class QuizAnswerController {

    private final QuizAnswerService quizAnswerService;

    public QuizAnswerController(QuizAnswerService quizAnswerService) {
        this.quizAnswerService = quizAnswerService;
    }

    // 909 - GET /quizQuestions/{quizQuestionId}/answers
    @GetMapping("/quizQuestions/{quizQuestionId}/answers")
    @ResponseStatus(HttpStatus.OK)
    public List<QuizAnswerGetDTO> getAnswersByQuestionId(@PathVariable Long quizQuestionId,
                                                         HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        return quizAnswerService.getAnswersByQuestionId(quizQuestionId, user)
                .stream()
                .map(DTOMapper.INSTANCE::convertQuizAnswerEntityToQuizAnswerGetDTO)
                .collect(Collectors.toList());
    }

    // 910 - POST /quizQuestions/{quizQuestionId}/answers
    @PostMapping("/quizQuestions/{quizQuestionId}/answers")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizAnswerGetDTO createAnswer(@PathVariable Long quizQuestionId,
                                         @RequestBody QuizAnswerPostDTO quizAnswerPostDTO,
                                         HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        QuizAnswer incoming = DTOMapper.INSTANCE.convertQuizAnswerPostDTOToQuizAnswerEntity(quizAnswerPostDTO);
        QuizAnswer created = quizAnswerService.createAnswer(quizQuestionId, incoming, user);
        return DTOMapper.INSTANCE.convertQuizAnswerEntityToQuizAnswerGetDTO(created);
    }

    // 911 - PATCH /answers/{answerId}
    @PatchMapping("/answers/{answerId}")
    @ResponseStatus(HttpStatus.OK)
    public QuizAnswerGetDTO updateAnswer(@PathVariable Long answerId,
                                         @RequestBody QuizAnswerPostDTO quizAnswerPostDTO,
                                         HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        QuizAnswer incoming = DTOMapper.INSTANCE.convertQuizAnswerPostDTOToQuizAnswerEntity(quizAnswerPostDTO);
        QuizAnswer updated = quizAnswerService.updateAnswer(answerId, incoming, user);
        return DTOMapper.INSTANCE.convertQuizAnswerEntityToQuizAnswerGetDTO(updated);
    }

    // 912 - DELETE /answers/{answerId}
    @DeleteMapping("/answers/{answerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAnswer(@PathVariable Long answerId, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        quizAnswerService.deleteAnswer(answerId, user);
    }
}