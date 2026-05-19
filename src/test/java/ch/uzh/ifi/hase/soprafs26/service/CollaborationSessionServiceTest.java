package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.CollaborationSession;
import ch.uzh.ifi.hase.soprafs26.entity.Quiz;
import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.CollaborationSessionRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.DashboardQuizSummaryDTO;
import ch.uzh.ifi.hase.soprafs26.repository.QuizAttemptRepository;
import ch.uzh.ifi.hase.soprafs26.repository.QuizRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapMembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillRepository;
import ch.uzh.ifi.hase.soprafs26.websocket.WebSocketBroadcastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
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
    private SpeedFeedbackService speedFeedbackService;

    @Mock
    private CurrentUnderstandingService currentUnderstandingService;

    @Mock
    private SkillMapMembershipRepository membershipRepository;

    @InjectMocks
    private CollaborationSessionService sessionService;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

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

    @Test
    public void startSession_privateMap_throwsForbidden() {
        SkillMap privateMap = buildSkillMap();
        privateMap.setIsPublic(false);
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(privateMap));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> sessionService.startSession(SKILL_MAP_ID, buildOwner()));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    public void startSession_publicMap_proceedsNormally() {
        SkillMap publicMap = buildSkillMap();
        publicMap.setIsPublic(true);
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(publicMap));
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

    // --- endSession ---

    @Test
    public void endSession_validInput_success() {
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(sessionRepository.findBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID))
                .thenReturn(Optional.of(buildActiveSession()));
        Mockito.when(sessionRepository.save(Mockito.any())).thenReturn(buildActiveSession());
        Mockito.doNothing().when(speedFeedbackService).clearSession(Mockito.any());

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

    // --- setPromptedQuiz ---

    @Test
    public void setPromptedQuiz_validOwner_setsSkillId() {
        CollaborationSession session = buildActiveSession();
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(sessionRepository.findBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID))
                .thenReturn(Optional.of(session));
        Mockito.when(sessionRepository.save(Mockito.any())).thenReturn(session);

        sessionService.setPromptedQuiz(SKILL_MAP_ID, buildOwner(), 42L);

        ArgumentCaptor<CollaborationSession> captor = ArgumentCaptor.forClass(CollaborationSession.class);
        Mockito.verify(sessionRepository).save(captor.capture());
        assertEquals(42L, captor.getValue().getPromptedQuizSkillId());
    }

    @Test
    public void setPromptedQuiz_nullSkillId_clearsPrompt() {
        CollaborationSession session = buildActiveSession();
        session.setPromptedQuizSkillId(42L);
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(sessionRepository.findBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID))
                .thenReturn(Optional.of(session));
        Mockito.when(sessionRepository.save(Mockito.any())).thenReturn(session);

        sessionService.setPromptedQuiz(SKILL_MAP_ID, buildOwner(), null);

        ArgumentCaptor<CollaborationSession> captor = ArgumentCaptor.forClass(CollaborationSession.class);
        Mockito.verify(sessionRepository).save(captor.capture());
        assertNull(captor.getValue().getPromptedQuizSkillId());
    }

    @Test
    public void setPromptedQuiz_skillMapNotFound_throwsNotFound() {
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sessionService.setPromptedQuiz(SKILL_MAP_ID, buildOwner(), 42L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    public void setPromptedQuiz_notOwner_throwsForbidden() {
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));

        assertThrows(ResponseStatusException.class,
                () -> sessionService.setPromptedQuiz(SKILL_MAP_ID, buildOtherUser(), 42L));
    }

    @Test
    public void setPromptedQuiz_noActiveSession_throwsNotFound() {
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(sessionRepository.findBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> sessionService.setPromptedQuiz(SKILL_MAP_ID, buildOwner(), 42L));
    }

    // getSessionById

    @Test
    public void getSessionById_sessionExists_returnsSession() {
        CollaborationSession session = buildActiveSession();
        Mockito.when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

        CollaborationSession result = sessionService.getSessionById(SESSION_ID);

        assertEquals(SESSION_ID, result.getId());
        assertEquals(SKILL_MAP_ID, result.getSkillMapId());
    }

    @Test
    public void getSessionById_notFound_throwsNotFound() {
        Mockito.when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sessionService.getSessionById(SESSION_ID));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // endSession additional cases

    @Test
    public void endSession_skillMapNotFound_throwsNotFound() {
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sessionService.endSession(SKILL_MAP_ID, buildOwner()));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // getQuizResults

    @Test
    public void getQuizResults_skillMapNotFound_throwsNotFound() {
        Mockito.when(membershipRepository.existsBySkillMapIdAndUserId(SKILL_MAP_ID, OWNER_ID)).thenReturn(true);
        Mockito.when(sessionRepository.findBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID))
                .thenReturn(Optional.of(buildActiveSession()));
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sessionService.getQuizResults(SKILL_MAP_ID, buildOwner()));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    public void getQuizResults_noSkillsForMap_returnsEmpty() {
        Mockito.when(membershipRepository.existsBySkillMapIdAndUserId(SKILL_MAP_ID, OWNER_ID)).thenReturn(true);
        Mockito.when(sessionRepository.findBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID))
                .thenReturn(Optional.of(buildActiveSession()));
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(skillRepository.findBySkillMap(Mockito.any())).thenReturn(List.of());

        var result = sessionService.getQuizResults(SKILL_MAP_ID, buildOwner());

        assertTrue(result.isEmpty());
    }

    @Test
    public void getQuizResults_skillsWithNoQuiz_returnsEmpty() {
        Skill skill = new Skill();
        skill.setId(5L);

        Mockito.when(membershipRepository.existsBySkillMapIdAndUserId(SKILL_MAP_ID, OWNER_ID)).thenReturn(true);
        Mockito.when(sessionRepository.findBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID))
                .thenReturn(Optional.of(buildActiveSession()));
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(skillRepository.findBySkillMap(Mockito.any())).thenReturn(List.of(skill));
        Mockito.when(quizRepository.findBySkillId(5L)).thenReturn(Optional.empty());

        var result = sessionService.getQuizResults(SKILL_MAP_ID, buildOwner());

        assertTrue(result.isEmpty());
    }

    @Test
    public void getQuizResults_withQuizAttempts_returnsSummaries() {
        Skill skill = new Skill();
        skill.setId(5L);

        Quiz quiz = new Quiz();
        quiz.setId(50L);
        quiz.setSkillId(5L);

        List<Object[]> rows = List.<Object[]>of(new Object[]{50L, 3L, 75.0});

        Mockito.when(membershipRepository.existsBySkillMapIdAndUserId(SKILL_MAP_ID, OWNER_ID)).thenReturn(true);
        Mockito.when(sessionRepository.findBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID))
                .thenReturn(Optional.of(buildActiveSession()));
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(skillRepository.findBySkillMap(Mockito.any())).thenReturn(List.of(skill));
        Mockito.when(quizRepository.findBySkillId(5L)).thenReturn(Optional.of(quiz));
        Mockito.when(quizAttemptRepository.aggregateByQuizIdsAndStartedAt(
                Mockito.eq(List.of(50L)), Mockito.any())).thenReturn(rows);
        Mockito.when(quizRepository.findById(50L)).thenReturn(Optional.of(quiz));

        List<DashboardQuizSummaryDTO> result = sessionService.getQuizResults(SKILL_MAP_ID, buildOwner());

        assertEquals(1, result.size());
        assertEquals(50L, result.get(0).getQuizId());
        assertEquals(5L, result.get(0).getSkillId());
        assertEquals(3, result.get(0).getTotalAttempts());
        assertEquals(75.0, result.get(0).getAverageScore());
    }
}
