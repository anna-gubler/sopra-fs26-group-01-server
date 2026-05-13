package ch.uzh.ifi.hase.soprafs26.websocket;

import ch.uzh.ifi.hase.soprafs26.entity.LiveQuestion;
import ch.uzh.ifi.hase.soprafs26.websocket.dto.QuestionsStateMessageDTO;
import ch.uzh.ifi.hase.soprafs26.websocket.dto.RatingUpdatedMessageDTO;
import ch.uzh.ifi.hase.soprafs26.websocket.dto.SessionEndedMessageDTO;
import ch.uzh.ifi.hase.soprafs26.websocket.dto.SessionStartedMessageDTO;
import ch.uzh.ifi.hase.soprafs26.websocket.dto.SpeedUpdatedMessageDTO;
import ch.uzh.ifi.hase.soprafs26.websocket.dto.UnderstandingRequestedMessageDTO;
import ch.uzh.ifi.hase.soprafs26.websocket.dto.UnderstandingUpdatedMessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WebSocketBroadcastServiceTest {

    private static final Long SKILL_MAP_ID = 1L;
    private static final Long SESSION_ID   = 10L;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketBroadcastService broadcastService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }


    // --- broadcastSessionStarted ---

    @Test
    public void broadcastSessionStarted_sendsToCorrectTopic() {
        LocalDateTime startedAt = LocalDateTime.now();

        broadcastService.broadcastSessionStarted(SKILL_MAP_ID, SESSION_ID, startedAt);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate, times(1)).convertAndSend(topicCaptor.capture(), (Object) any());
        assertEquals("/topic/skillmaps/1/live", topicCaptor.getValue());
    }

    @Test
    public void broadcastSessionStarted_sendsCorrectPayload() {
        LocalDateTime startedAt = LocalDateTime.now();

        broadcastService.broadcastSessionStarted(SKILL_MAP_ID, SESSION_ID, startedAt);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(1)).convertAndSend(any(String.class), payloadCaptor.capture());

        SessionStartedMessageDTO payload = (SessionStartedMessageDTO) payloadCaptor.getValue();
        assertEquals("SESSION_STARTED", payload.getType());
        assertEquals(SESSION_ID, payload.getSessionId());
        assertEquals(startedAt, payload.getStartedAt());
    }


    // --- broadcastSessionEnded ---

    @Test
    public void broadcastSessionEnded_sendsToCorrectTopic() {
        LocalDateTime endedAt = LocalDateTime.now();

        broadcastService.broadcastSessionEnded(SKILL_MAP_ID, SESSION_ID, endedAt);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate, times(1)).convertAndSend(topicCaptor.capture(), (Object) any());
        assertEquals("/topic/skillmaps/1/live", topicCaptor.getValue());
    }

    @Test
    public void broadcastSessionEnded_sendsCorrectPayload() {
        LocalDateTime endedAt = LocalDateTime.now();

        broadcastService.broadcastSessionEnded(SKILL_MAP_ID, SESSION_ID, endedAt);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(1)).convertAndSend(any(String.class), payloadCaptor.capture());

        SessionEndedMessageDTO payload = (SessionEndedMessageDTO) payloadCaptor.getValue();
        assertEquals("SESSION_ENDED", payload.getType());
        assertEquals(SESSION_ID, payload.getSessionId());
        assertEquals(endedAt, payload.getEndedAt());
    }


    // --- broadcastUnderstandingRequested ---

    @Test
    public void broadcastUnderstandingRequested_sendsToCorrectTopic() {
        broadcastService.broadcastUnderstandingRequested(SKILL_MAP_ID);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate, times(1)).convertAndSend(topicCaptor.capture(), (Object) any());
        assertEquals("/topic/skillmaps/1/live", topicCaptor.getValue());
    }

    @Test
    public void broadcastUnderstandingRequested_sendsCorrectPayload() {
        broadcastService.broadcastUnderstandingRequested(SKILL_MAP_ID);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(1)).convertAndSend(any(String.class), payloadCaptor.capture());

        UnderstandingRequestedMessageDTO payload = (UnderstandingRequestedMessageDTO) payloadCaptor.getValue();
        assertEquals("UNDERSTANDING_REQUESTED", payload.getType());
    }


    // --- broadcastUnderstandingUpdated ---

    @Test
    public void broadcastUnderstandingUpdated_sendsToCorrectTopic() {
        broadcastService.broadcastUnderstandingUpdated(SKILL_MAP_ID, 75.0, 10);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate, times(1)).convertAndSend(topicCaptor.capture(), (Object) any());
        assertEquals("/topic/skillmaps/1/live", topicCaptor.getValue());
    }

    @Test
    public void broadcastUnderstandingUpdated_sendsCorrectPayload() {
        broadcastService.broadcastUnderstandingUpdated(SKILL_MAP_ID, 75.0, 10);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(1)).convertAndSend(any(String.class), payloadCaptor.capture());

        UnderstandingUpdatedMessageDTO payload = (UnderstandingUpdatedMessageDTO) payloadCaptor.getValue();
        assertEquals("UNDERSTANDING_UPDATED", payload.getType());
        assertEquals(75.0, payload.getAverageRating());
        assertEquals(10, payload.getTotalResponses());
    }


    // --- broadcastRatingUpdate ---

    @Test
    public void broadcastRatingUpdate_sendsToCorrectTopic() {
        broadcastService.broadcastRatingUpdate(SESSION_ID, 42L, 85.5);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate, times(1)).convertAndSend(topicCaptor.capture(), (Object) any());
        assertEquals("/topic/sessions/10/ratings", topicCaptor.getValue());
    }

    @Test
    public void broadcastRatingUpdate_sendsCorrectPayload() {
        broadcastService.broadcastRatingUpdate(SESSION_ID, 42L, 85.5);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(1)).convertAndSend(any(String.class), payloadCaptor.capture());

        RatingUpdatedMessageDTO payload = (RatingUpdatedMessageDTO) payloadCaptor.getValue();
        assertEquals("RATING_UPDATED", payload.getType());
        assertEquals(42L, payload.getSkillId());
        assertEquals(85.5, payload.getAverageRating());
    }


    // --- broadcastQuestionsState ---

    @Test
    public void broadcastQuestionsState_sendsToCorrectTopic() {
        LiveQuestion q = new LiveQuestion();
        broadcastService.broadcastQuestionsState(SESSION_ID, List.of(q));

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate, times(1)).convertAndSend(topicCaptor.capture(), (Object) any());
        assertEquals("/topic/sessions/10/questions", topicCaptor.getValue());
    }

    @Test
    public void broadcastQuestionsState_sendsCorrectPayload() {
        LiveQuestion q = new LiveQuestion();
        q.setText("Test question?");
        broadcastService.broadcastQuestionsState(SESSION_ID, List.of(q));

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(1)).convertAndSend(any(String.class), payloadCaptor.capture());

        QuestionsStateMessageDTO payload = (QuestionsStateMessageDTO) payloadCaptor.getValue();
        assertEquals("QUESTIONS_STATE", payload.getType());
        assertEquals(SESSION_ID, payload.getSessionId());
        assertEquals(1, payload.getQuestions().size());
    }


    // --- broadcastSpeedUpdated ---

    @Test
    public void broadcastSpeedUpdated_sendsToCorrectTopic() {
        broadcastService.broadcastSpeedUpdated(SKILL_MAP_ID, 2, 3, 5);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate, times(1)).convertAndSend(topicCaptor.capture(), (Object) any());
        assertEquals("/topic/skillmaps/1/live", topicCaptor.getValue());
    }

    @Test
    public void broadcastSpeedUpdated_sendsCorrectPayload() {
        broadcastService.broadcastSpeedUpdated(SKILL_MAP_ID, 2, 3, 5);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(1)).convertAndSend(any(String.class), payloadCaptor.capture());

        SpeedUpdatedMessageDTO payload = (SpeedUpdatedMessageDTO) payloadCaptor.getValue();
        assertEquals("SPEED_UPDATED", payload.getType());
        assertEquals(2, payload.getTooFast());
        assertEquals(3, payload.getTooSlow());
        assertEquals(5, payload.getTotalResponses());
    }
}
