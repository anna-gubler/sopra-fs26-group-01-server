package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.CollaborationSession;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.CollaborationSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapMembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapRepository;
import ch.uzh.ifi.hase.soprafs26.websocket.WebSocketBroadcastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CollaborationSessionServiceTest {

    private static final Long SKILL_MAP_ID = 1L;
    private static final Long SESSION_ID = 10L;
    private static final Long OWNER_ID = 100L;
    private static final Long OTHER_ID = 200L;

    @Mock
    private CollaborationSessionRepository sessionRepository;

    @Mock
    private SkillMapRepository skillMapRepository;

    @Mock
    private WebSocketBroadcastService broadcastService;

    @Mock
    private SkillMapMembershipRepository membershipRepository;

    @InjectMocks
    private CollaborationSessionService sessionService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private User buildOwner() {
        User user = new User();
        user.setId(OWNER_ID);
        return user;
    }

    private User buildOtherUser() {
        User user = new User();
        user.setId(OTHER_ID);
        return user;
    }

    private SkillMap buildSkillMap() {
        SkillMap skillMap = new SkillMap();
        skillMap.setId(SKILL_MAP_ID);
        skillMap.setOwnerId(OWNER_ID);
        return skillMap;
    }

    private CollaborationSession buildActiveSession() {
        CollaborationSession session = new CollaborationSession();
        ReflectionTestUtils.setField(session, "id", SESSION_ID);
        session.setSkillMapId(SKILL_MAP_ID);
        session.setStartedAt(LocalDateTime.now());
        session.setActive(true);
        return session;
    }

    // --- startSession ---

    @Test
    public void startSession_validInput_success() {
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(sessionRepository.existsBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID)).thenReturn(false);
        Mockito.when(sessionRepository.save(Mockito.any())).thenReturn(buildActiveSession());

        sessionService.startSession(SKILL_MAP_ID, buildOwner());

        ArgumentCaptor<CollaborationSession> captor = ArgumentCaptor.forClass(CollaborationSession.class);
        Mockito.verify(sessionRepository, Mockito.times(1)).save(captor.capture());
        assertTrue(captor.getValue().isActive());
        assertEquals(SKILL_MAP_ID, captor.getValue().getSkillMapId());
        assertNotNull(captor.getValue().getStartedAt());

        Mockito.verify(broadcastService, Mockito.times(1))
                .broadcastSessionStarted(Mockito.eq(SKILL_MAP_ID), Mockito.anyLong(), Mockito.any());
    }

    @Test
    public void startSession_notOwner_throwsForbidden() {
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));

        assertThrows(ResponseStatusException.class,
                () -> sessionService.startSession(SKILL_MAP_ID, buildOtherUser()));
    }

    @Test
    public void startSession_skillMapNotFound_throwsNotFound() {
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> sessionService.startSession(SKILL_MAP_ID, buildOwner()));
    }

    @Test
    public void startSession_alreadyActive_throwsConflict() {
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(sessionRepository.existsBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID)).thenReturn(true);

        assertThrows(ResponseStatusException.class,
                () -> sessionService.startSession(SKILL_MAP_ID, buildOwner()));
    }

    // --- endSession ---

    @Test
    public void endSession_validInput_success() {
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(sessionRepository.findBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID))
                .thenReturn(Optional.of(buildActiveSession()));
        Mockito.when(sessionRepository.save(Mockito.any())).thenReturn(buildActiveSession());

        sessionService.endSession(SKILL_MAP_ID, buildOwner());

        ArgumentCaptor<CollaborationSession> captor = ArgumentCaptor.forClass(CollaborationSession.class);
        Mockito.verify(sessionRepository, Mockito.times(1)).save(captor.capture());
        assertFalse(captor.getValue().isActive());
        assertNotNull(captor.getValue().getEndedAt());

        Mockito.verify(broadcastService, Mockito.times(1))
                .broadcastSessionEnded(Mockito.eq(SKILL_MAP_ID), Mockito.anyLong(), Mockito.any());
    }

    @Test
    public void endSession_notOwner_throwsForbidden() {
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));

        assertThrows(ResponseStatusException.class,
                () -> sessionService.endSession(SKILL_MAP_ID, buildOtherUser()));
    }

    @Test
    public void endSession_noActiveSession_throwsNotFound() {
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(sessionRepository.findBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> sessionService.endSession(SKILL_MAP_ID, buildOwner()));
    }

    // --- getActiveSession ---

    // --- getActiveSession ---

    @Test
    public void getActiveSession_memberAndSessionExists_returnsSession() {
        Mockito.when(membershipRepository.existsBySkillMapIdAndUserId(SKILL_MAP_ID, OWNER_ID)).thenReturn(true);
        Mockito.when(sessionRepository.findBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID))
                .thenReturn(Optional.of(buildActiveSession()));

        CollaborationSession result = sessionService.getActiveSession(SKILL_MAP_ID, buildOwner());

        assertTrue(result.isActive());
        assertEquals(SKILL_MAP_ID, result.getSkillMapId());
    }

    @Test
    public void getActiveSession_notMember_throwsForbidden() {
        Mockito.when(membershipRepository.existsBySkillMapIdAndUserId(SKILL_MAP_ID, OWNER_ID)).thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> sessionService.getActiveSession(SKILL_MAP_ID, buildOwner()));
    }

    @Test
    public void getActiveSession_noActiveSession_throwsNotFound() {
        Mockito.when(membershipRepository.existsBySkillMapIdAndUserId(SKILL_MAP_ID, OWNER_ID)).thenReturn(true);
        Mockito.when(sessionRepository.findBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> sessionService.getActiveSession(SKILL_MAP_ID, buildOwner()));
    }
}
