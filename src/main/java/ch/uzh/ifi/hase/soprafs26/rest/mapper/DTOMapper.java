package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs26.entity.Dependency;
import ch.uzh.ifi.hase.soprafs26.entity.Quiz;
import ch.uzh.ifi.hase.soprafs26.entity.StudentProgress;
import ch.uzh.ifi.hase.soprafs26.rest.dto.StudentProgressDTO;
import ch.uzh.ifi.hase.soprafs26.entity.QuizAnswer;
import ch.uzh.ifi.hase.soprafs26.entity.QuizAttempt;
import ch.uzh.ifi.hase.soprafs26.entity.QuizQuestion;
import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMapMembership;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.DependencyGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizAnswerGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizAnswerPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizAttemptGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizQuestionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizQuestionPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapMembershipGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPatchDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutAvatarDTO;

@Mapper
public interface DTOMapper {

    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

    // User mappings
    @Mapping(source = "username", target = "username")
    @Mapping(source = "bio", target = "bio")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(source = "password", target = "password")
    @Mapping(target = "style", ignore = true)
    @Mapping(target = "seed", ignore = true)
    @Mapping(target = "hasSeenDashboard", ignore = true)
    @Mapping(target = "hasSeenMapAsMember", ignore = true)
    @Mapping(target = "hasSeenMapAsOwner", ignore = true)
    User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "bio", target = "bio")
    @Mapping(source = "creationDate", target = "creationDate")
    @Mapping(source = "token", target = "token", ignore = true)
    @Mapping(source = "seed", target = "seed")
    @Mapping(source = "style", target = "style")
    UserGetDTO convertEntityToUserGetDTO(User user);


    @Mapping(source = "username", target = "username")
    @Mapping(source = "bio", target = "bio")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "token", target = "token")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(source = "password", target = "password")
    @Mapping(target = "style", ignore = true)
    @Mapping(target = "seed", ignore = true)
    @Mapping(target = "hasSeenDashboard", ignore = true)
    @Mapping(target = "hasSeenMapAsMember", ignore = true)
    @Mapping(target = "hasSeenMapAsOwner", ignore = true)
    User convertUserPutDTOtoEntity(UserPutDTO userPutDTO);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "bio", target = "bio")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "token", target = "token")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(source = "password", target = "password")
    @Mapping(target = "style", ignore = true)
    @Mapping(target = "seed", ignore = true)
    @Mapping(source = "hasSeenDashboard", target = "hasSeenDashboard")
    @Mapping(source = "hasSeenMapAsMember", target = "hasSeenMapAsMember")
    @Mapping(source = "hasSeenMapAsOwner", target = "hasSeenMapAsOwner")
    User convertUserPatchDTOtoEntity(UserPatchDTO userPatchDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(source = "style", target = "style")
    @Mapping(source = "seed", target = "seed")
    @Mapping(target = "hasSeenDashboard", ignore = true)
    @Mapping(target = "hasSeenMapAsMember", ignore = true)
    @Mapping(target = "hasSeenMapAsOwner", ignore = true)
    User convertUserPutAvatarDTOtoEntity(UserPutAvatarDTO userPutAvatarDTO);

    // Skill mappings
    @Mapping(source = "skillMap.id", target = "skillMapId")
    SkillGetDTO convertEntityToSkillGetDTO(Skill skill);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "skillMap", ignore = true)
    @Mapping(target = "isLocked", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Skill convertSkillPostDTOtoEntity(SkillPostDTO skillPostDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "skillMap", ignore = true)
    @Mapping(target = "isLocked", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Skill convertSkillPutDTOtoEntity(SkillPutDTO skillPutDTO);

    // SkillMap mappings
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "isPublic", target = "isPublic")
    @Mapping(source = "numberOfLevels", target = "numberOfLevels")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "inviteCode", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SkillMap convertSkillMapPostDTOtoEntity(SkillMapPostDTO skillMapPostDTO);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "isPublic", target = "isPublic")
    @Mapping(source = "numberOfLevels", target = "numberOfLevels")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "inviteCode", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SkillMap convertSkillMapPutDTOtoEntity(SkillMapPutDTO skillMapPutDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "isPublic", target = "isPublic")
    @Mapping(source = "inviteCode", target = "inviteCode")
    @Mapping(source = "numberOfLevels", target = "numberOfLevels")
    @Mapping(source = "ownerId", target = "ownerId")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    SkillMapGetDTO convertEntityToSkillMapGetDTO(SkillMap skillMap);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "skillMapId", target = "skillMapId")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "joinedAt", target = "joinedAt")
    SkillMapMembershipGetDTO convertEntityToSkillMapMembershipGetDTO(SkillMapMembership membership);

    // Dependencies
    @Mapping(source = "fromSkill.id", target = "fromSkillId")
    @Mapping(source = "toSkill.id", target = "toSkillId")
    DependencyGetDTO convertDependencyEntityToGetDTO(Dependency dependency);

    // Quiz
    @Mapping(source = "id", target = "id")
    QuizGetDTO convertQuizEntityToQuizGetDTO(Quiz quiz);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "skillId", ignore = true)
    Quiz convertQuizPostDTOToQuizEntity(QuizPostDTO quizPostDTO);

    // QuizQuestion
    @Mapping(source = "id", target = "id")
    QuizQuestionGetDTO convertQuizQuestionEntityToQuizQuestionGetDTO(QuizQuestion quizQuestion);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "quizId", ignore = true)
    QuizQuestion convertQuizQuestionPostDTOToQuizQuestionEntity(QuizQuestionPostDTO quizQuestionPostDTO);

    // QuizAnswer
    @Mapping(source = "id", target = "id")
    QuizAnswerGetDTO convertQuizAnswerEntityToQuizAnswerGetDTO(QuizAnswer quizAnswer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "quizQuestionId", ignore = true)
    QuizAnswer convertQuizAnswerPostDTOToQuizAnswerEntity(QuizAnswerPostDTO quizAnswerPostDTO);

    // QuizAttempt
    @Mapping(source = "id", target = "id")
    QuizAttemptGetDTO convertQuizAttemptEntityToQuizAttemptGetDTO(QuizAttempt quizAttempt);

    // StudentProgress
    StudentProgressDTO convertStudentProgressToDTO(StudentProgress studentProgress);

}