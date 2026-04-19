package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;

import ch.uzh.ifi.hase.soprafs26.constant.SpeedFeedback;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SpeedFeedbackPutDTO;
import ch.uzh.ifi.hase.soprafs26.service.SpeedFeedbackService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpeedFeedbackController.class)
public class SpeedFeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpeedFeedbackService speedFeedbackService;

    @MockitoBean
    private UserService userService;

    private static final Long SESSION_ID = 10L;
    private static final String TOKEN = "Bearer test-token";

    private User buildUser() {
        User user = new User();
        user.setId(100L);
        return user;
    }

    private void mockAuthentication(User user, boolean success) {
        if (success) {
            given(userService.getUserByToken(any())).willReturn(user);
        }
    }

    private SpeedFeedbackPutDTO buildRequestBody(SpeedFeedback feedback) {
        SpeedFeedbackPutDTO dto = new SpeedFeedbackPutDTO();
        dto.setFeedback(feedback);
        return dto;
    }

    // --- PUT /sessions/{sessionId}/speed ---

    @Test
    public void givenValidFeedback_whenSubmitSpeed_thenReturnNoContent() throws Exception {
        mockAuthentication(buildUser(), true);

        mockMvc.perform(put("/sessions/{sessionId}/speed", SESSION_ID)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(buildRequestBody(SpeedFeedback.TOO_FAST))))
                .andExpect(status().isNoContent());
    }

    @Test
    public void givenSessionNotActive_whenSubmitSpeed_thenReturnForbidden() throws Exception {
        mockAuthentication(buildUser(), true);
        willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Session is not active"))
                .given(speedFeedbackService).submitFeedback(eq(SESSION_ID), any(), any());

        mockMvc.perform(put("/sessions/{sessionId}/speed", SESSION_ID)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(buildRequestBody(SpeedFeedback.TOO_FAST))))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenSessionNotFound_whenSubmitSpeed_thenReturnNotFound() throws Exception {
        mockAuthentication(buildUser(), true);
        willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"))
                .given(speedFeedbackService).submitFeedback(eq(SESSION_ID), any(), any());

        mockMvc.perform(put("/sessions/{sessionId}/speed", SESSION_ID)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(buildRequestBody(SpeedFeedback.TOO_SLOW))))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenNoAuthorization_whenSubmitSpeed_thenReturnUnauthorized() throws Exception {
        mockAuthentication(buildUser(), false);

        mockMvc.perform(put("/sessions/{sessionId}/speed", SESSION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(buildRequestBody(SpeedFeedback.OK))))
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