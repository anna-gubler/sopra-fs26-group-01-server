package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.CollaborationSession;
import ch.uzh.ifi.hase.soprafs26.entity.User;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private void mockGetUserByToken(boolean success) {
        if (success) {
            given(userService.getUserByToken(any())).willReturn(buildUser());
        }
    }

    // --- POST /skillmaps/{skillMapId}/sessions ---

    @Test
    public void givenValidOwner_whenStartSession_thenReturnCreated() throws Exception {
        mockGetUserByToken(true);
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
        mockGetUserByToken(true);
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
        mockGetUserByToken(true);
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
        mockGetUserByToken(true);
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
        mockGetUserByToken(false);

        MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions", SKILL_MAP_ID)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
    }

    // --- GET /skillmaps/{skillMapId}/sessions/active ---

    @Test
    public void givenActiveSession_whenGetActiveSession_thenReturnOk() throws Exception {
        mockGetUserByToken(true);
        given(sessionService.getActiveSession(eq(SKILL_MAP_ID), any())).willReturn(buildActiveSession());

        MockHttpServletRequestBuilder getRequest = get("/skillmaps/{skillMapId}/sessions/active", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    public void givenNoActiveSession_whenGetActiveSession_thenReturnNotFound() throws Exception {
        mockGetUserByToken(true);
        given(sessionService.getActiveSession(eq(SKILL_MAP_ID), any()))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No active session"));

        MockHttpServletRequestBuilder getRequest = get("/skillmaps/{skillMapId}/sessions/active", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenNonMember_whenGetActiveSession_thenReturnForbidden() throws Exception {
        mockGetUserByToken(true);
        given(sessionService.getActiveSession(eq(SKILL_MAP_ID), any()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a member of this skill map"));

        MockHttpServletRequestBuilder getRequest = get("/skillmaps/{skillMapId}/sessions/active", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenNoAuthorization_whenGetActiveSession_thenReturnUnauthorized() throws Exception {
        mockGetUserByToken(false);

        MockHttpServletRequestBuilder getRequest = get("/skillmaps/{skillMapId}/sessions/active", SKILL_MAP_ID)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized());
    }

    // --- POST /skillmaps/{skillMapId}/sessions/active/end ---

    @Test
    public void givenValidOwner_whenEndSession_thenReturnNoContent() throws Exception {
        mockGetUserByToken(true);

        MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions/active/end", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isNoContent());
    }

    @Test
    public void givenNonOwner_whenEndSession_thenReturnForbidden() throws Exception {
        mockGetUserByToken(true);
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
        mockGetUserByToken(false);

        MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions/active/end", SKILL_MAP_ID)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenNonExistingSkillMap_whenEndSession_thenReturnNotFound() throws Exception {
        mockGetUserByToken(true);
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
        mockGetUserByToken(true);
        willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No active session found"))
                .given(sessionService).endSession(eq(SKILL_MAP_ID), any());

        MockHttpServletRequestBuilder postRequest = post("/skillmaps/{skillMapId}/sessions/active/end", SKILL_MAP_ID)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());
    }
}