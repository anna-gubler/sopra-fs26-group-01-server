package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.QuizQuestion;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizQuestionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizQuestionPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.QuizQuestionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class QuizQuestionController {

    private final QuizQuestionService quizQuestionService;

    public QuizQuestionController(QuizQuestionService quizQuestionService) {
        this.quizQuestionService = quizQuestionService;
    }

    // 905 - GET /quizzes/{quizId}/quizQuestions
    @GetMapping("/quizzes/{quizId}/quizQuestions")
    @ResponseStatus(HttpStatus.OK)
    public List<QuizQuestionGetDTO> getQuestionsByQuizId(@PathVariable Long quizId, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        return quizQuestionService.getQuestionsByQuizId(quizId, user)
                .stream()
                .map(DTOMapper.INSTANCE::convertQuizQuestionEntityToQuizQuestionGetDTO)
                .collect(Collectors.toList());
    }

    // 906 - POST /quizzes/{quizId}/quizQuestions
    @PostMapping("/quizzes/{quizId}/quizQuestions")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizQuestionGetDTO createQuestion(@PathVariable Long quizId,
                                             @RequestBody QuizQuestionPostDTO quizQuestionPostDTO,
                                             HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        QuizQuestion incoming = DTOMapper.INSTANCE.convertQuizQuestionPostDTOToQuizQuestionEntity(quizQuestionPostDTO);
        QuizQuestion created = quizQuestionService.createQuestion(quizId, incoming, user);
        return DTOMapper.INSTANCE.convertQuizQuestionEntityToQuizQuestionGetDTO(created);
    }

    // 907 - PATCH /quizQuestions/{quizQuestionId}
    @PatchMapping("/quizQuestions/{quizQuestionId}")
    @ResponseStatus(HttpStatus.OK)
    public QuizQuestionGetDTO updateQuestion(@PathVariable Long quizQuestionId,
                                             @RequestBody QuizQuestionPostDTO quizQuestionPostDTO,
                                             HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        QuizQuestion incoming = DTOMapper.INSTANCE.convertQuizQuestionPostDTOToQuizQuestionEntity(quizQuestionPostDTO);
        QuizQuestion updated = quizQuestionService.updateQuestion(quizQuestionId, incoming, user);
        return DTOMapper.INSTANCE.convertQuizQuestionEntityToQuizQuestionGetDTO(updated);
    }

    // 908 - DELETE /quizQuestions/{quizQuestionId}
    @DeleteMapping("/quizQuestions/{quizQuestionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuestion(@PathVariable Long quizQuestionId, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        quizQuestionService.deleteQuestion(quizQuestionId, user);
    }
}