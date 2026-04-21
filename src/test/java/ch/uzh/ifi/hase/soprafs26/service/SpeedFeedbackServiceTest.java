package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.SpeedFeedback;
import ch.uzh.ifi.hase.soprafs26.entity.CollaborationSession;
import ch.uzh.ifi.hase.soprafs26.repository.CollaborationSessionRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SpeedFeedbackGetDTO;
import ch.uzh.ifi.hase.soprafs26.websocket.WebSocketBroadcastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SpeedFeedbackServiceTest {

    private static final Long SESSION_ID = 10L;
    private static final Long SKILL_MAP_ID = 1L;
    private static final Long USER_ID = 100L;
    private static final Long OTHER_USER_ID = 200L;

    @Mock
    private CollaborationSessionRepository collaborationSessionRepository;

    @Mock
    private WebSocketBroadcastService webSocketBroadcastService;

    @InjectMocks
    private SpeedFeedbackService speedFeedbackService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private CollaborationSession buildActiveSession() {
        CollaborationSession session = new CollaborationSession();
        ReflectionTestUtils.setField(session, "id", SESSION_ID);
        session.setSkillMapId(SKILL_MAP_ID);
        session.setActive(true);
        return session;
    }

    private CollaborationSession buildInactiveSession() {
        CollaborationSession session = new CollaborationSession();
        ReflectionTestUtils.setField(session, "id", SESSION_ID);
        session.setSkillMapId(SKILL_MAP_ID);
        session.setActive(false);
        return session;
    }

    // --- submitFeedback ---

    @Test
    public void submitFeedback_newVote_broadcastsCorrectCounts() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(buildActiveSession()));

        speedFeedbackService.submitFeedback(SESSION_ID, USER_ID, SpeedFeedback.TOO_FAST);

        Mockito.verify(webSocketBroadcastService, Mockito.times(1))
                .broadcastSpeedUpdated(SKILL_MAP_ID, 1, 0, 1);
    }

    @Test
    public void submitFeedback_multipleUsers_broadcastsCorrectCounts() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(buildActiveSession()));

        speedFeedbackService.submitFeedback(SESSION_ID, USER_ID, SpeedFeedback.TOO_FAST);
        speedFeedbackService.submitFeedback(SESSION_ID, OTHER_USER_ID, SpeedFeedback.TOO_SLOW);

        Mockito.verify(webSocketBroadcastService, Mockito.times(1))
                .broadcastSpeedUpdated(SKILL_MAP_ID, 1, 1, 2);
    }

    @Test
    public void submitFeedback_userUpdatesVote_countsReflectUpdate() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(buildActiveSession()));

        speedFeedbackService.submitFeedback(SESSION_ID, USER_ID, SpeedFeedback.TOO_FAST);
        speedFeedbackService.submitFeedback(SESSION_ID, USER_ID, SpeedFeedback.TOO_SLOW);

        Mockito.verify(webSocketBroadcastService, Mockito.times(1))
                .broadcastSpeedUpdated(SKILL_MAP_ID, 0, 1, 1);
    }

    @Test
    public void submitFeedback_okVote_countsInTotalResponses() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(buildActiveSession()));

        speedFeedbackService.submitFeedback(SESSION_ID, USER_ID, SpeedFeedback.OK);

        Mockito.verify(webSocketBroadcastService, Mockito.times(1))
                .broadcastSpeedUpdated(SKILL_MAP_ID, 0, 0, 1);
    }

    @Test
    public void submitFeedback_sessionNotActive_throwsForbidden() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(buildInactiveSession()));

        assertThrows(ResponseStatusException.class,
                () -> speedFeedbackService.submitFeedback(SESSION_ID, USER_ID, SpeedFeedback.TOO_FAST));
    }

    @Test
    public void submitFeedback_sessionNotFound_throwsNotFound() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> speedFeedbackService.submitFeedback(SESSION_ID, USER_ID, SpeedFeedback.TOO_FAST));
    }

    // --- getCounts ---

    @Test
    public void getCounts_noVotes_returnsAllZeros() {
        SpeedFeedbackGetDTO dto = speedFeedbackService.getCounts(SESSION_ID);

        assertEquals(0, dto.getTooFast());
        assertEquals(0, dto.getTooSlow());
        assertEquals(0, dto.getTotalResponses());
    }

    @Test
    public void getCounts_mixedVotes_returnsCorrectCounts() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(buildActiveSession()));

        speedFeedbackService.submitFeedback(SESSION_ID, USER_ID, SpeedFeedback.TOO_FAST);
        speedFeedbackService.submitFeedback(SESSION_ID, OTHER_USER_ID, SpeedFeedback.TOO_SLOW);

        SpeedFeedbackGetDTO dto = speedFeedbackService.getCounts(SESSION_ID);

        assertEquals(1, dto.getTooFast());
        assertEquals(1, dto.getTooSlow());
        assertEquals(2, dto.getTotalResponses());
    }

    @Test
    public void getCounts_okVote_countedInTotalOnly() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(buildActiveSession()));

        speedFeedbackService.submitFeedback(SESSION_ID, USER_ID, SpeedFeedback.OK);

        SpeedFeedbackGetDTO dto = speedFeedbackService.getCounts(SESSION_ID);

        assertEquals(0, dto.getTooFast());
        assertEquals(0, dto.getTooSlow());
        assertEquals(1, dto.getTotalResponses());
    }

    @Test
    public void getCounts_afterClear_returnsAllZeros() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(buildActiveSession()));

        speedFeedbackService.submitFeedback(SESSION_ID, USER_ID, SpeedFeedback.TOO_FAST);
        speedFeedbackService.clearSession(SESSION_ID);

        SpeedFeedbackGetDTO dto = speedFeedbackService.getCounts(SESSION_ID);

        assertEquals(0, dto.getTooFast());
        assertEquals(0, dto.getTooSlow());
        assertEquals(0, dto.getTotalResponses());
    }

    // --- clearSession ---

    @Test
    public void clearSession_afterVotesSubmitted_broadcastsEmptyCounts() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(buildActiveSession()));

        speedFeedbackService.submitFeedback(SESSION_ID, USER_ID, SpeedFeedback.TOO_FAST);
        speedFeedbackService.clearSession(SESSION_ID);
        speedFeedbackService.submitFeedback(SESSION_ID, USER_ID, SpeedFeedback.TOO_FAST);

        // after clear, next submission should broadcast as if starting fresh
        Mockito.verify(webSocketBroadcastService, Mockito.times(2))
                .broadcastSpeedUpdated(SKILL_MAP_ID, 1, 0, 1);
    }
}