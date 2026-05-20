package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.entity.LiveQuestion;

import java.time.LocalDateTime;
import java.util.List;

public class SessionStateDTO {
    private Long id;
    private Long skillMapId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private boolean isActive;
    private Long promptedQuizSkillId;
    private List<LiveQuestion> questions;
    private List<SkillRatingSummaryDTO> skillRatings;
    private List<DashboardQuizSummaryDTO> quizResults;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSkillMapId() { return skillMapId; }
    public void setSkillMapId(Long skillMapId) { this.skillMapId = skillMapId; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }

    public Long getPromptedQuizSkillId() { return promptedQuizSkillId; }
    public void setPromptedQuizSkillId(Long promptedQuizSkillId) { this.promptedQuizSkillId = promptedQuizSkillId; }

    public List<LiveQuestion> getQuestions() { return questions; }
    public void setQuestions(List<LiveQuestion> questions) { this.questions = questions; }

    public List<SkillRatingSummaryDTO> getSkillRatings() { return skillRatings; }
    public void setSkillRatings(List<SkillRatingSummaryDTO> skillRatings) { this.skillRatings = skillRatings; }

    public List<DashboardQuizSummaryDTO> getQuizResults() { return quizResults; }
    public void setQuizResults(List<DashboardQuizSummaryDTO> quizResults) { this.quizResults = quizResults; }
}
