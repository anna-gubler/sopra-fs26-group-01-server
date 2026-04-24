package ch.uzh.ifi.hase.soprafs26.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.entity.StudentProgress;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.interceptor.AuthInterceptor;
import ch.uzh.ifi.hase.soprafs26.service.StudentProgressService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import java.time.LocalDateTime;
import java.util.Map;

@WebMvcTest(StudentProgressController.class)
@AutoConfigureMockMvc(addFilters = false)
class StudentProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentProgressService studentProgressService;

    @MockitoBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User dummyUser;
    private StudentProgress dummyProgress;

    @BeforeEach
    void setup() throws Exception {
        dummyUser = new User();
        dummyUser.setId(99L);
        dummyUser.setToken("valid-token");

        given(userService.getUserByToken(any())).willReturn(dummyUser);

        dummyProgress = new StudentProgress();
        dummyProgress.setId(1L);
        dummyProgress.setUserId(99L);
        dummyProgress.setSkillId(10L);
        dummyProgress.setIsUnderstood(true);
        dummyProgress.setIsUnderstandingDate(LocalDateTime.now());
    }

    @Test
    void updateProgress_validRequest_returns200() throws Exception {
        given(studentProgressService.updateProgress(eq(10L), eq(true), any(User.class)))
            .willReturn(dummyProgress);
        mockMvc.perform(put("/skills/10/progress/me")
                .header("Authorization", "Bearer valid-token")  // needs Bearer prefix
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("isUnderstood", true))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isUnderstood").value(true))
            .andExpect(jsonPath("$.userId").value(99))
            .andExpect(jsonPath("$.skillId").value(10));
    }

    @Test
    void updateProgress_invalidToken_returns401() throws Exception {
        given(userService.getUserByToken(any()))
            .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        mockMvc.perform(put("/skills/10/progress/me")
                .header("Authorization", "Bearer bad-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("isUnderstood", true))))
            .andExpect(status().isUnauthorized());
    }
}