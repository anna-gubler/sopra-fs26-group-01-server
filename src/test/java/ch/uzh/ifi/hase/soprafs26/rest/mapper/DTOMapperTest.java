package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.constant.SkillMapRole;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Dependency;
import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMapMembership;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.DependencyGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapMembershipGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPatchDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutAvatarDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutDTO;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class DTOMapperTest {

	private static final String USERNAME = "firstname@lastname";
	private static final String BIO = "Hey there! I'm using Whatsapp.";
	private static final String TOKEN = "6dd696b4-83a2-42a6-8769-e2d755c6b8b8";
	private static final String PASSWORD = "somePassword123";
	private static final LocalDateTime CREATION_DATE = LocalDateTime.of(2026, 1, 1, 0, 0);

	@Test
	public void shouldConvertUserPostDTOToEntity() {
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername(USERNAME);
		userPostDTO.setBio(BIO);
		userPostDTO.setPassword(PASSWORD);

		User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		assertAll(
				() -> assertEquals(userPostDTO.getUsername(), user.getUsername()),
				() -> assertEquals(userPostDTO.getBio(), user.getBio()),
				() -> assertEquals(userPostDTO.getPassword(), user.getPassword()));
	}

	@Test
	public void shouldReturnNullWhenConvertingNullUserPostDTO() {
		assertNull(DTOMapper.INSTANCE.convertUserPostDTOtoEntity(null));
	}

	@Test
	public void shouldConvertEntityToUserGetDTO() {
		User user = new User();
		user.setId(1L);
		user.setUsername(USERNAME);
		user.setBio(BIO);
		user.setToken(TOKEN);
		user.setStatus(UserStatus.OFFLINE);
		user.setCreationDate(CREATION_DATE);
		user.setHasSeenDashboard(true);
		user.setHasSeenMapAsMember(true);
		user.setHasSeenMapAsOwner(true);

		UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

		assertAll(
				() -> assertEquals(user.getId(), userGetDTO.getId()),
				() -> assertEquals(user.getUsername(), userGetDTO.getUsername()),
				() -> assertEquals(user.getStatus(), userGetDTO.getStatus()),
				() -> assertEquals(user.getBio(), userGetDTO.getBio()),
				() -> assertEquals(user.getCreationDate(), userGetDTO.getCreationDate()),
				() -> assertTrue(userGetDTO.isHasSeenDashboard()),
				() -> assertTrue(userGetDTO.isHasSeenMapAsMember()),
				() -> assertTrue(userGetDTO.isHasSeenMapAsOwner()));
	}

	@Test
	public void shouldReturnNullWhenConvertingNullUserEntity() {
		assertNull(DTOMapper.INSTANCE.convertEntityToUserGetDTO(null));
	}

	@Test
	public void shouldConvertUserPutDTOToEntity() {
		UserPutDTO userPutDTO = new UserPutDTO();
		userPutDTO.setId(1L);
		userPutDTO.setUsername(USERNAME);
		userPutDTO.setBio(BIO);
		userPutDTO.setPassword(PASSWORD);
		userPutDTO.setToken(TOKEN);

		User user = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);

		assertAll(
				() -> assertEquals(userPutDTO.getId(), user.getId()),
				() -> assertEquals(userPutDTO.getUsername(), user.getUsername()),
				() -> assertEquals(userPutDTO.getBio(), user.getBio()),
				() -> assertEquals(userPutDTO.getPassword(), user.getPassword()),
				() -> assertEquals(userPutDTO.getToken(), user.getToken()));
	}

	@Test
	public void shouldReturnNullWhenConvertingNullUserPutDTO() {
		assertNull(DTOMapper.INSTANCE.convertUserPutDTOtoEntity(null));
	}

	@Test
	public void shouldConvertUserPatchDTOToEntity() {
		UserPatchDTO userPatchDTO = new UserPatchDTO();
		userPatchDTO.setUsername(USERNAME);
		userPatchDTO.setBio(BIO);
		userPatchDTO.setPassword(PASSWORD);
		userPatchDTO.setToken(TOKEN);

		User user = DTOMapper.INSTANCE.convertUserPatchDTOtoEntity(userPatchDTO);

		assertAll(
				() -> assertEquals(userPatchDTO.getUsername(), user.getUsername()),
				() -> assertEquals(userPatchDTO.getBio(), user.getBio()),
				() -> assertEquals(userPatchDTO.getPassword(), user.getPassword()),
				() -> assertEquals(userPatchDTO.getToken(), user.getToken()));
	}

	@Test
	public void shouldConvertUserPatchDTOWithHasSeenFlagsToEntity() {
		UserPatchDTO userPatchDTO = new UserPatchDTO();
		userPatchDTO.setHasSeenDashboard(true);
		userPatchDTO.setHasSeenMapAsMember(true);
		userPatchDTO.setHasSeenMapAsOwner(true);

		User user = DTOMapper.INSTANCE.convertUserPatchDTOtoEntity(userPatchDTO);

		assertAll(
				() -> assertTrue(user.isHasSeenDashboard()),
				() -> assertTrue(user.isHasSeenMapAsMember()),
				() -> assertTrue(user.isHasSeenMapAsOwner()));
	}

	@Test
	public void shouldReturnNullWhenConvertingNullUserPatchDTO() {
		assertNull(DTOMapper.INSTANCE.convertUserPatchDTOtoEntity(null));
	}

	@Test
	public void shouldConvertUserPutAvatarDTOToEntity() {
		UserPutAvatarDTO dto = new UserPutAvatarDTO();
		dto.setStyle("style1");
		dto.setSeed("seed1");

		User user = DTOMapper.INSTANCE.convertUserPutAvatarDTOtoEntity(dto);

		assertAll(
				() -> assertEquals("style1", user.getStyle()),
				() -> assertEquals("seed1", user.getSeed()));
	}

	@Test
	public void shouldReturnNullWhenConvertingNullUserPutAvatarDTO() {
		assertNull(DTOMapper.INSTANCE.convertUserPutAvatarDTOtoEntity(null));
	}

	@Test
	public void shouldConvertSkillPostDTOToEntity() {
		SkillPostDTO dto = new SkillPostDTO();
		dto.setName("Java Basics");
		dto.setDescription("Intro to Java");
		dto.setResources("book.pdf");
		dto.setDifficulty("EASY");
		dto.setLevel(1);
		dto.setPositionX(1.5f);
		dto.setNotes("important notes");

		Skill skill = DTOMapper.INSTANCE.convertSkillPostDTOtoEntity(dto);

		assertAll(
				() -> assertEquals("Java Basics", skill.getName()),
				() -> assertEquals("Intro to Java", skill.getDescription()),
				() -> assertEquals("book.pdf", skill.getResources()),
				() -> assertEquals("EASY", skill.getDifficulty()),
				() -> assertEquals(1, skill.getLevel()),
				() -> assertEquals(1.5f, skill.getPositionX()),
				() -> assertEquals("important notes", skill.getNotes()));
	}

	@Test
	public void shouldReturnNullWhenConvertingNullSkillPostDTO() {
		assertNull(DTOMapper.INSTANCE.convertSkillPostDTOtoEntity(null));
	}

	@Test
	public void shouldConvertSkillPutDTOToEntity() {
		SkillPutDTO dto = new SkillPutDTO();
		dto.setName("Updated Skill");
		dto.setDescription("Updated desc");
		dto.setResources("updated.pdf");
		dto.setDifficulty("HARD");
		dto.setLevel(3);
		dto.setPositionX(2.0f);
		dto.setNotes("updated notes");

		Skill skill = DTOMapper.INSTANCE.convertSkillPutDTOtoEntity(dto);

		assertAll(
				() -> assertEquals("Updated Skill", skill.getName()),
				() -> assertEquals("Updated desc", skill.getDescription()),
				() -> assertEquals("updated.pdf", skill.getResources()),
				() -> assertEquals("HARD", skill.getDifficulty()),
				() -> assertEquals(3, skill.getLevel()),
				() -> assertEquals(2.0f, skill.getPositionX()),
				() -> assertEquals("updated notes", skill.getNotes()));
	}

	@Test
	public void shouldReturnNullWhenConvertingNullSkillPutDTO() {
		assertNull(DTOMapper.INSTANCE.convertSkillPutDTOtoEntity(null));
	}

	@Test
	public void shouldConvertEntityToSkillGetDTO() {
		SkillMap skillMap = new SkillMap();
		skillMap.setId(42L);

		Skill skill = new Skill();
		skill.setId(5L);
		skill.setName("OOP");
		skill.setDescription("Object-Oriented");
		skill.setResources("oop.pdf");
		skill.setDifficulty("MEDIUM");
		skill.setLevel(2);
		skill.setPositionX(3.0f);
		skill.setIsLocked(false);
		skill.setNotes("some notes");
		skill.setSkillMap(skillMap);

		SkillGetDTO dto = DTOMapper.INSTANCE.convertEntityToSkillGetDTO(skill);

		assertAll(
				() -> assertEquals(5L, dto.getId()),
				() -> assertEquals("OOP", dto.getName()),
				() -> assertEquals(42L, dto.getSkillMapId()),
				() -> assertEquals("oop.pdf", dto.getResources()),
				() -> assertEquals("some notes", dto.getNotes()));
	}

	@Test
	public void shouldConvertEntityToSkillGetDTOWithNullSkillMap() {
		Skill skill = new Skill();
		skill.setId(5L);
		skill.setName("Skill");
		skill.setSkillMap(null);

		SkillGetDTO dto = DTOMapper.INSTANCE.convertEntityToSkillGetDTO(skill);

		assertNull(dto.getSkillMapId());
	}

	@Test
	public void shouldReturnNullWhenConvertingNullSkillEntity() {
		assertNull(DTOMapper.INSTANCE.convertEntityToSkillGetDTO(null));
	}

	@Test
	public void shouldConvertEntityToSkillMapGetDTO() {
		SkillMap skillMap = new SkillMap();
		skillMap.setId(10L);
		skillMap.setTitle("My Map");
		skillMap.setDescription("desc");
		skillMap.setIsPublic(true);
		skillMap.setInviteCode("ABC123");
		skillMap.setNumberOfLevels(5);
		skillMap.setOwnerId(1L);

		SkillMapGetDTO dto = DTOMapper.INSTANCE.convertEntityToSkillMapGetDTO(skillMap);

		assertAll(
				() -> assertEquals(10L, dto.getId()),
				() -> assertEquals("My Map", dto.getTitle()),
				() -> assertEquals("desc", dto.getDescription()),
				() -> assertTrue(dto.getIsPublic()),
				() -> assertEquals("ABC123", dto.getInviteCode()),
				() -> assertEquals(5, dto.getNumberOfLevels()),
				() -> assertEquals(1L, dto.getOwnerId()));
	}

	@Test
	public void shouldReturnNullWhenConvertingNullSkillMapEntity() {
		assertNull(DTOMapper.INSTANCE.convertEntityToSkillMapGetDTO(null));
	}

	@Test
	public void shouldConvertEntityToSkillMapMembershipGetDTO() {
		SkillMapMembership membership = new SkillMapMembership();
		membership.setId(1L);
		membership.setUserId(2L);
		membership.setSkillMapId(3L);
		membership.setRole(SkillMapRole.STUDENT);
		membership.setJoinedAt(CREATION_DATE);

		SkillMapMembershipGetDTO dto = DTOMapper.INSTANCE.convertEntityToSkillMapMembershipGetDTO(membership);

		assertAll(
				() -> assertEquals(1L, dto.getId()),
				() -> assertEquals(2L, dto.getUserId()),
				() -> assertEquals(3L, dto.getSkillMapId()),
				() -> assertEquals(SkillMapRole.STUDENT, dto.getRole()),
				() -> assertEquals(CREATION_DATE, dto.getJoinedAt()));
	}

	@Test
	public void shouldReturnNullWhenConvertingNullMembership() {
		assertNull(DTOMapper.INSTANCE.convertEntityToSkillMapMembershipGetDTO(null));
	}

	@Test
	public void shouldConvertDependencyEntityToGetDTO() {
		Skill fromSkill = new Skill();
		fromSkill.setId(1L);

		Skill toSkill = new Skill();
		toSkill.setId(2L);

		Dependency dependency = new Dependency();
		dependency.setId(10L);
		dependency.setFromSkill(fromSkill);
		dependency.setToSkill(toSkill);

		DependencyGetDTO dto = DTOMapper.INSTANCE.convertDependencyEntityToGetDTO(dependency);

		assertAll(
				() -> assertEquals(10L, dto.getId()),
				() -> assertEquals(1L, dto.getFromSkillId()),
				() -> assertEquals(2L, dto.getToSkillId()));
	}

	@Test
	public void shouldReturnNullWhenConvertingNullDependency() {
		assertNull(DTOMapper.INSTANCE.convertDependencyEntityToGetDTO(null));
	}
}
