package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.CollaborationSession;
import ch.uzh.ifi.hase.soprafs26.entity.LiveQuestion;
import ch.uzh.ifi.hase.soprafs26.entity.Quiz;
import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.UnderstandingRating;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.CollaborationSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.LiveQuestionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UnderstandingRatingRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.DashboardQuizSummaryDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionStateDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillRatingSummaryDTO;
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

    @Mock
    private LiveQuestionRepository liveQuestionRepository;

    @Mock
    private UnderstandingRatingRepository understandingRatingRepository;

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

    // --- getActiveSessionState ---

    private UnderstandingRating buildRating(Long skillId, Long userId, Integer rating) {
        UnderstandingRating r = new UnderstandingRating();
        r.setSessionId(SESSION_ID);
        r.setSkillId(skillId);
        r.setUserId(userId);
        r.setRating(rating);
        r.setSubmittedAt(LocalDateTime.now());
        return r;
    }

    private void mockActiveSessionState(List<LiveQuestion> questions, List<UnderstandingRating> ratings) {
        Mockito.when(membershipRepository.existsBySkillMapIdAndUserId(SKILL_MAP_ID, OWNER_ID)).thenReturn(true);
        Mockito.when(sessionRepository.findBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID))
                .thenReturn(Optional.of(buildActiveSession()));
        Mockito.when(liveQuestionRepository.findBySessionId(SESSION_ID)).thenReturn(questions);
        Mockito.when(understandingRatingRepository.findBySessionId(SESSION_ID)).thenReturn(ratings);
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(skillRepository.findBySkillMap(Mockito.any())).thenReturn(List.of());
    }

    @Test
    public void getActiveSessionState_noRatings_returnsEmptySkillRatings() {
        mockActiveSessionState(List.of(), List.of());

        SessionStateDTO result = sessionService.getActiveSessionState(SKILL_MAP_ID, buildOwner());

        assertNotNull(result);
        assertTrue(result.getSkillRatings().isEmpty());
        assertTrue(result.getQuestions().isEmpty());
    }

    @Test
    public void getActiveSessionState_withRatings_computesCorrectAverageAndTotal() {
        List<UnderstandingRating> ratings = List.of(
                buildRating(5L, OTHER_ID, 60),
                buildRating(5L, 300L, 80)
        );
        mockActiveSessionState(List.of(), ratings);

        SessionStateDTO result = sessionService.getActiveSessionState(SKILL_MAP_ID, buildOwner());

        assertEquals(1, result.getSkillRatings().size());
        SkillRatingSummaryDTO summary = result.getSkillRatings().get(0);
        assertEquals(5L, summary.getSkillId());
        assertEquals(70.0, summary.getAverageRating());
        assertEquals(2, summary.getTotalRatings());
    }

    @Test
    public void getActiveSessionState_myRatingSetForCallingUser() {
        List<UnderstandingRating> ratings = List.of(
                buildRating(5L, OWNER_ID, 75),
                buildRating(5L, OTHER_ID, 25)
        );
        mockActiveSessionState(List.of(), ratings);

        SessionStateDTO result = sessionService.getActiveSessionState(SKILL_MAP_ID, buildOwner());

        SkillRatingSummaryDTO summary = result.getSkillRatings().get(0);
        assertEquals(75, summary.getMyRating());
        assertEquals(50.0, summary.getAverageRating());
    }

    @Test
    public void getActiveSessionState_myRatingNullWhenUserHasNotRated() {
        List<UnderstandingRating> ratings = List.of(
                buildRating(5L, OTHER_ID, 80)
        );
        mockActiveSessionState(List.of(), ratings);

        SessionStateDTO result = sessionService.getActiveSessionState(SKILL_MAP_ID, buildOwner());

        SkillRatingSummaryDTO summary = result.getSkillRatings().get(0);
        assertNull(summary.getMyRating());
        assertEquals(1, summary.getTotalRatings());
    }

    @Test
    public void getActiveSessionState_ratingsGroupedBySkill() {
        List<UnderstandingRating> ratings = List.of(
                buildRating(5L, OTHER_ID, 60),
                buildRating(6L, OTHER_ID, 90)
        );
        mockActiveSessionState(List.of(), ratings);

        SessionStateDTO result = sessionService.getActiveSessionState(SKILL_MAP_ID, buildOwner());

        assertEquals(2, result.getSkillRatings().size());
    }

    @Test
    public void getActiveSessionState_multipleSkills_computesCorrectAverageAndTotalPerSkill() {
        List<UnderstandingRating> ratings = List.of(
                buildRating(5L, OTHER_ID, 40),
                buildRating(5L, 300L, 80),
                buildRating(6L, OTHER_ID, 90),
                buildRating(6L, OWNER_ID, 70)
        );
        mockActiveSessionState(List.of(), ratings);

        SessionStateDTO result = sessionService.getActiveSessionState(SKILL_MAP_ID, buildOwner());

        assertEquals(2, result.getSkillRatings().size());

        SkillRatingSummaryDTO skill5 = result.getSkillRatings().stream()
                .filter(s -> s.getSkillId().equals(5L)).findFirst().orElseThrow();
        assertEquals(60.0, skill5.getAverageRating());
        assertEquals(2, skill5.getTotalRatings());
        assertNull(skill5.getMyRating());

        SkillRatingSummaryDTO skill6 = result.getSkillRatings().stream()
                .filter(s -> s.getSkillId().equals(6L)).findFirst().orElseThrow();
        assertEquals(80.0, skill6.getAverageRating());
        assertEquals(2, skill6.getTotalRatings());
        assertEquals(70, skill6.getMyRating());
    }

    @Test
    public void getActiveSessionState_questionsIncludedInResponse() {
        LiveQuestion q1 = new LiveQuestion();
        q1.setId(1L);
        q1.setSessionId(SESSION_ID);
        LiveQuestion q2 = new LiveQuestion();
        q2.setId(2L);
        q2.setSessionId(SESSION_ID);
        mockActiveSessionState(List.of(q1, q2), List.of());

        SessionStateDTO result = sessionService.getActiveSessionState(SKILL_MAP_ID, buildOwner());

        assertEquals(2, result.getQuestions().size());
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

    //restartSession

    @Test
    public void restartSession_validInput_success() {
        CollaborationSession inactiveSession = buildActiveSession();
        inactiveSession.setActive(false);
        inactiveSession.setEndedAt(LocalDateTime.now());

        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(inactiveSession));
        Mockito.when(sessionRepository.existsBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID)).thenReturn(false);
        Mockito.when(liveQuestionRepository.findBySessionId(SESSION_ID)).thenReturn(List.of());
        Mockito.when(sessionRepository.save(Mockito.any())).thenReturn(inactiveSession);

        sessionService.restartSession(SKILL_MAP_ID, SESSION_ID, buildOwner());

        ArgumentCaptor<CollaborationSession> captor = ArgumentCaptor.forClass(CollaborationSession.class);
        Mockito.verify(sessionRepository).save(captor.capture());
        assertTrue(captor.getValue().isActive());
        assertNull(captor.getValue().getEndedAt());

        Mockito.verify(broadcastService, Mockito.times(1))
                .broadcastSessionStarted(Mockito.eq(SKILL_MAP_ID), Mockito.eq(SESSION_ID), Mockito.any());
    }

    @Test
    public void restartSession_skillMapNotFound_throwsNotFound() {
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> sessionService.restartSession(SKILL_MAP_ID, SESSION_ID, buildOwner()));
    }

    @Test
    public void restartSession_notOwner_throwsForbidden() {
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> sessionService.restartSession(SKILL_MAP_ID, SESSION_ID, buildOtherUser()));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    public void restartSession_sessionNotFound_throwsNotFound() {
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> sessionService.restartSession(SKILL_MAP_ID, SESSION_ID, buildOwner()));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    public void restartSession_sessionDoesNotBelongToSkillMap_throwsForbidden() {
        CollaborationSession session = buildActiveSession();
        session.setSkillMapId(999L); // Different skill map

        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> sessionService.restartSession(SKILL_MAP_ID, SESSION_ID, buildOwner()));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    public void restartSession_alreadyActive_throwsConflict() {
        CollaborationSession activeSession = buildActiveSession();
        activeSession.setActive(true);

        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(activeSession));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> sessionService.restartSession(SKILL_MAP_ID, SESSION_ID, buildOwner()));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    public void restartSession_anotherSessionAlreadyActive_throwsConflict() {
        CollaborationSession inactiveSession = buildActiveSession();
        inactiveSession.setActive(false);

        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(inactiveSession));
        Mockito.when(sessionRepository.existsBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> sessionService.restartSession(SKILL_MAP_ID, SESSION_ID, buildOwner()));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    public void restartSession_marksUnaddressedQuestionsAsAddressed() {
        CollaborationSession inactiveSession = buildActiveSession();
        inactiveSession.setActive(false);

        LiveQuestion q1 = new LiveQuestion();
        q1.setId(1L);
        q1.setSessionId(SESSION_ID);
        q1.setIsAddressed(false);

        LiveQuestion q2 = new LiveQuestion();
        q2.setId(2L);
        q2.setSessionId(SESSION_ID);
        q2.setIsAddressed(true);

        LiveQuestion q3 = new LiveQuestion();
        q3.setId(3L);
        q3.setSessionId(SESSION_ID);
        q3.setIsAddressed(false);

        List<LiveQuestion> questions = List.of(q1, q2, q3);

        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(inactiveSession));
        Mockito.when(sessionRepository.existsBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID)).thenReturn(false);
        Mockito.when(liveQuestionRepository.findBySessionId(SESSION_ID)).thenReturn(questions);
        Mockito.when(sessionRepository.save(Mockito.any())).thenReturn(inactiveSession);

        sessionService.restartSession(SKILL_MAP_ID, SESSION_ID, buildOwner());

        ArgumentCaptor<LiveQuestion> questionCaptor = ArgumentCaptor.forClass(LiveQuestion.class);
        Mockito.verify(liveQuestionRepository, Mockito.times(2)).save(questionCaptor.capture());

        List<LiveQuestion> savedQuestions = questionCaptor.getAllValues();
        assertEquals(2, savedQuestions.size());
        assertTrue(savedQuestions.stream().allMatch(q -> Boolean.TRUE.equals(q.getIsAddressed())));
    }

    @Test
    public void restartSession_noLiveQuestions_restarts() {
        CollaborationSession inactiveSession = buildActiveSession();
        inactiveSession.setActive(false);

        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(inactiveSession));
        Mockito.when(sessionRepository.existsBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID)).thenReturn(false);
        Mockito.when(liveQuestionRepository.findBySessionId(SESSION_ID)).thenReturn(List.of());
        Mockito.when(sessionRepository.save(Mockito.any())).thenReturn(inactiveSession);

        sessionService.restartSession(SKILL_MAP_ID, SESSION_ID, buildOwner());

        Mockito.verify(liveQuestionRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void restartSession_clearsEndedAt() {
        CollaborationSession inactiveSession = buildActiveSession();
        inactiveSession.setActive(false);
        inactiveSession.setEndedAt(LocalDateTime.now());

        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(inactiveSession));
        Mockito.when(sessionRepository.existsBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID)).thenReturn(false);
        Mockito.when(liveQuestionRepository.findBySessionId(SESSION_ID)).thenReturn(List.of());
        Mockito.when(sessionRepository.save(Mockito.any())).thenReturn(inactiveSession);

        sessionService.restartSession(SKILL_MAP_ID, SESSION_ID, buildOwner());

        ArgumentCaptor<CollaborationSession> captor = ArgumentCaptor.forClass(CollaborationSession.class);
        Mockito.verify(sessionRepository).save(captor.capture());
        assertNull(captor.getValue().getEndedAt());
    }

    @Test
    public void restartSession_doesNotModifyAlreadyAddressedQuestions() {
        CollaborationSession inactiveSession = buildActiveSession();
        inactiveSession.setActive(false);

        LiveQuestion addressed = new LiveQuestion();
        addressed.setId(1L);
        addressed.setSessionId(SESSION_ID);
        addressed.setIsAddressed(true);

        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(inactiveSession));
        Mockito.when(sessionRepository.existsBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID)).thenReturn(false);
        Mockito.when(liveQuestionRepository.findBySessionId(SESSION_ID)).thenReturn(List.of(addressed));
        Mockito.when(sessionRepository.save(Mockito.any())).thenReturn(inactiveSession);

        sessionService.restartSession(SKILL_MAP_ID, SESSION_ID, buildOwner());

        Mockito.verify(liveQuestionRepository, Mockito.never()).save(Mockito.any(LiveQuestion.class));
    }

    @Test
    public void restartSession_returnsRestoredSession() {
        CollaborationSession inactiveSession = buildActiveSession();
        inactiveSession.setActive(false);
        inactiveSession.setEndedAt(LocalDateTime.now());

        CollaborationSession savedSession = buildActiveSession();
        savedSession.setActive(true);
        savedSession.setEndedAt(null);

        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(inactiveSession));
        Mockito.when(sessionRepository.existsBySkillMapIdAndIsActiveTrue(SKILL_MAP_ID)).thenReturn(false);
        Mockito.when(liveQuestionRepository.findBySessionId(SESSION_ID)).thenReturn(List.of());
        Mockito.when(sessionRepository.save(Mockito.any())).thenReturn(savedSession);

        CollaborationSession result = sessionService.restartSession(SKILL_MAP_ID, SESSION_ID, buildOwner());

        assertNotNull(result);
        assertTrue(result.isActive());
        assertNull(result.getEndedAt());
    }

    // --- getPastSessions ---

    @Test
    public void getPastSessions_validOwner_returnsPastSessions() {
        CollaborationSession past1 = buildActiveSession();
        past1.setActive(false);
        CollaborationSession past2 = buildActiveSession();
        past2.setActive(false);

        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(sessionRepository.findBySkillMapIdAndIsActiveFalseOrderByStartedAtDesc(SKILL_MAP_ID))
                .thenReturn(List.of(past1, past2));

        List<CollaborationSession> result = sessionService.getPastSessions(SKILL_MAP_ID, buildOwner());

        assertEquals(2, result.size());
        Mockito.verify(sessionRepository, Mockito.times(1))
                .findBySkillMapIdAndIsActiveFalseOrderByStartedAtDesc(SKILL_MAP_ID);
    }

    @Test
    public void getPastSessions_notOwner_throwsForbidden() {
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> sessionService.getPastSessions(SKILL_MAP_ID, buildOtherUser()));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    public void getPastSessions_skillMapNotFound_throwsNotFound() {
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> sessionService.getPastSessions(SKILL_MAP_ID, buildOwner()));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    public void getPastSessions_noSessions_returnsEmpty() {
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(sessionRepository.findBySkillMapIdAndIsActiveFalseOrderByStartedAtDesc(SKILL_MAP_ID))
                .thenReturn(List.of());

        List<CollaborationSession> result = sessionService.getPastSessions(SKILL_MAP_ID, buildOwner());

        assertTrue(result.isEmpty());
    }
}
