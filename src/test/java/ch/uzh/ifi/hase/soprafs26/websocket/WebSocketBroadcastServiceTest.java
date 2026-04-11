package ch.uzh.ifi.hase.soprafs26.websocket;

import ch.uzh.ifi.hase.soprafs26.websocket.dto.SessionEndedMessageDTO;
import ch.uzh.ifi.hase.soprafs26.websocket.dto.SessionStartedMessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

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
}
