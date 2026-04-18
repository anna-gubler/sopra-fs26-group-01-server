package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.LiveQuestion;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.LiveQuestionService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.websocket.WebSocketBroadcastService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LiveQuestionController.class)
@AutoConfigureMockMvc(addFilters = false)
class LiveQuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LiveQuestionService liveQuestionService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private WebSocketBroadcastService webSocketBroadcastService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User dummyUser;
    private LiveQuestion dummyQuestion;

    @BeforeEach
    void setup() {
        dummyUser = new User();
        dummyUser.setId(1L);
        dummyUser.setToken("valid-token");
        given(userService.getUserByToken(any())).willReturn(dummyUser);

        dummyQuestion = new LiveQuestion();
        dummyQuestion.setId(10L);
        dummyQuestion.setSessionId(1L);
        dummyQuestion.setSkillId(2L);
        dummyQuestion.setSkillName("Skill A");
        dummyQuestion.setText("What is polymorphism?");
        dummyQuestion.setUpvoteCount(0);
        dummyQuestion.setIsAddressed(false);
        dummyQuestion.setPostedAt(LocalDateTime.now());
    }

    // 801: GET /sessions/{sessionId}/questions
    @Test
    void getQuestions_validSession_returnsOk() throws Exception {
        given(liveQuestionService.getQuestionsBySession(eq(1L))).willReturn(List.of(dummyQuestion));

        mockMvc.perform(get("/sessions/1/questions")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].text").value("What is polymorphism?"));
    }

    // 802: POST /sessions/{sessionId}/questions
    @Test
    void postQuestion_validInput_returnsCreated() throws Exception {
        given(liveQuestionService.postQuestion(eq(1L), eq(2L), any(), eq("What is polymorphism?")))
                .willReturn(dummyQuestion);

        Map<String, Object> body = Map.of(
                "skillId", 2,
                "skillName", "Skill A",
                "text", "What is polymorphism?"
        );

        mockMvc.perform(post("/sessions/1/questions")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("What is polymorphism?"));
    }

    // 803: DELETE /questions/{questionId}
    @Test
    void deleteQuestion_validId_returnsNoContent() throws Exception {
        doNothing().when(liveQuestionService).deleteQuestion(eq(10L));

        mockMvc.perform(delete("/questions/10")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNoContent());
    }

    // 804: POST /questions/{questionId}/upvotes
    @Test
    void upvoteQuestion_validRequest_returnsCreated() throws Exception {
        given(liveQuestionService.upvoteQuestion(eq(10L), eq(1L))).willReturn(dummyQuestion);

        mockMvc.perform(post("/questions/10/upvotes")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isCreated());
    }

    // 805: DELETE /questions/{questionId}/upvotes/me
    @Test
    void removeUpvote_validRequest_returnsNoContent() throws Exception {
        doNothing().when(liveQuestionService).removeUpvote(eq(10L), eq(1L));

        mockMvc.perform(delete("/questions/10/upvotes/me")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNoContent());
    }

    // 806: POST /questions/{questionId}/mark-addressed
    @Test
    void markAddressed_validRequest_returnsOk() throws Exception {
        dummyQuestion.setIsAddressed(true);
        given(liveQuestionService.markAddressed(eq(10L), eq(1L))).willReturn(dummyQuestion);

        mockMvc.perform(post("/questions/10/mark-addressed")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAddressed").value(true));
    }
}