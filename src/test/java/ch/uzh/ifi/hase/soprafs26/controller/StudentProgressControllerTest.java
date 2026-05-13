package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.StudentProgress;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.StudentProgressService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentProgressController.class)
@AutoConfigureMockMvc(addFilters = false)
class StudentProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentProgressService studentProgressService;

    @MockitoBean
    private UserService userService;

    private User dummyUser;

    @BeforeEach
    void setupMocks() {
        dummyUser = new User();
        dummyUser.setId(1L);
        given(userService.getUserByToken(any())).willReturn(dummyUser);
    }

    @Test
    void setProgress_validRequest_returns200WithDTO() throws Exception {
        StudentProgress progress = new StudentProgress();
        progress.setSkillId(5L);
        progress.setUserId(1L);
        progress.setIsUnderstood(true);

        given(studentProgressService.setProgress(eq(5L), any(User.class), eq(true)))
            .willReturn(progress);

        mockMvc.perform(put("/skills/5/progress/me")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"isUnderstood\": true }"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.skillId", is(5)))
            .andExpect(jsonPath("$.userId", is(1)))
            .andExpect(jsonPath("$.isUnderstood", is(true)));
    }

    @Test
    void setProgress_invalidToken_returns401() throws Exception {
        given(userService.getUserByToken("invalid-token"))
            .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

        mockMvc.perform(put("/skills/5/progress/me")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"isUnderstood\": true }"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void setProgress_skillNotFound_returns404() throws Exception {
        given(studentProgressService.setProgress(eq(99L), any(User.class), any(Boolean.class)))
            .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found"));

        mockMvc.perform(put("/skills/99/progress/me")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"isUnderstood\": true }"))
            .andExpect(status().isNotFound());
    }

    @Test
    void setProgress_notMemberOrOwner_returns403() throws Exception {
        given(studentProgressService.setProgress(eq(5L), any(User.class), any(Boolean.class)))
            .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));

        mockMvc.perform(put("/skills/5/progress/me")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"isUnderstood\": true }"))
            .andExpect(status().isForbidden());
    }
}
