package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.SkillMapRole;
import ch.uzh.ifi.hase.soprafs26.entity.Dependency;
import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMapMembership;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import ch.uzh.ifi.hase.soprafs26.rest.dto.DependencyExportDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillExportDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapExportDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SkillMapServiceIntegrationTest {

    @Autowired
    private SkillMapService skillMapService;

    @Autowired
    private SkillMapRepository skillMapRepository;

    @Autowired
    private SkillMapMembershipRepository skillMapMembershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private DependencyRepository dependencyRepository;

    @Autowired
    private SkillRepository skillRepository;

    private User owner;
    private User student;
    private SkillMap skillMap;

    @BeforeEach
    void setup() {
        skillMapMembershipRepository.deleteAll();
        dependencyRepository.deleteAll();
        skillRepository.deleteAll();
        skillMapRepository.deleteAll(); 
        userRepository.deleteAll();  

        User ownerInput = new User();
        ownerInput.setUsername("owner");
        ownerInput.setPassword("password123");
        owner = userService.createUser(ownerInput);

        User studentInput = new User();
        studentInput.setUsername("student");
        studentInput.setPassword("password456");
        student = userService.createUser(studentInput);

        SkillMap mapInput = new SkillMap();
        mapInput.setTitle("Test Map");
        mapInput.setIsPublic(true);
        mapInput.setNumberOfLevels(3);
        skillMap = skillMapService.createSkillMap(mapInput, owner);
    }

    // createSkillMap
    @Test
    void createSkillMap_persistedCorrectlyWithOwnerMembership() {
        SkillMap input = new SkillMap();
        input.setTitle("Integration Map");
        input.setIsPublic(true);
        input.setNumberOfLevels(3);

        SkillMap result = skillMapService.createSkillMap(input, owner);

        assertNotNull(result.getId());
        assertEquals("Integration Map", result.getTitle());
        assertTrue(skillMapRepository.findById(result.getId()).isPresent());
        assertTrue(skillMapMembershipRepository
                .existsBySkillMapIdAndUserId(result.getId(), owner.getId()));
    }

    // deleteSkillMap
    @Test
    void deleteSkillMap_removedFromDatabaseWithMemberships() {
        SkillMap input = new SkillMap();
        input.setTitle("To Delete");
        input.setIsPublic(true);
        input.setNumberOfLevels(2);
        SkillMap saved = skillMapService.createSkillMap(input, owner);
        Long mapId = saved.getId();

        skillMapService.deleteSkillMap(mapId, owner);

        assertFalse(skillMapRepository.findById(mapId).isPresent());
        assertTrue(skillMapMembershipRepository.findBySkillMapId(mapId).isEmpty());
    }

    // joinSkillMap
    @Test
    void joinSkillMap_withValidInviteCode_createsMembershipWithStudentRole() {
        SkillMapMembership membership = skillMapService.joinSkillMap(
                skillMap.getInviteCode(), student);

        assertEquals(student.getId(), membership.getUserId());
        assertEquals(skillMap.getId(), membership.getSkillMapId());
        assertEquals(SkillMapRole.STUDENT, membership.getRole());
    }

    @Test
    void joinSkillMap_withValidInviteCode_skillMapAppearsInStudentsMapList() {
        skillMapService.joinSkillMap(skillMap.getInviteCode(), student);

        List<SkillMap> maps = skillMapService.getSkillMaps(student);

        assertEquals(1, maps.size());
    }

    @Test
    void joinSkillMap_withWrongInviteCode_Forbidden() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                skillMapService.joinSkillMap("WRONGCODE1", student));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void joinSkillMap_whenStudentAlreadyMember_throwsConflict() {
        skillMapService.joinSkillMap(skillMap.getInviteCode(), student);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                skillMapService.joinSkillMap(skillMap.getInviteCode(), student));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void joinSkillMap_whenOwnerTriesToJoinOwnMap_throwsConflict() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                skillMapService.joinSkillMap(skillMap.getInviteCode(), owner));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    // getSkillMaps
    @Test
    void getSkillMaps_whenStudentHasNotJoinedAnyMap_returnsEmptyList() {
        List<SkillMap> maps = skillMapService.getSkillMaps(student);

        assertTrue(maps.isEmpty());
    }

    @Test
    void getSkillMaps_whenCalledByOwner_returnsOwnedMap() {
        List<SkillMap> maps = skillMapService.getSkillMaps(owner);

        assertEquals(1, maps.size());
        assertEquals(skillMap.getId(), maps.get(0).getId());
    }

    // inviteCode generation
    @Test
    void createSkillMap_inviteCode_isAutoGenerated() {
        assertNotNull(skillMap.getInviteCode());
    }

    @Test
    void createSkillMap_inviteCode_isFourCharsUppercase() {
        String code = skillMap.getInviteCode();
        assertEquals(4, code.length());
        assertEquals(code.toUpperCase(), code);
    }

    @Test
    void createSkillMap_twoMaps_haveDistinctInviteCodes() {
        SkillMap mapInput = new SkillMap();
        mapInput.setTitle("Second Map");
        mapInput.setIsPublic(false);
        mapInput.setNumberOfLevels(2);
        SkillMap second = skillMapService.createSkillMap(mapInput, owner);

        assertNotEquals(skillMap.getInviteCode(), second.getInviteCode());
    }

    // private map access control
    @Test
    void getSkillMapById_privateMap_nonMember_throwsForbidden() {
        SkillMap privateMapInput = new SkillMap();
        privateMapInput.setTitle("Private Map");
        privateMapInput.setIsPublic(false);
        privateMapInput.setNumberOfLevels(2);
        SkillMap privateMap = skillMapService.createSkillMap(privateMapInput, owner);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                skillMapService.getSkillMapById(privateMap.getId(), student));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void getSkillMapById_privateMap_member_succeeds() {
        skillMapService.joinSkillMap(skillMap.getInviteCode(), student);

        SkillMap result = skillMapService.getSkillMapById(skillMap.getId(), student);

        assertEquals(skillMap.getId(), result.getId());
    }

    @Test
    void getSkillMapById_publicMap_nonMember_succeeds() {
        SkillMap mapInput = new SkillMap();
        mapInput.setTitle("Public Map");
        mapInput.setIsPublic(true);
        mapInput.setNumberOfLevels(2);
        SkillMap publicMap = skillMapService.createSkillMap(mapInput, owner);

        SkillMap result = skillMapService.getSkillMapById(publicMap.getId(), student);

        assertEquals(publicMap.getId(), result.getId());
    }

    // privacy toggle
    @Test
    void updateSkillMap_privateToPublic_nonMemberCanAccess() {
        SkillMap updates = new SkillMap();
        updates.setIsPublic(true);
        skillMapService.updateSkillMap(skillMap.getId(), updates, owner);

        SkillMap result = skillMapService.getSkillMapById(skillMap.getId(), student);

        assertEquals(skillMap.getId(), result.getId());
    }

    @Test
    void updateSkillMap_publicToPrivate_nonMemberForbidden() {
        SkillMap mapInput = new SkillMap();
        mapInput.setTitle("Public Map");
        mapInput.setIsPublic(true);
        mapInput.setNumberOfLevels(2);
        SkillMap publicMap = skillMapService.createSkillMap(mapInput, owner);

        SkillMap updates = new SkillMap();
        updates.setIsPublic(false);
        skillMapService.updateSkillMap(publicMap.getId(), updates, owner);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                skillMapService.getSkillMapById(publicMap.getId(), student));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // exportSkillMap
    @Test
    void exportSkillMap_withSkillsAndDependencies_returnsCorrectStructure() {
        Skill s1 = new Skill();
        s1.setName("Variables");
        s1.setSkillMap(skillMap);
        s1.setLevel(1);
        s1.setIsLocked(false);
        s1.setDifficulty("EASY");
        s1 = skillRepository.save(s1);

        Skill s2 = new Skill();
        s2.setName("Control Flow");
        s2.setSkillMap(skillMap);
        s2.setLevel(2);
        s2.setIsLocked(false);
        s2.setDifficulty("MEDIUM");
        s2 = skillRepository.save(s2);

        Dependency dep = new Dependency();
        dep.setFromSkill(s1);
        dep.setToSkill(s2); 
        dependencyRepository.save(dep);

        SkillMapExportDTO result = skillMapService.exportSkillMap(skillMap.getId(), owner);

        assertEquals(skillMap.getTitle(), result.getTitle());
        assertEquals(2, result.getSkills().size());
        assertEquals(1, result.getDependencies().size());
        // keine internen IDs im Export
        assertTrue(result.getSkills().stream()
                .allMatch(s -> s.getExportId().startsWith("skill-")));
        // dependency referenziert korrekte exportIds
        String fromId = result.getDependencies().get(0).getFromExportId();
        String toId = result.getDependencies().get(0).getToExportId();
        assertTrue(result.getSkills().stream().anyMatch(s -> s.getExportId().equals(fromId)));
        assertTrue(result.getSkills().stream().anyMatch(s -> s.getExportId().equals(toId)));
    }

    @Test
    void exportSkillMap_nonMember_throws403() {
        SkillMap privateMap = new SkillMap();
        privateMap.setTitle("Private Map");
        privateMap.setIsPublic(false);
        privateMap.setNumberOfLevels(2);
        SkillMap saved = skillMapService.createSkillMap(privateMap, owner);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.exportSkillMap(saved.getId(), student));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // importSkillMap
    @Test
    void importSkillMap_roundTrip_preservesStructure() {
        // Erst eine Map mit Skills und Dependencies aufbauen
        Skill s1 = new Skill();
        s1.setName("Variables");
        s1.setSkillMap(skillMap);
        s1.setLevel(1);
        s1.setIsLocked(false);
        s1.setDifficulty("EASY");
        s1 = skillRepository.save(s1);

        Skill s2 = new Skill();
        s2.setName("Control Flow");
        s2.setSkillMap(skillMap);
        s2.setLevel(2);
        s2.setIsLocked(false);
        s2.setDifficulty("MEDIUM");
        s2 = skillRepository.save(s2);

        Dependency dep = new Dependency();
        dep.setFromSkill(s1);
        dep.setToSkill(s2);
        dependencyRepository.save(dep);

        // Export
        SkillMapExportDTO exportDTO = skillMapService.exportSkillMap(skillMap.getId(), owner);

        // Import
        SkillMap importedMap = skillMapService.importSkillMap(exportDTO, owner);

        // Struktur prüfen
        assertNotNull(importedMap.getId());
        assertNotEquals(skillMap.getId(), importedMap.getId()); // neue ID
        assertEquals(skillMap.getTitle(), importedMap.getTitle());

        List<Skill> importedSkills = skillRepository.findBySkillMap(importedMap);
        assertEquals(2, importedSkills.size());

        List<Long> importedSkillIds = importedSkills.stream()
                .map(Skill::getId).collect(Collectors.toList());
        List<Dependency> importedDeps = dependencyRepository
                .findByFromSkill_IdIn(importedSkillIds);
        assertEquals(1, importedDeps.size());
    }

    @Test
    void importSkillMap_brokenEdge_throws400() {
        SkillExportDTO skillDTO = new SkillExportDTO();
        skillDTO.setExportId("skill-1");
        skillDTO.setName("Variables");
        skillDTO.setDifficulty("EASY");
        skillDTO.setLevel(1);
        skillDTO.setIsLocked(false);

        DependencyExportDTO brokenDep = new DependencyExportDTO();
        brokenDep.setFromExportId("skill-1");
        brokenDep.setToExportId("skill-99");

        SkillMapExportDTO dto = new SkillMapExportDTO();
        dto.setTitle("Broken Map");
        dto.setNumberOfLevels(1);
        dto.setIsPublic(false);
        dto.setSkills(List.of(skillDTO));
        dto.setDependencies(List.of(brokenDep));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> skillMapService.importSkillMap(dto, owner));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}
