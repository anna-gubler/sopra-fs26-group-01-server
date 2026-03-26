package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPatchDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representation
 * works.
 */
public class DTOMapperTest {

	// define constants to not copypaste stuff all the time
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
	public void shouldConvertEntityToUserGetDTO() {
		User user = new User();
		user.setId(1L);
		user.setUsername(USERNAME);
		user.setBio(BIO);
		user.setToken(TOKEN);
		user.setStatus(UserStatus.OFFLINE);
		user.setCreationDate(CREATION_DATE);

		UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

		assertAll(
				() -> assertEquals(user.getId(), userGetDTO.getId()),
				() -> assertEquals(user.getUsername(), userGetDTO.getUsername()),
				() -> assertEquals(user.getStatus(), userGetDTO.getStatus()),
				() -> assertEquals(user.getBio(), userGetDTO.getBio()),
				() -> assertEquals(user.getCreationDate(), userGetDTO.getCreationDate()));

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
}