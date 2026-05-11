package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Quiz;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.QuizService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    // 901 - GET /skills/{skillId}/quiz
    @GetMapping("/skills/{skillId}/quiz")
    @ResponseStatus(HttpStatus.OK)
    public QuizGetDTO getQuizBySkillId(@PathVariable Long skillId, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        Quiz quiz = quizService.getQuizBySkillId(skillId, user);
        return DTOMapper.INSTANCE.convertQuizEntityToQuizGetDTO(quiz);
    }

    // 902 - POST /skills/{skillId}/quiz
    @PostMapping("/skills/{skillId}/quiz")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizGetDTO createQuiz(@PathVariable Long skillId,
                                 @RequestBody QuizPostDTO quizPostDTO,
                                 HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        Quiz incoming = DTOMapper.INSTANCE.convertQuizPostDTOToQuizEntity(quizPostDTO);
        Quiz created = quizService.createQuiz(skillId, incoming, user);
        return DTOMapper.INSTANCE.convertQuizEntityToQuizGetDTO(created);
    }

    // 903 - PATCH /quizzes/{quizId}
    @PatchMapping("/quizzes/{quizId}")
    @ResponseStatus(HttpStatus.OK)
    public QuizGetDTO updateQuiz(@PathVariable Long quizId,
                                 @RequestBody QuizPostDTO quizPostDTO,
                                 HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        Quiz incoming = DTOMapper.INSTANCE.convertQuizPostDTOToQuizEntity(quizPostDTO);
        Quiz updated = quizService.updateQuiz(quizId, incoming, user);
        return DTOMapper.INSTANCE.convertQuizEntityToQuizGetDTO(updated);
    }

    // 904 - DELETE /quizzes/{quizId}
    @DeleteMapping("/quizzes/{quizId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuiz(@PathVariable Long quizId, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        quizService.deleteQuiz(quizId, user);
    }
}