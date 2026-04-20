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

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class LiveQuestionService {
    private final LiveQuestionRepository liveQuestionRepository;
    private final UpvoteRecordRepository upvoteRecordRepository;
    private final CollaborationSessionRepository collaborationSessionRepository;
    private final SkillMapRepository skillMapRepository;
    private final WebSocketBroadcastService webSocketBroadcastService;

    public LiveQuestionService(LiveQuestionRepository liveQuestionRepository,
                            UpvoteRecordRepository upvoteRecordRepository,
                            CollaborationSessionRepository collaborationSessionRepository,
                            SkillMapRepository skillMapRepository,
                            WebSocketBroadcastService webSocketBroadcastService) {
        this.liveQuestionRepository = liveQuestionRepository;
        this.upvoteRecordRepository = upvoteRecordRepository;
        this.collaborationSessionRepository = collaborationSessionRepository;
        this.skillMapRepository = skillMapRepository;
        this.webSocketBroadcastService = webSocketBroadcastService;
    }


    public List<LiveQuestion> getQuestionsBySession(Long sessionId) {
        return liveQuestionRepository.findBySessionId(sessionId);
    }

    public LiveQuestion postQuestion(Long sessionId, Long skillId, String skillName, String text) {
        LiveQuestion question = new LiveQuestion();
        question.setSessionId(sessionId);
        question.setSkillId(skillId);
        question.setSkillName(skillName);
        question.setText(text);
        question.setUpvoteCount(0);
        question.setIsAddressed(false);
        question.setPostedAt(LocalDateTime.now());
        LiveQuestion saved = liveQuestionRepository.save(question);
        webSocketBroadcastService.broadcastQuestionsState(sessionId, getQuestionsBySession(sessionId));
        return saved;
    }

    public void deleteQuestion(Long questionId) {
        LiveQuestion question = liveQuestionRepository.findById(questionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        liveQuestionRepository.delete(question);
    }

    public LiveQuestion upvoteQuestion(Long questionId, Long userId) {
        LiveQuestion question = liveQuestionRepository.findById(questionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        boolean alreadyUpvoted = upvoteRecordRepository.existsByQuestionIdAndUserId(questionId, userId);
        if (alreadyUpvoted) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already upvoted this question");
        }
        UpvoteRecord record = new UpvoteRecord();
        record.setQuestionId(questionId);
        record.setUserId(userId);
        upvoteRecordRepository.save(record);

        question.setUpvoteCount(question.getUpvoteCount() + 1);
        LiveQuestion saved = liveQuestionRepository.save(question);
        webSocketBroadcastService.broadcastQuestionsState(question.getSessionId(), getQuestionsBySession(question.getSessionId()));
        return saved;
    }

    public void removeUpvote(Long questionId, Long userId) {
        LiveQuestion question = liveQuestionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));

        UpvoteRecord record = upvoteRecordRepository.findByQuestionIdAndUserId(questionId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Upvote not found"));

        upvoteRecordRepository.delete(record);
        question.setUpvoteCount(Math.max(0, question.getUpvoteCount() - 1));
        liveQuestionRepository.save(question);
        webSocketBroadcastService.broadcastQuestionsState(question.getSessionId(), getQuestionsBySession(question.getSessionId()));
    }

    public LiveQuestion markAddressed(Long questionId, Long requestingUserId) {
        LiveQuestion question = liveQuestionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));

        CollaborationSession session = collaborationSessionRepository.findById(question.getSessionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        SkillMap skillMap = skillMapRepository.findById(session.getSkillMapId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SkillMap not found"));

        if (!skillMap.getOwnerId().equals(requestingUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can mark questions as addressed");
        }

        question.setIsAddressed(true);
        question.setUpdatedAt(LocalDateTime.now());
        LiveQuestion saved = liveQuestionRepository.save(question);
        webSocketBroadcastService.broadcastQuestionsState(question.getSessionId(), getQuestionsBySession(question.getSessionId()));
        return saved;
    }

    public void deleteAllQuestionsForSession(Long sessionId) {
        liveQuestionRepository.deleteBySessionId(sessionId);
    }
}
