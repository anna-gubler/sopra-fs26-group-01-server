package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.CollaborationSession;
import ch.uzh.ifi.hase.soprafs26.entity.Quiz;
import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.CollaborationSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.QuizAttemptRepository;
import ch.uzh.ifi.hase.soprafs26.repository.QuizRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapMembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.DashboardQuizSummaryDTO;
import ch.uzh.ifi.hase.soprafs26.websocket.WebSocketBroadcastService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CollaborationSessionService {

    private final SkillMapRepository skillMapRepository;
    private final SkillRepository skillRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CollaborationSessionRepository sessionRepository;
    private final WebSocketBroadcastService broadcastService;
    private final SkillMapMembershipRepository membershipRepository;
    private final SpeedFeedbackService speedFeedbackService;
    private final CurrentUnderstandingService currentUnderstandingService;

    public CollaborationSessionService(CollaborationSessionRepository sessionRepository,
            WebSocketBroadcastService broadcastService, SkillMapRepository skillMapRepository,
            SkillMapMembershipRepository membershipRepository,
            SpeedFeedbackService speedFeedbackService,
            CurrentUnderstandingService currentUnderstandingService,
            SkillRepository skillRepository,
            QuizRepository quizRepository,
            QuizAttemptRepository quizAttemptRepository) {
        this.sessionRepository = sessionRepository;
        this.broadcastService = broadcastService;
        this.skillMapRepository = skillMapRepository;
        this.membershipRepository = membershipRepository;
        this.speedFeedbackService = speedFeedbackService;
        this.currentUnderstandingService = currentUnderstandingService;
        this.quizRepository = quizRepository;
        this.skillRepository = skillRepository;
        this.quizAttemptRepository = quizAttemptRepository;
    }

    public CollaborationSession startSession(Long skillMapId, User user) {
        SkillMap skillMap = skillMapRepository.findById(skillMapId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill map not found"));

        if (!skillMap.getOwnerId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can start a session");
        }

        if (sessionRepository.existsBySkillMapIdAndIsActiveTrue(skillMapId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A session is already active");
        }

        CollaborationSession session = new CollaborationSession();
        session.setSkillMapId(skillMapId);
        session.setStartedAt(LocalDateTime.now());
        session.setActive(true);
        session = sessionRepository.save(session);

        broadcastService.broadcastSessionStarted(skillMapId, session.getId(), session.getStartedAt());
        return session;
    }

    public void endSession(Long skillMapId, User user) {
        SkillMap skillMap = skillMapRepository.findById(skillMapId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill map not found"));

        if (!skillMap.getOwnerId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can end a session");
        }

        CollaborationSession session = sessionRepository.findBySkillMapIdAndIsActiveTrue(skillMapId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active session found"));

        session.setActive(false);
        session.setEndedAt(LocalDateTime.now());
        session = sessionRepository.save(session);
        speedFeedbackService.clearSession(session.getId());
        currentUnderstandingService.clearSession(session.getId());

        //design decision to NOT delete the questions after session end
        // liveQuestionService.deleteAllQuestionsForSession(session.getId()); 

        broadcastService.broadcastSessionEnded(skillMapId, session.getId(), session.getEndedAt());
    }

    public CollaborationSession getActiveSession(Long skillMapId, User user) {
        if (!membershipRepository.existsBySkillMapIdAndUserId(skillMapId, user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a member of this skill map");
        }
        return sessionRepository.findBySkillMapIdAndIsActiveTrue(skillMapId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active session found"));
    }

    public CollaborationSession getSessionById(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
    }

    public List<DashboardQuizSummaryDTO> getQuizResults(Long skillMapId, User user) {
        CollaborationSession session = getActiveSession(skillMapId, user);

        SkillMap skillMap = skillMapRepository.findById(skillMapId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SkillMap not found"));
        List<Skill> skills = skillRepository.findBySkillMap(skillMap);

        List<Long> quizIds = skills.stream()
            .flatMap(skill -> quizRepository.findBySkillId(skill.getId()).stream())
            .map(Quiz::getId)
            .toList();

        if (quizIds.isEmpty()) return List.of();

        List<Object[]> rows = quizAttemptRepository
            .aggregateByQuizIdsAndStartedAt(quizIds, session.getStartedAt());

        return rows.stream().map(row -> {
            Long quizId = (Long) row[0];
            Long skillId = quizRepository.findById(quizId)
                .map(Quiz::getSkillId)
                .orElse(null);
            DashboardQuizSummaryDTO dto = new DashboardQuizSummaryDTO();
            dto.setQuizId(quizId);
            dto.setSkillId(skillId);
            dto.setTotalAttempts(((Long) row[1]).intValue());
            dto.setAverageScore((Double) row[2]);
            return dto;
        }).toList();
    }
    public void setPromptedQuiz(Long skillMapId, User user, Long skillId) {
        SkillMap skillMap = skillMapRepository.findById(skillMapId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill map not found"));

        if (!skillMap.getOwnerId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can prompt a quiz");
        }

        CollaborationSession session = sessionRepository.findBySkillMapIdAndIsActiveTrue(skillMapId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active session found"));

        session.setPromptedQuizSkillId(skillId);
        sessionRepository.save(session);
    }
}