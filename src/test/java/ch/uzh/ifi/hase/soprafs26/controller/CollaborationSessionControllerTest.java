package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.CollaborationSession;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.DashboardQuizSummaryDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionStateDTO;
import ch.uzh.ifi.hase.soprafs26.service.CollaborationSessionService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@WebMvcTest(CollaborationSessionController.class)
public class CollaborationSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CollaborationSessionService sessionService;

    @MockitoBean
    private UserService userService;

    private static final Long SKILL_MAP_ID = 1L;
    private static final String TOKEN = "test-token";

    private User buildUser() {
        User user = new User();
        user.setId(100L);
        return user;
    }

    private CollaborationSession buildActiveSession() {
        CollaborationSession session = new CollaborationSession();
        session.setSkillMapId(SKILL_MAP_ID);
        session.setStartedAt(LocalDateTime.now());
        session.setActive(true);
        return session;
    }

    // AuthInterceptor runs before every protected request and calls userService.getUserByToken().
    // This mock makes the interceptor pass and sets the resolved user as a request attribute,
    // which the controller then reads. UserService itself is not used by the controller directly.
    private void mockAuthentication(User user, boolean success) {
        if (success) {
            given(userService.getUserByToken(any())).willReturn(user);
        }
    }

    // --- POST /skillmaps/{skillMapId}/sessions ---

    @Test
    public void givenValidOwner_whenStartSession_thenReturnCreated() throws Exception {
        mockAuthentication(buildUser(), true);
        given(sessionService.startSession(eq(SKILL_MAP_ID), any())).willReturn(buildActiveSession());

        MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    public void givenNonOwner_whenStartSession_thenReturnForbidden() throws Exception {
        mockAuthentication(buildUser(), true);
        given(sessionService.startSession(eq(SKILL_MAP_ID), any()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can start a session"));

        MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenNonExistingSkillMap_whenStartSession_thenReturnNotFound() throws Exception {
        mockAuthentication(buildUser(), true);
        given(sessionService.startSession(eq(SKILL_MAP_ID), any()))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill map not found"));

        MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenAlreadyActiveSession_whenStartSession_thenReturnConflict() throws Exception {
        mockAuthentication(buildUser(), true);
        given(sessionService.startSession(eq(SKILL_MAP_ID), any()))
                .willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "A session is already active"));

        MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isConflict());
    }

    @Test
    public void givenNoAuthorization_whenStartSession_thenReturnUnauthorized() throws Exception {
        mockAuthentication(buildUser(), false);

        MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions", SKILL_MAP_ID)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
    }

    // --- GET /skillmaps/{skillMapId}/sessions/active ---

    @Test
    public void givenActiveSession_whenGetActiveSession_thenReturnOk() throws Exception {
        mockAuthentication(buildUser(), true);
        SessionStateDTO state = new SessionStateDTO();
        state.setId(10L);
        state.setActive(true);
        given(sessionService.getActiveSessionState(eq(SKILL_MAP_ID), any())).willReturn(state);

        MockHttpServletRequestBuilder getRequest = get("/skillmaps/{skillMapId}/sessions/active", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    public void givenNoActiveSession_whenGetActiveSession_thenReturnNotFound() throws Exception {
        mockAuthentication(buildUser(), true);
        given(sessionService.getActiveSessionState(eq(SKILL_MAP_ID), any()))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No active session"));

        MockHttpServletRequestBuilder getRequest = get("/skillmaps/{skillMapId}/sessions/active", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenNonMember_whenGetActiveSession_thenReturnForbidden() throws Exception {
        mockAuthentication(buildUser(), true);
        given(sessionService.getActiveSessionState(eq(SKILL_MAP_ID), any()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a member of this skill map"));

        MockHttpServletRequestBuilder getRequest = get("/skillmaps/{skillMapId}/sessions/active", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenNoAuthorization_whenGetActiveSession_thenReturnUnauthorized() throws Exception {
        mockAuthentication(buildUser(), false);

        MockHttpServletRequestBuilder getRequest = get("/skillmaps/{skillMapId}/sessions/active", SKILL_MAP_ID)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized());
    }

    // --- POST /skillmaps/{skillMapId}/sessions/active/end ---

    @Test
    public void givenValidOwner_whenEndSession_thenReturnNoContent() throws Exception {
        mockAuthentication(buildUser(), true);

        MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions/active/end", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isNoContent());
    }

    @Test
    public void givenNonOwner_whenEndSession_thenReturnForbidden() throws Exception {
        mockAuthentication(buildUser(), true);
        willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can end a session"))
                .given(sessionService).endSession(eq(SKILL_MAP_ID), any());

        MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions/active/end", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenNoAuthorization_whenEndSession_thenReturnUnauthorized() throws Exception {
        mockAuthentication(buildUser(), false);

        MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions/active/end", SKILL_MAP_ID)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenNonExistingSkillMap_whenEndSession_thenReturnNotFound() throws Exception {
        mockAuthentication(buildUser(), true);
        willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill map not found"))
                .given(sessionService).endSession(eq(SKILL_MAP_ID), any());

        MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions/active/end", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenNoActiveSession_whenEndSession_thenReturnNotFound() throws Exception {
        mockAuthentication(buildUser(), true);
        willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No active session found"))
                .given(sessionService).endSession(eq(SKILL_MAP_ID), any());

        MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions/active/end", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());
    }
    // --- PUT /skillmaps/{skillMapId}/sessions/active/prompted-quiz ---
    @Test
    public void givenValidOwner_whenSetPromptedQuiz_thenReturnNoContent() throws Exception {
        mockAuthentication(buildUser(), true);

        MockHttpServletRequestBuilder putRequest = put("/skillmaps/{skillMapId}/sessions/active/prompted-quiz", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"skillId\": 42}");

        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());
        }

        @Test
        public void givenNonOwner_whenSetPromptedQuiz_thenReturnForbidden() throws Exception {
        mockAuthentication(buildUser(), true);
        willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can prompt a quiz"))
                .given(sessionService).setPromptedQuiz(eq(SKILL_MAP_ID), any(), any());

        MockHttpServletRequestBuilder putRequest = put("/skillmaps/{skillMapId}/sessions/active/prompted-quiz", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"skillId\": 42}");

        mockMvc.perform(putRequest)
                .andExpect(status().isForbidden());
        }
        @Test
        public void givenNullSkillId_whenSetPromptedQuiz_thenReturnNoContent() throws Exception {
        mockAuthentication(buildUser(), true);

        MockHttpServletRequestBuilder putRequest = put("/skillmaps/{skillMapId}/sessions/active/prompted-quiz", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"skillId\": null}");

        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());
        }

        @Test
        public void givenNoAuthorization_whenSetPromptedQuiz_thenReturnUnauthorized() throws Exception {
        mockAuthentication(buildUser(), false);

        MockHttpServletRequestBuilder putRequest = put("/skillmaps/{skillMapId}/sessions/active/prompted-quiz", SKILL_MAP_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"skillId\": 42}");

        mockMvc.perform(putRequest)
                .andExpect(status().isUnauthorized());
        }

        // --- GET /skillmaps/{skillMapId}/sessions/active/quiz-results ---

        @Test
        public void givenActiveSession_whenGetQuizResults_thenReturnOk() throws Exception {
        mockAuthentication(buildUser(), true);

        DashboardQuizSummaryDTO summary = new DashboardQuizSummaryDTO();
        summary.setQuizId(1L);
        summary.setSkillId(2L);
        summary.setTotalAttempts(5);
        summary.setAverageScore(80.0);

        given(sessionService.getQuizResults(eq(SKILL_MAP_ID), any())).willReturn(List.of(summary));

        MockHttpServletRequestBuilder getRequest = get("/skillmaps/{skillMapId}/sessions/active/quiz-results", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quizId", is(1)))
                .andExpect(jsonPath("$[0].skillId", is(2)))
                .andExpect(jsonPath("$[0].totalAttempts", is(5)))
                .andExpect(jsonPath("$[0].averageScore", is(80.0)));
        }

        @Test
        public void givenNoActiveSession_whenGetQuizResults_thenReturnNotFound() throws Exception {
        mockAuthentication(buildUser(), true);
        given(sessionService.getQuizResults(eq(SKILL_MAP_ID), any()))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No active session found"));

        MockHttpServletRequestBuilder getRequest = get("/skillmaps/{skillMapId}/sessions/active/quiz-results", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
        }

        @Test
        public void givenNoAuthorization_whenGetQuizResults_thenReturnUnauthorized() throws Exception {
        mockAuthentication(buildUser(), false);

        MockHttpServletRequestBuilder getRequest = get("/skillmaps/{skillMapId}/sessions/active/quiz-results", SKILL_MAP_ID)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized());
        }

        // POST /skillmaps/{skillMapId}/sessions/{sessionId}/restart

        @Test
        public void givenValidOwner_whenRestartSession_thenReturnOk() throws Exception {
                mockAuthentication(buildUser(), true);
                given(sessionService.restartSession(eq(SKILL_MAP_ID), eq(1L), any())).willReturn(buildActiveSession());

                MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions/{sessionId}/restart", SKILL_MAP_ID, 1L)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON);

                mockMvc.perform(postRequest)
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.active", is(true)));
        }

        @Test
        public void givenNonOwner_whenRestartSession_thenReturnForbidden() throws Exception {
                mockAuthentication(buildUser(), true);
                given(sessionService.restartSession(eq(SKILL_MAP_ID), eq(1L), any()))
                        .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can restart a session"));

                MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions/{sessionId}/restart", SKILL_MAP_ID, 1L)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON);

                mockMvc.perform(postRequest)
                        .andExpect(status().isForbidden());
        }

        @Test
        public void givenNonExistingSkillMap_whenRestartSession_thenReturnNotFound() throws Exception {
                mockAuthentication(buildUser(), true);
                given(sessionService.restartSession(eq(SKILL_MAP_ID), eq(1L), any()))
                        .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill map not found"));

                MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions/{sessionId}/restart", SKILL_MAP_ID, 1L)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON);

                mockMvc.perform(postRequest)
                        .andExpect(status().isNotFound());
        }

        @Test
        public void givenNonExistingSession_whenRestartSession_thenReturnNotFound() throws Exception {
                mockAuthentication(buildUser(), true);
                given(sessionService.restartSession(eq(SKILL_MAP_ID), eq(1L), any()))
                        .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

                MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions/{sessionId}/restart", SKILL_MAP_ID, 1L)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON);

                mockMvc.perform(postRequest)
                        .andExpect(status().isNotFound());
        }

        @Test
        public void givenSessionAlreadyActive_whenRestartSession_thenReturnConflict() throws Exception {
                mockAuthentication(buildUser(), true);
                given(sessionService.restartSession(eq(SKILL_MAP_ID), eq(1L), any()))
                        .willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Session is already active"));

                MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions/{sessionId}/restart", SKILL_MAP_ID, 1L)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON);

                mockMvc.perform(postRequest)
                        .andExpect(status().isConflict());
        }

        @Test
        public void givenAnotherSessionAlreadyActive_whenRestartSession_thenReturnConflict() throws Exception {
                mockAuthentication(buildUser(), true);
                given(sessionService.restartSession(eq(SKILL_MAP_ID), eq(1L), any()))
                        .willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "A session is already active for this skill map"));

                MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions/{sessionId}/restart", SKILL_MAP_ID, 1L)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON);

                mockMvc.perform(postRequest)
                        .andExpect(status().isConflict());
        }

        @Test
        public void givenSessionNotBelongingToSkillMap_whenRestartSession_thenReturnForbidden() throws Exception {
                mockAuthentication(buildUser(), true);
                given(sessionService.restartSession(eq(SKILL_MAP_ID), eq(1L), any()))
                        .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Session does not belong to this skill map"));

                MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions/{sessionId}/restart", SKILL_MAP_ID, 1L)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON);

                mockMvc.perform(postRequest)
                        .andExpect(status().isForbidden());
        }

        @Test
        public void givenNoAuthorization_whenRestartSession_thenReturnUnauthorized() throws Exception {
                mockAuthentication(buildUser(), false);

                MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions/{sessionId}/restart", SKILL_MAP_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON);

                mockMvc.perform(postRequest)
                        .andExpect(status().isUnauthorized());
        }

        // --- GET /skillmaps/{skillMapId}/sessions ---

        @Test
        public void givenValidOwner_whenGetPastSessions_thenReturnOk() throws Exception {
                mockAuthentication(buildUser(), true);
                given(sessionService.getPastSessions(eq(SKILL_MAP_ID), any())).willReturn(List.of(buildActiveSession()));

                MockHttpServletRequestBuilder getRequest = get("/skillmaps/{skillMapId}/sessions", SKILL_MAP_ID)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON);

                mockMvc.perform(getRequest)
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].active", is(true)));
        }

        @Test
        public void givenNonOwner_whenGetPastSessions_thenReturnForbidden() throws Exception {
                mockAuthentication(buildUser(), true);
                given(sessionService.getPastSessions(eq(SKILL_MAP_ID), any()))
                        .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can view past sessions"));

                MockHttpServletRequestBuilder getRequest = get("/skillmaps/{skillMapId}/sessions", SKILL_MAP_ID)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON);

                mockMvc.perform(getRequest)
                        .andExpect(status().isForbidden());
        }

        @Test
        public void givenNonExistingSkillMap_whenGetPastSessions_thenReturnNotFound() throws Exception {
                mockAuthentication(buildUser(), true);
                given(sessionService.getPastSessions(eq(SKILL_MAP_ID), any()))
                        .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill map not found"));

                MockHttpServletRequestBuilder getRequest = get("/skillmaps/{skillMapId}/sessions", SKILL_MAP_ID)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON);

                mockMvc.perform(getRequest)
                        .andExpect(status().isNotFound());
        }

        @Test
        public void givenNoAuthorization_whenGetPastSessions_thenReturnUnauthorized() throws Exception {
                mockAuthentication(buildUser(), false);

                MockHttpServletRequestBuilder getRequest = get("/skillmaps/{skillMapId}/sessions", SKILL_MAP_ID)
                        .contentType(MediaType.APPLICATION_JSON);

                mockMvc.perform(getRequest)
                        .andExpect(status().isUnauthorized());
        }
}