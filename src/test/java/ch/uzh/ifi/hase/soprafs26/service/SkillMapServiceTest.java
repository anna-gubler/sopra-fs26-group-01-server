package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.SkillMapRole;
import ch.uzh.ifi.hase.soprafs26.entity.Dependency;
import ch.uzh.ifi.hase.soprafs26.entity.Quiz;
import ch.uzh.ifi.hase.soprafs26.entity.QuizAnswer;
import ch.uzh.ifi.hase.soprafs26.entity.QuizQuestion;
import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMapMembership;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.DependencyRepository;
import ch.uzh.ifi.hase.soprafs26.repository.QuizAnswerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.QuizQuestionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.QuizRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapMembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.DependencyExportDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizAnswerExportDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizExportDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.QuizQuestionExportDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillExportDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapExportDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillMapServiceTest {

    @Mock
    private SkillMapRepository skillMapRepository;

    @Mock
    private SkillMapMembershipRepository skillMapMembershipRepository;

    @Mock
    private SkillRepository skillRepository;
    
    @Mock
    private DependencyRepository dependencyRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private SkillMapService skillMapService;

    @Mock
    private QuizRepository quizRepository;
    @Mock
    private QuizQuestionRepository quizQuestionRepository;
    
    @Mock
    private QuizAnswerRepository quizAnswerRepository;

    // ─── shared fixtures ──────────────────────────────────────────────────────

    private User owner;
    private User otherUser;
    private SkillMap skillMap;
    private SkillMapMembership ownerMembership;

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setId(1L);

        otherUser = new User();
        otherUser.setId(2L);

        skillMap = new SkillMap();
        skillMap.setId(10L);
        skillMap.setTitle("Test Map");
        skillMap.setOwnerId(owner.getId());
        skillMap.setIsPublic(true);
        skillMap.setNumberOfLevels(3);
        skillMap.setInviteCode("INVITE1234");

        ownerMembership = new SkillMapMembership();
        ownerMembership.setId(100L);
        ownerMembership.setUserId(owner.getId());
        ownerMembership.setSkillMapId(skillMap.getId());
        ownerMembership.setRole(SkillMapRole.OWNER);
    }

    // ─── 201  getSkillMaps ────────────────────────────────────────────────────

    @Test
    void getSkillMaps_validUser_returnsMemberMaps() {
        given(skillMapMembershipRepository.findByUserId(owner.getId()))
                .willReturn(List.of(ownerMembership));
        given(skillMapRepository.findAllById(List.of(skillMap.getId())))
                .willReturn(List.of(skillMap));

        List<SkillMap> result = skillMapService.getSkillMaps(owner);

        assertEquals(1, result.size());
        assertEquals(skillMap.getId(), result.get(0).getId());
    }

    @Test
    void getSkillMaps_noMemberships_returnsEmptyList() {
        given(skillMapMembershipRepository.findByUserId(owner.getId()))
                .willReturn(List.of());
        given(skillMapRepository.findAllById(List.of()))
                .willReturn(List.of());

        List<SkillMap> result = skillMapService.getSkillMaps(owner);

        assertTrue(result.isEmpty());
    }

    // ─── 202  createSkillMap ──────────────────────────────────────────────────

    @Test
    void createSkillMap_validInput_returnsCreatedMap() {
        given(skillMapRepository.save(any(SkillMap.class))).willReturn(skillMap);
        given(skillMapRepository.existsByInviteCode(any())).willReturn(false);

        SkillMap input = new SkillMap();
        input.setTitle("Test Map");
        input.setIsPublic(true);
        input.setNumberOfLevels(3);

        SkillMap result = skillMapService.createSkillMap(input, owner);

        assertEquals(skillMap.getId(), result.getId());
        assertEquals(owner.getId(), result.getOwnerId());
        verify(skillMapMembershipRepository).save(any(SkillMapMembership.class));
    }

    @Test
    void createSkillMap_missingTitle_throws400() {
        SkillMap input = new SkillMap();
        input.setIsPublic(true);
        input.setNumberOfLevels(3);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.createSkillMap(input, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createSkillMap_missingIsPublic_throws400() {
        SkillMap input = new SkillMap();
        input.setTitle("Test Map");
        input.setNumberOfLevels(3);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.createSkillMap(input, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createSkillMap_invalidNumberOfLevels_throws400() {
        SkillMap input = new SkillMap();
        input.setTitle("Test Map");
        input.setIsPublic(true);
        input.setNumberOfLevels(0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.createSkillMap(input, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // ─── 203  getSkillMapById ─────────────────────────────────────────────────

    @Test
    void getSkillMapById_publicMap_anyUser_returnsMap() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(false);

        SkillMap result = skillMapService.getSkillMapById(10L, otherUser);

        assertEquals(skillMap.getId(), result.getId());
    }

    @Test
    void getSkillMapById_privateMap_member_returnsMap() {
        skillMap.setIsPublic(false);
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(true);

        SkillMap result = skillMapService.getSkillMapById(10L, otherUser);

        assertEquals(skillMap.getId(), result.getId());
    }

    @Test
    void getSkillMapById_privateMap_nonMember_throws403() {
        skillMap.setIsPublic(false);
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.getSkillMapById(10L, otherUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void getSkillMapById_notFound_throws404() {
        given(skillMapRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.getSkillMapById(99L, owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ─── 204  updateSkillMap ──────────────────────────────────────────────────

    @Test
    void updateSkillMap_validInput_returnsUpdatedMap() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId()))
                .willReturn(true);
        given(skillMapRepository.save(any(SkillMap.class))).willReturn(skillMap);

        SkillMap updates = new SkillMap();
        updates.setTitle("Updated Title");

        SkillMap result = skillMapService.updateSkillMap(10L, updates, owner);

        assertEquals("Updated Title", result.getTitle());
    }

    @Test
    void updateSkillMap_notOwner_throws403() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(true);

        SkillMap updates = new SkillMap();
        updates.setTitle("Hacked Title");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.updateSkillMap(10L, updates, otherUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void updateSkillMap_blankTitle_throws400() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId()))
                .willReturn(true);

        SkillMap updates = new SkillMap();
        updates.setTitle("   ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.updateSkillMap(10L, updates, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void updateSkillMap_description_updatesCorrectly() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId()))
                .willReturn(true);
        given(skillMapRepository.save(any(SkillMap.class))).willAnswer(inv -> inv.getArgument(0));

        SkillMap updates = new SkillMap();
        updates.setDescription("New description");

        SkillMap result = skillMapService.updateSkillMap(10L, updates, owner);

        assertEquals("New description", result.getDescription());
        verify(skillMapRepository).save(skillMap);
    }

    // ─── 205  deleteSkillMap ──────────────────────────────────────────────────

    @Test
    void deleteSkillMap_owner_deletesSuccessfully() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.findBySkillMapId(10L))
                .willReturn(List.of(ownerMembership));

        assertDoesNotThrow(() -> skillMapService.deleteSkillMap(10L, owner));

        verify(skillMapMembershipRepository).deleteAll(List.of(ownerMembership));
        verify(skillMapRepository).deleteById(10L);
    }

    @Test
    void deleteSkillMap_notOwner_throws403() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.deleteSkillMap(10L, otherUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(skillMapRepository, never()).deleteById(any());
    }

    @Test
    void deleteSkillMap_notFound_throws404() {
        given(skillMapRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.deleteSkillMap(99L, owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ─── 206  joinSkillMap ────────────────────────────────────────────────────

    @Test
    void joinSkillMap_validInviteCode_createsMembership() {
        given(skillMapRepository.findByInviteCode("INVITE1234")).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(false);
        given(skillMapMembershipRepository.save(any(SkillMapMembership.class)))
                .willAnswer(inv -> inv.getArgument(0));

        SkillMapMembership result = skillMapService.joinSkillMap("INVITE1234", otherUser);

        assertEquals(SkillMapRole.STUDENT, result.getRole());
        }

    @Test
    void joinSkillMap_wrongInviteCode_throws403() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.joinSkillMap("WRONGCODE", otherUser));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void joinSkillMap_privateMap_throws403() {
        skillMap.setIsPublic(false);
        given(skillMapRepository.findByInviteCode("INVITE1234")).willReturn(Optional.of(skillMap));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.joinSkillMap("INVITE1234", otherUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void joinSkillMap_alreadyMember_throws409() {
        given(skillMapRepository.findByInviteCode("INVITE1234")).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.joinSkillMap("INVITE1234", otherUser));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        }

    // ─── 207  getMembers ─────────────────────────────────────────────────────

    @Test
    void getMembers_validRequest_returnsMemberList() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId()))
                .willReturn(true);
        given(skillMapMembershipRepository.findBySkillMapId(10L))
                .willReturn(List.of(ownerMembership));

        List<SkillMapMembership> result = skillMapService.getMembers(10L, owner);

        assertEquals(1, result.size());
        assertEquals(ownerMembership.getId(), result.get(0).getId());
        }

    // ─── 208  removeMember ───────────────────────────────────────────────────

    @Test
    void removeMember_ownerRemovesOther_succeeds() {
        SkillMapMembership otherMembership = new SkillMapMembership();
        otherMembership.setId(101L);
        otherMembership.setUserId(otherUser.getId());
        otherMembership.setSkillMapId(10L);
        otherMembership.setRole(SkillMapRole.STUDENT);

        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.findBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(Optional.of(otherMembership));

        assertDoesNotThrow(() -> skillMapService.removeMember(10L, otherUser.getId(), owner));
        verify(skillMapMembershipRepository).deleteById(101L);
    }

    @Test
    void removeMember_memberRemovesSelf_succeeds() {
        SkillMapMembership otherMembership = new SkillMapMembership();
        otherMembership.setId(101L);
        otherMembership.setUserId(otherUser.getId());
        otherMembership.setSkillMapId(10L);
        otherMembership.setRole(SkillMapRole.STUDENT);

        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.findBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(Optional.of(otherMembership));

        assertDoesNotThrow(() -> skillMapService.removeMember(10L, otherUser.getId(), otherUser));
        verify(skillMapMembershipRepository).deleteById(101L);
    }

    @Test
    void removeMember_unauthorizedUser_throws403() {
        User thirdUser = new User();
        thirdUser.setId(3L);

        SkillMapMembership otherMembership = new SkillMapMembership();
        otherMembership.setId(101L);
        otherMembership.setUserId(otherUser.getId());
        otherMembership.setSkillMapId(10L);

        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.findBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(Optional.of(otherMembership));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.removeMember(10L, otherUser.getId(), thirdUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(skillMapMembershipRepository, never()).deleteById(any());
    }

    @Test
    void removeMember_membershipNotFound_throws404() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.findBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.removeMember(10L, otherUser.getId(), owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ─── 1101  exportSkillMap ─────────────────────────────────────────────────
    
    @Test
    void exportSkillMap_validRequest_returnsDTOWithCorrectStructure() {
        Skill skill1 = new Skill();
        skill1.setId(1L);
        skill1.setName("Variables");
        skill1.setDescription("Basic variables");
        skill1.setDifficulty("EASY");
        skill1.setLevel(1);

        Skill skill2 = new Skill();
        skill2.setId(2L);
        skill2.setName("Control Flow");
        skill2.setDescription("If/else");
        skill2.setDifficulty("MEDIUM");
        skill2.setLevel(2);

        Dependency dep = new Dependency();
        dep.setFromSkill(skill1);
        dep.setToSkill(skill2); 

        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId()))
                .willReturn(true);
        given(skillRepository.findBySkillMap(skillMap)).willReturn(List.of(skill1, skill2));
        given(dependencyRepository.findByFromSkill_IdIn(List.of(1L, 2L)))
                .willReturn(List.of(dep));
        given(quizRepository.findBySkillId(any())).willReturn(Optional.empty());
        SkillMapExportDTO result = skillMapService.exportSkillMap(10L, owner);

        assertEquals("Test Map", result.getTitle());
        assertEquals(2, result.getSkills().size());
        assertEquals(1, result.getDependencies().size());
        assertEquals("skill-1", result.getSkills().get(0).getExportId());
        assertEquals("skill-2", result.getSkills().get(1).getExportId());
        assertEquals("skill-1", result.getDependencies().get(0).getFromExportId());
        assertEquals("skill-2", result.getDependencies().get(0).getToExportId());
        }

    @Test
    void exportSkillMap_notFound_throws404() {
        given(skillMapRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.exportSkillMap(99L, owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void exportSkillMap_privateMap_nonMember_throws403() {
        skillMap.setIsPublic(false);
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, otherUser.getId()))
                .willReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.exportSkillMap(10L, otherUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void exportSkillMap_noSkillsNoDependencies_returnsEmptyLists() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId()))
                .willReturn(true);
        given(skillRepository.findBySkillMap(skillMap)).willReturn(List.of());
        given(dependencyRepository.findByFromSkill_IdIn(List.of())).willReturn(List.of());
        SkillMapExportDTO result = skillMapService.exportSkillMap(10L, owner);

        assertEquals("Test Map", result.getTitle());
        assertTrue(result.getSkills().isEmpty());
        assertTrue(result.getDependencies().isEmpty());
    }
    // ─── 1102  importSkillMap ─────────────────────────────────────────────────
    private SkillMapExportDTO buildValidImportDTO() {
        SkillExportDTO s1 = new SkillExportDTO();
        s1.setExportId("skill-1");
        s1.setName("Variables");
        s1.setDifficulty("EASY");
        s1.setLevel(1);
        s1.setIsLocked(false);

        SkillExportDTO s2 = new SkillExportDTO();
        s2.setExportId("skill-2");
        s2.setName("Control Flow");
        s2.setDifficulty("MEDIUM");
        s2.setLevel(2);
        s2.setIsLocked(false);

        DependencyExportDTO dep = new DependencyExportDTO();
        dep.setFromExportId("skill-1");
        dep.setToExportId("skill-2");

        SkillMapExportDTO dto = new SkillMapExportDTO();
        dto.setTitle("Imported Map");
        dto.setDescription("Some description");
        dto.setNumberOfLevels(3);
        dto.setIsPublic(false);
        dto.setSkills(List.of(s1, s2));
        dto.setDependencies(List.of(dep));
        return dto;
        }

    @Test
    void importSkillMap_validDTO_createsSkillMapWithSkillsAndDependencies() {
        given(skillMapRepository.existsByInviteCode(any())).willReturn(false);
        given(skillMapRepository.save(any(SkillMap.class))).willReturn(skillMap);
        
        Skill savedSkill1 = new Skill();
        savedSkill1.setId(1L);
        Skill savedSkill2 = new Skill();
        savedSkill2.setId(2L);
        given(skillRepository.save(any(Skill.class)))
                .willReturn(savedSkill1, savedSkill2);
        given(skillRepository.findById(1L)).willReturn(Optional.of(savedSkill1));
        given(skillRepository.findById(2L)).willReturn(Optional.of(savedSkill2));

        SkillMap result = skillMapService.importSkillMap(buildValidImportDTO(), owner);

        assertNotNull(result);
        verify(skillRepository, times(2)).save(any(Skill.class));
        verify(dependencyRepository, times(1)).save(any(Dependency.class));
    }

    @Test
    void importSkillMap_nullNumberOfLevels_defaultsToOne() {
        SkillMapExportDTO dto = new SkillMapExportDTO();
        dto.setTitle("Test");
        dto.setNumberOfLevels(null);
        dto.setIsPublic(false);
        dto.setSkills(List.of());
        dto.setDependencies(List.of());

        given(skillMapRepository.existsByInviteCode(any())).willReturn(false);
        given(skillMapRepository.save(any(SkillMap.class))).willAnswer(inv -> {
            SkillMap m = inv.getArgument(0);
            m.setId(10L);
            return m;
        });

        SkillMap result = skillMapService.importSkillMap(dto, owner);

        assertEquals(1, result.getNumberOfLevels());
    }

    @Test
    void importSkillMap_nullIsPublic_defaultsToFalse() {
        SkillMapExportDTO dto = new SkillMapExportDTO();
        dto.setTitle("Test");
        dto.setNumberOfLevels(3);
        dto.setIsPublic(null);
        dto.setSkills(List.of());
        dto.setDependencies(List.of());

        given(skillMapRepository.existsByInviteCode(any())).willReturn(false);
        given(skillMapRepository.save(any(SkillMap.class))).willAnswer(inv -> {
            SkillMap m = inv.getArgument(0);
            m.setId(10L);
            return m;
        });

        SkillMap result = skillMapService.importSkillMap(dto, owner);

        assertFalse(result.getIsPublic());
    }

    @Test
    void importSkillMap_missingTitle_throws400() {
        SkillMapExportDTO dto = buildValidImportDTO();
        dto.setTitle(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.importSkillMap(dto, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void importSkillMap_nullSkillsList_throws400() {
        SkillMapExportDTO dto = buildValidImportDTO();
        dto.setSkills(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.importSkillMap(dto, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void importSkillMap_brokenEdge_throws400() {
        SkillMapExportDTO dto = buildValidImportDTO();
        DependencyExportDTO brokenDep = new DependencyExportDTO();
        brokenDep.setFromExportId("skill-1");
        brokenDep.setToExportId("skill-99"); // existiert nicht
        dto.setDependencies(List.of(brokenDep));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.importSkillMap(dto, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

        @Test
        void exportSkillMap_skillWithQuiz_includesQuizInExport() {
        Skill skill = new Skill();
        skill.setId(1L);
        skill.setName("Variables");
        skill.setLevel(1);

        Quiz quiz = new Quiz();
        quiz.setId(10L);
        quiz.setSkillId(1L);
        quiz.setIsActive(true);
        quiz.setPassMark(70);

        QuizQuestion question = new QuizQuestion();
        question.setId(100L);
        question.setQuizId(10L);
        question.setQuizQuestionText("What is a variable?");
        question.setOrderIndex(1);

        QuizAnswer answer = new QuizAnswer();
        answer.setId(1000L);
        answer.setQuizQuestionId(100L);
        answer.setAnswerText("A container for data");
        answer.setIsCorrect(true);

        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);
        given(skillRepository.findBySkillMap(skillMap)).willReturn(List.of(skill));
        given(dependencyRepository.findByFromSkill_IdIn(List.of(1L))).willReturn(List.of());
        given(quizRepository.findBySkillId(1L)).willReturn(Optional.of(quiz));
        given(quizQuestionRepository.findByQuizId(10L)).willReturn(List.of(question));
        given(quizAnswerRepository.findByQuizQuestionId(100L)).willReturn(List.of(answer));

        SkillMapExportDTO result = skillMapService.exportSkillMap(10L, owner);

        assertEquals(1, result.getSkills().size());
        assertNotNull(result.getSkills().get(0).getQuiz());
        assertEquals(70, result.getSkills().get(0).getQuiz().getPassMark());
        assertEquals(1, result.getSkills().get(0).getQuiz().getQuestions().size());
        assertEquals("What is a variable?", result.getSkills().get(0).getQuiz().getQuestions().get(0).getQuizQuestionText());
        assertEquals(1, result.getSkills().get(0).getQuiz().getQuestions().get(0).getAnswers().size());
        }

        @Test
        void importSkillMap_skillWithQuiz_createsQuizWithQuestionsAndAnswers() {
                QuizAnswerExportDTO answerDTO = new QuizAnswerExportDTO();
                answerDTO.setAnswerText("A container for data");
                answerDTO.setIsCorrect(true);

                QuizQuestionExportDTO questionDTO = new QuizQuestionExportDTO();
                questionDTO.setQuizQuestionText("What is a variable?");
                questionDTO.setOrderIndex(1);
                questionDTO.setAnswers(List.of(answerDTO));

                QuizExportDTO quizDTO = new QuizExportDTO();
                quizDTO.setIsActive(true);
                quizDTO.setPassMark(70);
                quizDTO.setQuestions(List.of(questionDTO));

                SkillExportDTO skillDTO = new SkillExportDTO();
                skillDTO.setExportId("skill-1");
                skillDTO.setName("Variables");
                skillDTO.setLevel(1);
                skillDTO.setIsLocked(false);
                skillDTO.setQuiz(quizDTO);

                SkillMapExportDTO importDTO = new SkillMapExportDTO();
                importDTO.setTitle("Imported Map");
                importDTO.setNumberOfLevels(1);
                importDTO.setIsPublic(false);
                importDTO.setSkills(List.of(skillDTO));
                importDTO.setDependencies(List.of());

                given(skillMapRepository.existsByInviteCode(any())).willReturn(false);
                given(skillMapRepository.save(any(SkillMap.class))).willReturn(skillMap);

                Skill savedSkill = new Skill();
                savedSkill.setId(1L);
                given(skillRepository.save(any(Skill.class))).willReturn(savedSkill);

                Quiz savedQuiz = new Quiz();
                savedQuiz.setId(10L);
                given(quizRepository.save(any(Quiz.class))).willReturn(savedQuiz);

                QuizQuestion savedQuestion = new QuizQuestion();
                savedQuestion.setId(100L);
                given(quizQuestionRepository.save(any(QuizQuestion.class))).willReturn(savedQuestion);

                skillMapService.importSkillMap(importDTO, owner);

                verify(quizRepository, times(1)).save(any(Quiz.class));
                verify(quizQuestionRepository, times(1)).save(any(QuizQuestion.class));
                verify(quizAnswerRepository, times(1)).save(any(QuizAnswer.class));
        }

    @Test
    void importSkillMap_nullIsLockedAndNullIsActiveAndNullIsCorrect_defaultToFalse() {
        QuizAnswerExportDTO answerDTO = new QuizAnswerExportDTO();
        answerDTO.setAnswerText("answer");
        answerDTO.setIsCorrect(null);

        QuizQuestionExportDTO questionDTO = new QuizQuestionExportDTO();
        questionDTO.setQuizQuestionText("question");
        questionDTO.setOrderIndex(1);
        questionDTO.setAnswers(List.of(answerDTO));

        QuizExportDTO quizDTO = new QuizExportDTO();
        quizDTO.setIsActive(null);
        quizDTO.setPassMark(70);
        quizDTO.setQuestions(List.of(questionDTO));

        SkillExportDTO skillDTO = new SkillExportDTO();
        skillDTO.setExportId("skill-null");
        skillDTO.setName("NullFields");
        skillDTO.setLevel(1);
        skillDTO.setIsLocked(null);
        skillDTO.setQuiz(quizDTO);

        SkillMapExportDTO importDTO = new SkillMapExportDTO();
        importDTO.setTitle("Null Fields Map");
        importDTO.setNumberOfLevels(1);
        importDTO.setIsPublic(false);
        importDTO.setSkills(List.of(skillDTO));
        importDTO.setDependencies(List.of());

        given(skillMapRepository.existsByInviteCode(any())).willReturn(false);
        given(skillMapRepository.save(any(SkillMap.class))).willReturn(skillMap);

        Skill savedSkill = new Skill();
        savedSkill.setId(99L);
        given(skillRepository.save(any(Skill.class))).willReturn(savedSkill);

        Quiz savedQuiz = new Quiz();
        savedQuiz.setId(10L);
        given(quizRepository.save(any(Quiz.class))).willReturn(savedQuiz);

        QuizQuestion savedQuestion = new QuizQuestion();
        savedQuestion.setId(100L);
        given(quizQuestionRepository.save(any(QuizQuestion.class))).willReturn(savedQuestion);

        skillMapService.importSkillMap(importDTO, owner);

        verify(quizAnswerRepository, times(1)).save(any(QuizAnswer.class));
    }

    // ─── updateSkillMap numberOfLevels ──────────────────────────────────────────

    @Test
    void updateSkillMap_validNumberOfLevels_updatesSuccessfully() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);
        given(skillMapRepository.save(any(SkillMap.class))).willAnswer(inv -> inv.getArgument(0));

        SkillMap updates = new SkillMap();
        updates.setNumberOfLevels(5);

        SkillMap result = skillMapService.updateSkillMap(10L, updates, owner);

        assertEquals(5, result.getNumberOfLevels());
    }

    @Test
    void updateSkillMap_invalidNumberOfLevels_throws400() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);

        SkillMap updates = new SkillMap();
        updates.setNumberOfLevels(0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.updateSkillMap(10L, updates, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // ─── joinSkillMap null/blank inviteCode ─────────────────────────────────────

    @Test
    void joinSkillMap_nullInviteCode_throws400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.joinSkillMap(null, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void joinSkillMap_blankInviteCode_throws400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.joinSkillMap("   ", owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // ─── removeMember skillMap not found ─────────────────────────────────────────

    @Test
    void removeMember_skillMapNotFound_throws404() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.removeMember(10L, otherUser.getId(), owner));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ─── getSkillMapGraph ─────────────────────────────────────────────────────────

    @Test
    void getSkillMapGraph_returnsDTOWithSkillsAndDependencies() {
        given(skillMapRepository.findById(10L)).willReturn(Optional.of(skillMap));
        given(skillMapMembershipRepository.existsBySkillMapIdAndUserId(10L, owner.getId())).willReturn(true);

        Skill skill = new Skill();
        skill.setId(5L);
        skill.setName("Skill A");
        skill.setSkillMap(skillMap);
        given(skillRepository.findBySkillMap(skillMap)).willReturn(List.of(skill));
        given(dependencyRepository.findByFromSkill_IdIn(List.of(5L))).willReturn(List.of());

        ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapGraphDTO graph =
                skillMapService.getSkillMapGraph(10L, owner);

        assertNotNull(graph);
        assertEquals(10L, graph.getSkillMapId());
        assertEquals("Test Map", graph.getTitle());
        assertEquals(1, graph.getSkills().size());
        assertTrue(graph.getDependencies().isEmpty());
    }
}