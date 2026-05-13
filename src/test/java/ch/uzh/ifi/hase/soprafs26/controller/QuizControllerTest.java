package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Quiz;
import ch.uzh.ifi.hase.soprafs26.entity.QuizQuestion;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.QuizService;
import ch.uzh.ifi.hase.soprafs26.service.QuizQuestionService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({QuizController.class, QuizQuestionController.class})
public class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizService quizService;

    @MockitoBean
    private QuizQuestionService quizQuestionService;

    @MockitoBean
    private UserService userService;

    private static final String TOKEN = "test-token";
    private static final Long SKILL_ID = 1L;
    private static final Long QUIZ_ID = 10L;
    private static final Long QUESTION_ID = 100L;

    private void mockAuthentication(boolean success) {
        if (success) {
            User user = new User();
            user.setId(1L);
            given(userService.getUserByToken(any())).willReturn(user);
        }
    }

    private Quiz buildQuiz() {
        Quiz quiz = new Quiz();
        quiz.setId(QUIZ_ID);
        quiz.setSkillId(SKILL_ID);
        quiz.setIsActive(true);
        quiz.setPassMark(70);
        quiz.setCooldownHours(24);
        return quiz;
    }

    private QuizQuestion buildQuestion() {
        QuizQuestion q = new QuizQuestion();
        q.setId(QUESTION_ID);
        q.setQuizId(QUIZ_ID);
        q.setQuizQuestionText("What is a variable?");
        q.setOrderIndex(1);
        return q;
    }

    // --- GET /skills/{skillId}/quiz ---

    @Test
    public void givenValidSkillId_whenGetQuiz_thenReturnOk() throws Exception {
        mockAuthentication(true);
        given(quizService.getQuizBySkillId(eq(SKILL_ID), any())).willReturn(buildQuiz());

        MockHttpServletRequestBuilder request = get("/skills/{skillId}/quiz", SKILL_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(QUIZ_ID.intValue())))
                .andExpect(jsonPath("$.passMark", is(70)));
    }

    @Test
    public void givenNoQuizForSkill_whenGetQuiz_thenReturnNotFound() throws Exception {
        mockAuthentication(true);
        given(quizService.getQuizBySkillId(eq(SKILL_ID), any()))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No quiz found"));

        MockHttpServletRequestBuilder request = get("/skills/{skillId}/quiz", SKILL_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    public void givenNoAuthorization_whenGetQuiz_thenReturnUnauthorized() throws Exception {
        mockAuthentication(false);

        mockMvc.perform(get("/skills/{skillId}/quiz", SKILL_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // --- POST /skills/{skillId}/quiz ---

    @Test
    public void givenValidInput_whenCreateQuiz_thenReturnCreated() throws Exception {
        mockAuthentication(true);
        given(quizService.createQuiz(eq(SKILL_ID), any(), any())).willReturn(buildQuiz());

        MockHttpServletRequestBuilder request = post("/skills/{skillId}/quiz", SKILL_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isActive\": true, \"passMark\": 70, \"cooldownHours\": 24}");

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(QUIZ_ID.intValue())));
    }

    @Test
    public void givenNonOwner_whenCreateQuiz_thenReturnForbidden() throws Exception {
        mockAuthentication(true);
        given(quizService.createQuiz(eq(SKILL_ID), any(), any()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can create a quiz"));

        MockHttpServletRequestBuilder request = post("/skills/{skillId}/quiz", SKILL_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isActive\": true}");

        mockMvc.perform(request).andExpect(status().isForbidden());
    }

    @Test
    public void givenQuizAlreadyExists_whenCreateQuiz_thenReturnConflict() throws Exception {
        mockAuthentication(true);
        given(quizService.createQuiz(eq(SKILL_ID), any(), any()))
                .willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Quiz already exists"));

        MockHttpServletRequestBuilder request = post("/skills/{skillId}/quiz", SKILL_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isActive\": true}");

        mockMvc.perform(request).andExpect(status().isConflict());
    }

    @Test
    public void givenNoAuthorization_whenCreateQuiz_thenReturnUnauthorized() throws Exception {
        mockAuthentication(false);

        mockMvc.perform(post("/skills/{skillId}/quiz", SKILL_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isActive\": true}"))
                .andExpect(status().isUnauthorized());
    }

    // --- PATCH /quizzes/{quizId} ---

    @Test
    public void givenValidInput_whenUpdateQuiz_thenReturnOk() throws Exception {
        mockAuthentication(true);
        Quiz updated = buildQuiz();
        updated.setPassMark(80);
        given(quizService.updateQuiz(eq(QUIZ_ID), any(), any())).willReturn(updated);

        MockHttpServletRequestBuilder request = patch("/quizzes/{quizId}", QUIZ_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"passMark\": 80}");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passMark", is(80)));
    }

    @Test
    public void givenNonOwner_whenUpdateQuiz_thenReturnForbidden() throws Exception {
        mockAuthentication(true);
        given(quizService.updateQuiz(eq(QUIZ_ID), any(), any()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(patch("/quizzes/{quizId}", QUIZ_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"passMark\": 80}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenNoAuthorization_whenUpdateQuiz_thenReturnUnauthorized() throws Exception {
        mockAuthentication(false);

        mockMvc.perform(patch("/quizzes/{quizId}", QUIZ_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"passMark\": 80}"))
                .andExpect(status().isUnauthorized());
    }

    // --- DELETE /quizzes/{quizId} ---

    @Test
    public void givenValidOwner_whenDeleteQuiz_thenReturnNoContent() throws Exception {
        mockAuthentication(true);

        mockMvc.perform(delete("/quizzes/{quizId}", QUIZ_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void givenNonOwner_whenDeleteQuiz_thenReturnForbidden() throws Exception {
        mockAuthentication(true);
        willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .given(quizService).deleteQuiz(eq(QUIZ_ID), any());

        mockMvc.perform(delete("/quizzes/{quizId}", QUIZ_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenNoAuthorization_whenDeleteQuiz_thenReturnUnauthorized() throws Exception {
        mockAuthentication(false);

        mockMvc.perform(delete("/quizzes/{quizId}", QUIZ_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // --- GET /quizzes/{quizId}/quizQuestions ---

    @Test
    public void givenValidQuizId_whenGetQuestions_thenReturnOk() throws Exception {
        mockAuthentication(true);
        given(quizQuestionService.getQuestionsByQuizId(eq(QUIZ_ID), any()))
                .willReturn(List.of(buildQuestion()));

        mockMvc.perform(get("/quizzes/{quizId}/quizQuestions", QUIZ_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(QUESTION_ID.intValue())))
                .andExpect(jsonPath("$[0].quizQuestionText", is("What is a variable?")));
    }

    @Test
    public void givenNoAuthorization_whenGetQuestions_thenReturnUnauthorized() throws Exception {
        mockAuthentication(false);

        mockMvc.perform(get("/quizzes/{quizId}/quizQuestions", QUIZ_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // --- POST /quizzes/{quizId}/quizQuestions ---

    @Test
    public void givenValidInput_whenCreateQuestion_thenReturnCreated() throws Exception {
        mockAuthentication(true);
        given(quizQuestionService.createQuestion(eq(QUIZ_ID), any(), any()))
                .willReturn(buildQuestion());

        mockMvc.perform(post("/quizzes/{quizId}/quizQuestions", QUIZ_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quizQuestionText\": \"What is a variable?\", \"orderIndex\": 1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quizQuestionText", is("What is a variable?")));
    }

    @Test
    public void givenNonOwner_whenCreateQuestion_thenReturnForbidden() throws Exception {
        mockAuthentication(true);
        given(quizQuestionService.createQuestion(eq(QUIZ_ID), any(), any()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(post("/quizzes/{quizId}/quizQuestions", QUIZ_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quizQuestionText\": \"What is a variable?\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenNoAuthorization_whenCreateQuestion_thenReturnUnauthorized() throws Exception {
        mockAuthentication(false);

        mockMvc.perform(post("/quizzes/{quizId}/quizQuestions", QUIZ_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quizQuestionText\": \"What is a variable?\"}"))
                .andExpect(status().isUnauthorized());
    }

    // --- PATCH /quizQuestions/{quizQuestionId} ---

    @Test
    public void givenValidInput_whenUpdateQuestion_thenReturnOk() throws Exception {
        mockAuthentication(true);
        QuizQuestion updated = buildQuestion();
        updated.setQuizQuestionText("Updated question?");
        given(quizQuestionService.updateQuestion(eq(QUESTION_ID), any(), any())).willReturn(updated);

        mockMvc.perform(patch("/quizQuestions/{quizQuestionId}", QUESTION_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quizQuestionText\": \"Updated question?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quizQuestionText", is("Updated question?")));
    }

    @Test
    public void givenNoAuthorization_whenUpdateQuestion_thenReturnUnauthorized() throws Exception {
        mockAuthentication(false);

        mockMvc.perform(patch("/quizQuestions/{quizQuestionId}", QUESTION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quizQuestionText\": \"Updated?\"}"))
                .andExpect(status().isUnauthorized());
    }

    // --- DELETE /quizQuestions/{quizQuestionId} ---

    @Test
    public void givenValidOwner_whenDeleteQuestion_thenReturnNoContent() throws Exception {
        mockAuthentication(true);

        mockMvc.perform(delete("/quizQuestions/{quizQuestionId}", QUESTION_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void givenNonOwner_whenDeleteQuestion_thenReturnForbidden() throws Exception {
        mockAuthentication(true);
        willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .given(quizQuestionService).deleteQuestion(eq(QUESTION_ID), any());

        mockMvc.perform(delete("/quizQuestions/{quizQuestionId}", QUESTION_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenNoAuthorization_whenDeleteQuestion_thenReturnUnauthorized() throws Exception {
        mockAuthentication(false);

        mockMvc.perform(delete("/quizQuestions/{quizQuestionId}", QUESTION_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}