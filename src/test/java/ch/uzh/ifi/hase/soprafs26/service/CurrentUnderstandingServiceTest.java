package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.CollaborationSession;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.repository.CollaborationSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapRepository;
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

public class CurrentUnderstandingServiceTest {

    private static final Long SESSION_ID = 10L;
    private static final Long SKILL_MAP_ID = 1L;
    private static final Long OWNER_ID = 50L;
    private static final Long USER_ID = 100L;
    private static final Long OTHER_USER_ID = 200L;

    @Mock
    private CollaborationSessionRepository collaborationSessionRepository;

    @Mock
    private SkillMapRepository skillMapRepository;

    @Mock
    private WebSocketBroadcastService webSocketBroadcastService;

    @InjectMocks
    private CurrentUnderstandingService currentUnderstandingService;

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

    private SkillMap buildSkillMap(Long ownerId) {
        SkillMap skillMap = new SkillMap();
        skillMap.setOwnerId(ownerId);
        return skillMap;
    }

    // --- requestFeedback ---

    @Test
    public void requestFeedback_asOwner_broadcastsUnderstandingRequested() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(buildActiveSession()));
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID))
                .thenReturn(Optional.of(buildSkillMap(OWNER_ID)));

        currentUnderstandingService.requestFeedback(SESSION_ID, OWNER_ID);

        Mockito.verify(webSocketBroadcastService, Mockito.times(1))
                .broadcastUnderstandingRequested(SKILL_MAP_ID);
    }

    @Test
    public void requestFeedback_clearsExistingVotes() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(buildActiveSession()));
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID))
                .thenReturn(Optional.of(buildSkillMap(OWNER_ID)));

        // first round: student votes
        currentUnderstandingService.submitRating(SESSION_ID, USER_ID, 80);
        // professor starts new round
        currentUnderstandingService.requestFeedback(SESSION_ID, OWNER_ID);
        // student votes again in new round
        currentUnderstandingService.submitRating(SESSION_ID, USER_ID, 40);

        // broadcast after second submitRating should reflect only the new vote
        Mockito.verify(webSocketBroadcastService, Mockito.times(1))
                .broadcastUnderstandingUpdated(SKILL_MAP_ID, 40.0, 1);
    }

    @Test
    public void requestFeedback_sessionNotFound_throwsNotFound() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> currentUnderstandingService.requestFeedback(SESSION_ID, OWNER_ID));
    }

    @Test
    public void requestFeedback_sessionNotActive_throwsForbidden() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(buildInactiveSession()));

        assertThrows(ResponseStatusException.class,
                () -> currentUnderstandingService.requestFeedback(SESSION_ID, OWNER_ID));
    }

    @Test
    public void requestFeedback_notOwner_throwsForbidden() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(buildActiveSession()));
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID))
                .thenReturn(Optional.of(buildSkillMap(OWNER_ID)));

        assertThrows(ResponseStatusException.class,
                () -> currentUnderstandingService.requestFeedback(SESSION_ID, USER_ID));
    }

    @Test
    public void requestFeedback_skillMapNotFound_throwsNotFound() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(buildActiveSession()));
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> currentUnderstandingService.requestFeedback(SESSION_ID, OWNER_ID));
    }

    // --- submitRating ---

    @Test
    public void submitRating_singleVote_broadcastsCorrectAverage() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(buildActiveSession()));

        currentUnderstandingService.submitRating(SESSION_ID, USER_ID, 80);

        Mockito.verify(webSocketBroadcastService, Mockito.times(1))
                .broadcastUnderstandingUpdated(SKILL_MAP_ID, 80.0, 1);
    }

    @Test
    public void submitRating_multipleUsers_broadcastsCorrectAverage() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(buildActiveSession()));

        currentUnderstandingService.submitRating(SESSION_ID, USER_ID, 60);
        currentUnderstandingService.submitRating(SESSION_ID, OTHER_USER_ID, 100);

        Mockito.verify(webSocketBroadcastService, Mockito.times(1))
                .broadcastUnderstandingUpdated(SKILL_MAP_ID, 80.0, 2);
    }

    @Test
    public void submitRating_userUpdatesVote_averageReflectsUpdate() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(buildActiveSession()));

        currentUnderstandingService.submitRating(SESSION_ID, USER_ID, 100);
        currentUnderstandingService.submitRating(SESSION_ID, USER_ID, 0);

        Mockito.verify(webSocketBroadcastService, Mockito.times(1))
                .broadcastUnderstandingUpdated(SKILL_MAP_ID, 0.0, 1);
    }

    @Test
    public void submitRating_sessionNotFound_throwsNotFound() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> currentUnderstandingService.submitRating(SESSION_ID, USER_ID, 50));
    }

    @Test
    public void submitRating_sessionNotActive_throwsForbidden() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(buildInactiveSession()));

        assertThrows(ResponseStatusException.class,
                () -> currentUnderstandingService.submitRating(SESSION_ID, USER_ID, 50));
    }

    // --- clearSession ---

    @Test
    public void clearSession_afterVotesSubmitted_nextSubmitStartsFresh() {
        Mockito.when(collaborationSessionRepository.findById(SESSION_ID))
                .thenReturn(Optional.of(buildActiveSession()));

        currentUnderstandingService.submitRating(SESSION_ID, USER_ID, 80);
        currentUnderstandingService.submitRating(SESSION_ID, OTHER_USER_ID, 60);
        currentUnderstandingService.clearSession(SESSION_ID);
        currentUnderstandingService.submitRating(SESSION_ID, USER_ID, 80);

        // if clear didn't work, step 4 would broadcast (70.0, 2) instead of (80.0, 1)
        Mockito.verify(webSocketBroadcastService, Mockito.times(2))
                .broadcastUnderstandingUpdated(SKILL_MAP_ID, 80.0, 1);
    }
}
