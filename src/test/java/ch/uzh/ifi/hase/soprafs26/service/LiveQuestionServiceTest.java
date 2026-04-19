package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.CollaborationSession;
import ch.uzh.ifi.hase.soprafs26.entity.LiveQuestion;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.UpvoteRecord;
import ch.uzh.ifi.hase.soprafs26.repository.CollaborationSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.LiveQuestionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UpvoteRecordRepository;
import ch.uzh.ifi.hase.soprafs26.websocket.WebSocketBroadcastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LiveQuestionServiceTest {

    @Mock
    private LiveQuestionRepository liveQuestionRepository;

    @Mock
    private UpvoteRecordRepository upvoteRecordRepository;

    @Mock
    private CollaborationSessionRepository collaborationSessionRepository;

    @Mock
    private SkillMapRepository skillMapRepository;

    @Mock
    private WebSocketBroadcastService webSocketBroadcastService;

    @InjectMocks
    private LiveQuestionService liveQuestionService;

    private LiveQuestion dummyQuestion;
    private CollaborationSession dummySession;
    private SkillMap dummyMap;

    @BeforeEach
    void setup() {
        dummyQuestion = new LiveQuestion();
        dummyQuestion.setId(10L);
        dummyQuestion.setSessionId(1L);
        dummyQuestion.setSkillId(2L);
        dummyQuestion.setSkillName("Skill A");
        dummyQuestion.setText("What is polymorphism?");
        dummyQuestion.setUpvoteCount(0);
        dummyQuestion.setIsAddressed(false);
        dummyQuestion.setPostedAt(LocalDateTime.now());

        dummySession = new CollaborationSession();
        dummySession.setSkillMapId(5L);

        dummyMap = new SkillMap();
        dummyMap.setId(5L);
        dummyMap.setOwnerId(99L);
    }

    @Test
    void getQuestionsBySession_returnsListFromRepository() {
        given(liveQuestionRepository.findBySessionId(1L)).willReturn(List.of(dummyQuestion));

        List<LiveQuestion> result = liveQuestionService.getQuestionsBySession(1L);

        assertEquals(1, result.size());
        assertEquals("What is polymorphism?", result.get(0).getText());
    }

    @Test
    void postQuestion_savesAndReturnsQuestion() {
        given(liveQuestionRepository.save(any())).willReturn(dummyQuestion);

        LiveQuestion result = liveQuestionService.postQuestion(1L, 2L, "Skill A", "What is polymorphism?");

        assertNotNull(result);
        assertEquals("What is polymorphism?", result.getText());
        assertEquals(0, result.getUpvoteCount());
        assertFalse(result.getIsAddressed());
    }

    @Test
    void deleteQuestion_questionExists_deletesSuccessfully() {
        given(liveQuestionRepository.findById(10L)).willReturn(Optional.of(dummyQuestion));

        liveQuestionService.deleteQuestion(10L);

        verify(liveQuestionRepository).delete(dummyQuestion);
    }

    @Test
    void deleteQuestion_questionNotFound_throwsNotFound() {
        given(liveQuestionRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> liveQuestionService.deleteQuestion(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void upvoteQuestion_newUpvote_incrementsCount() {
        given(liveQuestionRepository.findById(10L)).willReturn(Optional.of(dummyQuestion));
        given(upvoteRecordRepository.existsByQuestionIdAndUserId(10L, 1L)).willReturn(false);
        given(upvoteRecordRepository.save(any())).willReturn(new UpvoteRecord());
        given(liveQuestionRepository.save(any())).willReturn(dummyQuestion);

        liveQuestionService.upvoteQuestion(10L, 1L);

        verify(upvoteRecordRepository).save(any());
        assertEquals(1, dummyQuestion.getUpvoteCount());
    }

    @Test
    void upvoteQuestion_alreadyUpvoted_throwsConflict() {
        given(liveQuestionRepository.findById(10L)).willReturn(Optional.of(dummyQuestion));
        given(upvoteRecordRepository.existsByQuestionIdAndUserId(10L, 1L)).willReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> liveQuestionService.upvoteQuestion(10L, 1L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void removeUpvote_existingUpvote_decrementsCount() {
        dummyQuestion.setUpvoteCount(1);
        UpvoteRecord record = new UpvoteRecord();
        given(liveQuestionRepository.findById(10L)).willReturn(Optional.of(dummyQuestion));
        given(upvoteRecordRepository.findByQuestionIdAndUserId(10L, 1L)).willReturn(Optional.of(record));

        liveQuestionService.removeUpvote(10L, 1L);

        verify(upvoteRecordRepository).delete(record);
        assertEquals(0, dummyQuestion.getUpvoteCount());
    }

    @Test
    void markAddressed_byOwner_setsAddressedTrue() {
        given(liveQuestionRepository.findById(10L)).willReturn(Optional.of(dummyQuestion));
        given(collaborationSessionRepository.findById(1L)).willReturn(Optional.of(dummySession));
        given(skillMapRepository.findById(5L)).willReturn(Optional.of(dummyMap));
        given(liveQuestionRepository.save(any())).willReturn(dummyQuestion);

        liveQuestionService.markAddressed(10L, 99L);

        assertTrue(dummyQuestion.getIsAddressed());
    }

    @Test
    void markAddressed_byNonOwner_throwsForbidden() {
        given(liveQuestionRepository.findById(10L)).willReturn(Optional.of(dummyQuestion));
        given(collaborationSessionRepository.findById(1L)).willReturn(Optional.of(dummySession));
        given(skillMapRepository.findById(5L)).willReturn(Optional.of(dummyMap));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> liveQuestionService.markAddressed(10L, 42L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }
}