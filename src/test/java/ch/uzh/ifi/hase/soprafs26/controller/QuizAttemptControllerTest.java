package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.QuizAttempt;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizAttemptPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SubmittedAnswerDTO;
import ch.uzh.ifi.hase.soprafs26.service.QuizAttemptService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuizAttemptController.class)
public class QuizAttemptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizAttemptService quizAttemptService;

    @MockitoBean
    private UserService userService;

    private static final String TOKEN = "test-token";
    private static final Long QUIZ_ID = 30L;
    private static final Long ATTEMPT_ID = 60L;

    private void mockAuthentication(boolean success) {
        if (success) {
            User user = new User();
            user.setId(2L);
            given(userService.getUserByToken(any())).willReturn(user);
        }
    }

    private QuizAttempt buildAttempt() {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setId(ATTEMPT_ID);
        attempt.setUserId(2L);
        attempt.setQuizId(QUIZ_ID);
        attempt.setScore(100);
        attempt.setPassed(true);
        return attempt;
    }

    private String asJsonString(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JacksonException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not serialize object");
        }
    }

    // --- POST /quizzes/{quizId}/attempts ---

    @Test
    public void givenValidRequest_whenCreateAttempt_thenReturnCreated() throws Exception {
        mockAuthentication(true);
        given(quizAttemptService.createAttempt(eq(QUIZ_ID), any())).willReturn(buildAttempt());

        mockMvc.perform(post("/quizzes/{quizId}/attempts", QUIZ_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(ATTEMPT_ID.intValue())))
                .andExpect(jsonPath("$.quizId", is(QUIZ_ID.intValue())));
    }

    @Test
    public void givenNoAuthorization_whenCreateAttempt_thenReturnUnauthorized() throws Exception {
        mockAuthentication(false);

        mockMvc.perform(post("/quizzes/{quizId}/attempts", QUIZ_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenInactiveQuiz_whenCreateAttempt_thenReturnForbidden() throws Exception {
        mockAuthentication(true);
        given(quizAttemptService.createAttempt(eq(QUIZ_ID), any()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Quiz is not active"));

        mockMvc.perform(post("/quizzes/{quizId}/attempts", QUIZ_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenExistingUnsubmittedAttempt_whenCreateAttempt_thenReturnConflict() throws Exception {
        mockAuthentication(true);
        given(quizAttemptService.createAttempt(eq(QUIZ_ID), any()))
                .willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Unsubmitted attempt exists"));

        mockMvc.perform(post("/quizzes/{quizId}/attempts", QUIZ_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    // --- GET /quizzes/{quizId}/attempts/me/latest ---

    @Test
    public void givenValidRequest_whenGetLatestAttempt_thenReturnOk() throws Exception {
        mockAuthentication(true);
        given(quizAttemptService.getLatestAttempt(eq(QUIZ_ID), any())).willReturn(buildAttempt());

        mockMvc.perform(get("/quizzes/{quizId}/attempts/me/latest", QUIZ_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ATTEMPT_ID.intValue())))
                .andExpect(jsonPath("$.passed", is(true)));
    }

    @Test
    public void givenNoAttempt_whenGetLatestAttempt_thenReturnNotFound() throws Exception {
        mockAuthentication(true);
        given(quizAttemptService.getLatestAttempt(eq(QUIZ_ID), any()))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No attempt found"));

        mockMvc.perform(get("/quizzes/{quizId}/attempts/me/latest", QUIZ_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenNoAuthorization_whenGetLatestAttempt_thenReturnUnauthorized() throws Exception {
        mockAuthentication(false);

        mockMvc.perform(get("/quizzes/{quizId}/attempts/me/latest", QUIZ_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // --- GET /attempts/{attemptId} ---

    @Test
    public void givenValidRequest_whenGetAttemptById_thenReturnOk() throws Exception {
        mockAuthentication(true);
        given(quizAttemptService.getAttemptById(eq(ATTEMPT_ID), any())).willReturn(buildAttempt());

        mockMvc.perform(get("/attempts/{attemptId}", ATTEMPT_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ATTEMPT_ID.intValue())))
                .andExpect(jsonPath("$.score", is(100)));
    }

    @Test
    public void givenOtherUsersAttempt_whenGetAttemptById_thenReturnForbidden() throws Exception {
        mockAuthentication(true);
        given(quizAttemptService.getAttemptById(eq(ATTEMPT_ID), any()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(get("/attempts/{attemptId}", ATTEMPT_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenNoAuthorization_whenGetAttemptById_thenReturnUnauthorized() throws Exception {
        mockAuthentication(false);

        mockMvc.perform(get("/attempts/{attemptId}", ATTEMPT_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // --- POST /attempts/{attemptId}/submit ---

    @Test
    public void givenValidAnswers_whenSubmitAttempt_thenReturnOk() throws Exception {
        mockAuthentication(true);
        QuizAttempt submitted = buildAttempt();
        submitted.setScore(80);
        given(quizAttemptService.submitAttempt(eq(ATTEMPT_ID), any(), any())).willReturn(submitted);

        SubmittedAnswerDTO answer = new SubmittedAnswerDTO();
        answer.setQuizQuestionId(40L);
        answer.setSelectedAnswerId(50L);

        QuizAttemptPostDTO postDTO = new QuizAttemptPostDTO();
        postDTO.setAnswers(List.of(answer));

        mockMvc.perform(post("/attempts/{attemptId}/submit", ATTEMPT_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(postDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(80)));
    }

    @Test
    public void givenAlreadySubmitted_whenSubmitAttempt_thenReturnConflict() throws Exception {
        mockAuthentication(true);
        given(quizAttemptService.submitAttempt(eq(ATTEMPT_ID), any(), any()))
                .willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Already submitted"));

        QuizAttemptPostDTO postDTO = new QuizAttemptPostDTO();
        postDTO.setAnswers(List.of());

        mockMvc.perform(post("/attempts/{attemptId}/submit", ATTEMPT_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(postDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    public void givenNoAuthorization_whenSubmitAttempt_thenReturnUnauthorized() throws Exception {
        mockAuthentication(false);

        QuizAttemptPostDTO postDTO = new QuizAttemptPostDTO();
        postDTO.setAnswers(List.of());

        mockMvc.perform(post("/attempts/{attemptId}/submit", ATTEMPT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(postDTO)))
                .andExpect(status().isUnauthorized());
    }
}
