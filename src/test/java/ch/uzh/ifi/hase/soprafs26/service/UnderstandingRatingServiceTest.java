package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.CollaborationSession;
import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.UnderstandingRating;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.CollaborationSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapMembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UnderstandingRatingRepository;
import ch.uzh.ifi.hase.soprafs26.websocket.WebSocketBroadcastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;
import static org.mockito.ArgumentMatchers.eq;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UnderstandingRatingServiceTest {

    private static final Long SESSION_ID = 1L;
    private static final Long SKILL_MAP_ID = 2L;
    private static final Long SKILL_ID = 3L;
    private static final Long OWNER_ID = 10L;
    private static final Long OTHER_ID = 20L;

    @Mock private UnderstandingRatingRepository ratingRepository;
    @Mock private CollaborationSessionRepository sessionRepository;
    @Mock private SkillMapRepository skillMapRepository;
    @Mock private SkillMapMembershipRepository membershipRepository;
    @Mock private SkillRepository skillRepository;
    @Mock private WebSocketBroadcastService broadcastService;

    @InjectMocks
    private UnderstandingRatingService ratingService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private User buildOwner() {
        User u = new User(); u.setId(OWNER_ID); return u;
    }

    private User buildOtherUser() {
        User u = new User(); u.setId(OTHER_ID); return u;
    }

    private CollaborationSession buildActiveSession() {
        CollaborationSession s = new CollaborationSession();
        s.setSkillMapId(SKILL_MAP_ID);
        s.setActive(true);
        return s;
    }

    private SkillMap buildSkillMap() {
        SkillMap sm = new SkillMap();
        sm.setId(SKILL_MAP_ID);
        sm.setOwnerId(OWNER_ID);
        return sm;
    }

    // --- submitRating ---

    @Test
    public void submitRating_validInput_success() {
        Mockito.when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(buildActiveSession()));
        Mockito.when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(new Skill()));
        Mockito.when(membershipRepository.existsBySkillMapIdAndUserId(SKILL_MAP_ID, OWNER_ID)).thenReturn(true);
        Mockito.when(ratingRepository.findBySessionIdAndSkillIdAndUserId(SESSION_ID, SKILL_ID, OWNER_ID)).thenReturn(Optional.empty());
        Mockito.when(ratingRepository.findBySessionIdAndSkillId(SESSION_ID, SKILL_ID)).thenReturn(List.of());
        Mockito.when(ratingRepository.save(Mockito.any())).thenAnswer(i -> i.getArgument(0));

        UnderstandingRating result = ratingService.submitRating(SESSION_ID, SKILL_ID, buildOwner(), 80);

        assertEquals(80, result.getRating());
        assertNotNull(result.getSubmittedAt());
        Mockito.verify(broadcastService).broadcastRatingUpdate(eq(SESSION_ID), eq(SKILL_ID), Mockito.anyDouble());
    }

    @Test
    public void submitRating_invalidRating_throwsBadRequest() {
        assertThrows(ResponseStatusException.class,
                () -> ratingService.submitRating(SESSION_ID, SKILL_ID, buildOwner(), 150));
    }

    @Test
    public void submitRating_sessionNotActive_throwsForbidden() {
        CollaborationSession inactive = buildActiveSession();
        inactive.setActive(false);
        Mockito.when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(inactive));

        assertThrows(ResponseStatusException.class,
                () -> ratingService.submitRating(SESSION_ID, SKILL_ID, buildOwner(), 50));
    }

    @Test
    public void submitRating_notMember_throwsForbidden() {
        Mockito.when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(buildActiveSession()));
        Mockito.when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(new Skill()));
        Mockito.when(membershipRepository.existsBySkillMapIdAndUserId(SKILL_MAP_ID, OTHER_ID)).thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> ratingService.submitRating(SESSION_ID, SKILL_ID, buildOtherUser(), 50));
    }

    // --- getRatingsBySkill ---

    @Test
    public void getRatingsBySkill_ownerAccess_success() {
        Mockito.when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(buildActiveSession()));
        Mockito.when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(new Skill()));
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(ratingRepository.findBySessionIdAndSkillId(SESSION_ID, SKILL_ID)).thenReturn(List.of());

        assertDoesNotThrow(() -> ratingService.getRatingsBySkill(SESSION_ID, SKILL_ID, buildOwner()));
    }

    @Test
    public void getRatingsBySkill_notOwner_throwsForbidden() {
        Mockito.when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(buildActiveSession()));
        Mockito.when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(new Skill()));
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));

        assertThrows(ResponseStatusException.class,
                () -> ratingService.getRatingsBySkill(SESSION_ID, SKILL_ID, buildOtherUser()));
    }

    // --- getRatingsBySession ---

    @Test
    public void getRatingsBySession_ownerAccess_success() {
        Mockito.when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(buildActiveSession()));
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));
        Mockito.when(ratingRepository.findBySessionId(SESSION_ID)).thenReturn(List.of());

        assertDoesNotThrow(() -> ratingService.getRatingsBySession(SESSION_ID, buildOwner()));
    }

    @Test
    public void getRatingsBySession_notOwner_throwsForbidden() {
        Mockito.when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(buildActiveSession()));
        Mockito.when(skillMapRepository.findById(SKILL_MAP_ID)).thenReturn(Optional.of(buildSkillMap()));

        assertThrows(ResponseStatusException.class,
                () -> ratingService.getRatingsBySession(SESSION_ID, buildOtherUser()));
    }
}