package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CurrentUnderstandingPutDTO;
import ch.uzh.ifi.hase.soprafs26.service.CurrentUnderstandingService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CurrentUnderstandingController.class)
public class CurrentUnderstandingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CurrentUnderstandingService currentUnderstandingService;

    @MockitoBean
    private UserService userService;

    private static final Long SESSION_ID = 10L;
    private static final String TOKEN = "Bearer test-token";

    private User buildUser(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private void mockAuthentication(User user) {
        given(userService.getUserByToken(any())).willReturn(user);
    }

    private CurrentUnderstandingPutDTO buildRatingBody(int rating) {
        CurrentUnderstandingPutDTO dto = new CurrentUnderstandingPutDTO();
        dto.setRating(rating);
        return dto;
    }

    // --- POST /sessions/{sessionId}/understanding/request ---

    @Test
    public void givenOwner_whenRequestUnderstanding_thenReturnNoContent() throws Exception {
        mockAuthentication(buildUser(50L));

        mockMvc.perform(post("/sessions/{sessionId}/understanding/request", SESSION_ID)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void givenNotOwner_whenRequestUnderstanding_thenReturnForbidden() throws Exception {
        mockAuthentication(buildUser(99L));
        willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can request understanding"))
                .given(currentUnderstandingService).requestFeedback(eq(SESSION_ID), any());

        mockMvc.perform(post("/sessions/{sessionId}/understanding/request", SESSION_ID)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenSessionNotActive_whenRequestUnderstanding_thenReturnForbidden() throws Exception {
        mockAuthentication(buildUser(50L));
        willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Session is not active"))
                .given(currentUnderstandingService).requestFeedback(eq(SESSION_ID), any());

        mockMvc.perform(post("/sessions/{sessionId}/understanding/request", SESSION_ID)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenSessionNotFound_whenRequestUnderstanding_thenReturnNotFound() throws Exception {
        mockAuthentication(buildUser(50L));
        willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"))
                .given(currentUnderstandingService).requestFeedback(eq(SESSION_ID), any());

        mockMvc.perform(post("/sessions/{sessionId}/understanding/request", SESSION_ID)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenNoAuthorization_whenRequestUnderstanding_thenReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/sessions/{sessionId}/understanding/request", SESSION_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // --- PUT /sessions/{sessionId}/understanding ---

    @Test
    public void givenValidRating_whenSubmitUnderstanding_thenReturnNoContent() throws Exception {
        mockAuthentication(buildUser(100L));

        mockMvc.perform(put("/sessions/{sessionId}/understanding", SESSION_ID)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(buildRatingBody(75))))
                .andExpect(status().isNoContent());
    }

    @Test
    public void givenSessionNotActive_whenSubmitUnderstanding_thenReturnForbidden() throws Exception {
        mockAuthentication(buildUser(100L));
        willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Session is not active"))
                .given(currentUnderstandingService).submitRating(eq(SESSION_ID), any(), any());

        mockMvc.perform(put("/sessions/{sessionId}/understanding", SESSION_ID)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(buildRatingBody(50))))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenSessionNotFound_whenSubmitUnderstanding_thenReturnNotFound() throws Exception {
        mockAuthentication(buildUser(100L));
        willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"))
                .given(currentUnderstandingService).submitRating(eq(SESSION_ID), any(), any());

        mockMvc.perform(put("/sessions/{sessionId}/understanding", SESSION_ID)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(buildRatingBody(50))))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenNoAuthorization_whenSubmitUnderstanding_thenReturnUnauthorized() throws Exception {
        mockMvc.perform(put("/sessions/{sessionId}/understanding", SESSION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(buildRatingBody(50))))
                .andExpect(status().isUnauthorized());
    }

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JacksonException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }
}
