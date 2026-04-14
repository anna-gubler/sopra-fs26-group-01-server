package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.UnderstandingRating;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.UnderstandingRatingService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UnderstandingRatingController.class)
public class UnderstandingRatingControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UnderstandingRatingService ratingService;
    // AuthInterceptor runs before every protected request and calls userService.getUserByToken().
    // Stubbing it in each test makes the interceptor pass and sets the resolved user as a request
    // attribute, which the controller then reads. UserService itself is not used by the controller directly.
    @MockitoBean private UserService userService;

    private static final Long SESSION_ID = 1L;
    private static final Long SKILL_ID = 2L;
    private static final String TOKEN = "Bearer test-token";

    private User buildUser() {
        User u = new User(); u.setId(10L); return u;
    }

    private UnderstandingRating buildRating() {
        UnderstandingRating r = new UnderstandingRating();
        r.setRating(80);
        r.setSubmittedAt(LocalDateTime.now());
        return r;
    }

    // --- PUT /sessions/{sessionId}/skills/{skillId}/rating ---

    @Test
    public void submitRating_validInput_returnsOk() throws Exception {
        given(userService.getUserByToken(any())).willReturn(buildUser());
        given(ratingService.submitRating(eq(SESSION_ID), eq(SKILL_ID), any(), any()))
                .willReturn(buildRating());

        mockMvc.perform(put("/sessions/{sessionId}/skills/{skillId}/rating", SESSION_ID, SKILL_ID)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\": 80}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(80)));
    }

    @Test
    public void submitRating_invalidRating_returnsBadRequest() throws Exception {
        given(userService.getUserByToken(any())).willReturn(buildUser());
        given(ratingService.submitRating(any(), any(), any(), any()))
                .willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));

        mockMvc.perform(put("/sessions/{sessionId}/skills/{skillId}/rating", SESSION_ID, SKILL_ID)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\": 150}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void submitRating_sessionNotActive_returnsForbidden() throws Exception {
        given(userService.getUserByToken(any())).willReturn(buildUser());
        given(ratingService.submitRating(any(), any(), any(), any()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(put("/sessions/{sessionId}/skills/{skillId}/rating", SESSION_ID, SKILL_ID)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\": 50}"))
                .andExpect(status().isForbidden());
    }

    // --- GET /sessions/{sessionId}/skills/{skillId}/ratings ---

    @Test
    public void getRatingsBySkill_ownerAccess_returnsOk() throws Exception {
        given(userService.getUserByToken(any())).willReturn(buildUser());
        given(ratingService.getRatingsBySkill(eq(SESSION_ID), eq(SKILL_ID), any()))
                .willReturn(List.of(buildRating()));

        mockMvc.perform(get("/sessions/{sessionId}/skills/{skillId}/ratings", SESSION_ID, SKILL_ID)
                .header("Authorization", TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    public void getRatingsBySkill_notOwner_returnsForbidden() throws Exception {
        given(userService.getUserByToken(any())).willReturn(buildUser());
        given(ratingService.getRatingsBySkill(any(), any(), any()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(get("/sessions/{sessionId}/skills/{skillId}/ratings", SESSION_ID, SKILL_ID)
                .header("Authorization", TOKEN))
                .andExpect(status().isForbidden());
    }

    // --- GET /sessions/{sessionId}/ratings ---

    @Test
    public void getRatingsBySession_ownerAccess_returnsOk() throws Exception {
        given(userService.getUserByToken(any())).willReturn(buildUser());
        given(ratingService.getRatingsBySession(eq(SESSION_ID), any()))
                .willReturn(List.of(buildRating()));

        mockMvc.perform(get("/sessions/{sessionId}/ratings", SESSION_ID)
                .header("Authorization", TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    public void getRatingsBySession_notOwner_returnsForbidden() throws Exception {
        given(userService.getUserByToken(any())).willReturn(buildUser());
        given(ratingService.getRatingsBySession(any(), any()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(get("/sessions/{sessionId}/ratings", SESSION_ID)
                .header("Authorization", TOKEN))
                .andExpect(status().isForbidden());
    }
}