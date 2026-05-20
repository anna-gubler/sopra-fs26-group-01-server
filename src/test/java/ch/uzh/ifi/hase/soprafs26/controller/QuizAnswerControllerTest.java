package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.QuizAnswer;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.QuizAnswerService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuizAnswerController.class)
public class QuizAnswerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizAnswerService quizAnswerService;

    @MockitoBean
    private UserService userService;

    private static final String TOKEN = "test-token";
    private static final Long QUESTION_ID = 40L;
    private static final Long ANSWER_ID = 50L;

    private void mockAuthentication(boolean success) {
        if (success) {
            User user = new User();
            user.setId(1L);
            given(userService.getUserByToken(any())).willReturn(user);
        }
    }

    private QuizAnswer buildAnswer() {
        QuizAnswer answer = new QuizAnswer();
        answer.setId(ANSWER_ID);
        answer.setQuizQuestionId(QUESTION_ID);
        answer.setAnswerText("Java is a programming language.");
        answer.setIsCorrect(true);
        return answer;
    }

    // --- GET /quizQuestions/{quizQuestionId}/answers ---

    @Test
    public void givenValidRequest_whenGetAnswers_thenReturnOk() throws Exception {
        mockAuthentication(true);
        given(quizAnswerService.getAnswersByQuestionId(eq(QUESTION_ID), any()))
                .willReturn(List.of(buildAnswer()));

        mockMvc.perform(get("/quizQuestions/{quizQuestionId}/answers", QUESTION_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(ANSWER_ID.intValue())))
                .andExpect(jsonPath("$[0].answerText", is("Java is a programming language.")));
    }

    @Test
    public void givenNoAuthorization_whenGetAnswers_thenReturnUnauthorized() throws Exception {
        mockAuthentication(false);

        mockMvc.perform(get("/quizQuestions/{quizQuestionId}/answers", QUESTION_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenNotMember_whenGetAnswers_thenReturnForbidden() throws Exception {
        mockAuthentication(true);
        given(quizAnswerService.getAnswersByQuestionId(eq(QUESTION_ID), any()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(get("/quizQuestions/{quizQuestionId}/answers", QUESTION_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // --- POST /quizQuestions/{quizQuestionId}/answers ---

    @Test
    public void givenValidInput_whenCreateAnswer_thenReturnCreated() throws Exception {
        mockAuthentication(true);
        given(quizAnswerService.createAnswer(eq(QUESTION_ID), any(), any()))
                .willReturn(buildAnswer());

        mockMvc.perform(post("/quizQuestions/{quizQuestionId}/answers", QUESTION_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"answerText\": \"Java is a programming language.\", \"isCorrect\": true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(ANSWER_ID.intValue())))
                .andExpect(jsonPath("$.answerText", is("Java is a programming language.")));
    }

    @Test
    public void givenNonOwner_whenCreateAnswer_thenReturnForbidden() throws Exception {
        mockAuthentication(true);
        given(quizAnswerService.createAnswer(eq(QUESTION_ID), any(), any()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(post("/quizQuestions/{quizQuestionId}/answers", QUESTION_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"answerText\": \"Some answer\", \"isCorrect\": false}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenNoAuthorization_whenCreateAnswer_thenReturnUnauthorized() throws Exception {
        mockAuthentication(false);

        mockMvc.perform(post("/quizQuestions/{quizQuestionId}/answers", QUESTION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"answerText\": \"Some answer\", \"isCorrect\": false}"))
                .andExpect(status().isUnauthorized());
    }

    // --- PATCH /answers/{answerId} ---

    @Test
    public void givenValidInput_whenUpdateAnswer_thenReturnOk() throws Exception {
        mockAuthentication(true);
        QuizAnswer updated = buildAnswer();
        updated.setAnswerText("Updated answer text.");
        given(quizAnswerService.updateAnswer(eq(ANSWER_ID), any(), any())).willReturn(updated);

        mockMvc.perform(patch("/answers/{answerId}", ANSWER_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"answerText\": \"Updated answer text.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answerText", is("Updated answer text.")));
    }

    @Test
    public void givenNonOwner_whenUpdateAnswer_thenReturnForbidden() throws Exception {
        mockAuthentication(true);
        given(quizAnswerService.updateAnswer(eq(ANSWER_ID), any(), any()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(patch("/answers/{answerId}", ANSWER_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"answerText\": \"Updated\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenNoAuthorization_whenUpdateAnswer_thenReturnUnauthorized() throws Exception {
        mockAuthentication(false);

        mockMvc.perform(patch("/answers/{answerId}", ANSWER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"answerText\": \"Updated\"}"))
                .andExpect(status().isUnauthorized());
    }

    // --- DELETE /answers/{answerId} ---

    @Test
    public void givenValidOwner_whenDeleteAnswer_thenReturnNoContent() throws Exception {
        mockAuthentication(true);

        mockMvc.perform(delete("/answers/{answerId}", ANSWER_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void givenNonOwner_whenDeleteAnswer_thenReturnForbidden() throws Exception {
        mockAuthentication(true);
        willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .given(quizAnswerService).deleteAnswer(eq(ANSWER_ID), any());

        mockMvc.perform(delete("/answers/{answerId}", ANSWER_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenNoAuthorization_whenDeleteAnswer_thenReturnUnauthorized() throws Exception {
        mockAuthentication(false);

        mockMvc.perform(delete("/answers/{answerId}", ANSWER_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
