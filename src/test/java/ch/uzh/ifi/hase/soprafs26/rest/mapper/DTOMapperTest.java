package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representation
 * works.
 */
public class DTOMapperTest {

	// define constants to not copypaste stuff all the time
	private static final String NAME = "Firstname Lastname";
	private static final String USERNAME = "firstname@lastname";
	private static final String BIO = "Hey there! I'm using Whatsapp.";
	private static final String TOKEN = "6dd696b4-83a2-42a6-8769-e2d755c6b8b8";

	// create new UserPostDTO instance
	private static UserPostDTO newUserPostDTO() {
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setName(NAME);
		userPostDTO.setUsername(USERNAME);
		userPostDTO.setBio(BIO);
		return userPostDTO;
	}

	// create new User instance
	private static User newUser() {
		User user = new User();
		user.setName(NAME);
		user.setUsername(USERNAME);
		user.setBio(BIO);
		user.setToken(TOKEN);
		user.setStatus(UserStatus.OFFLINE);
		return user;
	}

	@Test
	public void shouldConvertUserPostDTOToEntity() {
		UserPostDTO userPostDTO = newUserPostDTO();

		User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		assertAll(
				() -> assertEquals(userPostDTO.getName(), user.getName()),
				() -> assertEquals(userPostDTO.getUsername(), user.getUsername()),
				() -> assertEquals(userPostDTO.getBio(), user.getBio()));

	}

	@Test
	public void shouldConvertEntityToUserGetDTO() {
		User user = newUser();
		user.setId(1L);

		UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

		assertAll(
				() -> assertEquals(user.getId(), userGetDTO.getId()),
				() -> assertEquals(user.getName(), userGetDTO.getName()),
				() -> assertEquals(user.getUsername(), userGetDTO.getUsername()),
				() -> assertEquals(user.getStatus(), userGetDTO.getStatus()));

	}
}
