package ch.uzh.ifi.hase.soprafs26.websocket;

import java.time.LocalDateTime;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import ch.uzh.ifi.hase.soprafs26.websocket.dto.RatingUpdatedMessageDTO;
import ch.uzh.ifi.hase.soprafs26.websocket.dto.SessionEndedMessageDTO;
import ch.uzh.ifi.hase.soprafs26.websocket.dto.SessionStartedMessageDTO;
import ch.uzh.ifi.hase.soprafs26.entity.LiveQuestion;
import ch.uzh.ifi.hase.soprafs26.websocket.dto.QuestionUpdatedMessageDTO;


@Service
public class WebSocketBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketBroadcastService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastSessionStarted(long skillMapId, long sessionId, LocalDateTime startedAt) {
        String topic = String.format("/topic/skillmaps/%d/live", skillMapId);
        messagingTemplate.convertAndSend(topic, new SessionStartedMessageDTO(sessionId, startedAt));
    }

    public void broadcastSessionEnded(long skillMapId, long sessionId, LocalDateTime endedAt) {
        String topic = String.format("/topic/skillmaps/%d/live", skillMapId);
        messagingTemplate.convertAndSend(topic, new SessionEndedMessageDTO(sessionId, endedAt));
    }

    public void broadcastRatingUpdate(long sessionId, long skillId, double averageRating) {
        String topic = String.format("/topic/sessions/%d/ratings", sessionId);
        messagingTemplate.convertAndSend(topic, new RatingUpdatedMessageDTO(skillId, averageRating));
    }

    public void broadcastQuestionUpdate(long sessionId, LiveQuestion question) {
        String topic = String.format("/topic/sessions/%d/questions", sessionId);
        messagingTemplate.convertAndSend(topic, new QuestionUpdatedMessageDTO(
                question.getId(),
                question.getSessionId(),
                question.getText(),
                question.getUpvoteCount(),
                question.getIsAddressed()
        ));
    }
}