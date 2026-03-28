package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest {

	private static final String TEST_USERNAME = "testUsername";
	private static final String TEST_BIO      = "very fancy and impressive bio";
	private static final String RAW_PASSWORD  = "test28234876";

	@Qualifier("userRepository")
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@BeforeEach
	public void setup() {
		userRepository.deleteAll();
	}

	// Returns a User as it would look coming in from a request:
	// plain-text password, no id/token/status set.
	private User buildNewUser() {
		User user = new User();
		user.setUsername(TEST_USERNAME);
		user.setPassword(RAW_PASSWORD);
		user.setBio(TEST_BIO);
		return user;
	}


	// --- createUser ---

	@Test
	public void createUser_validInputs_success() {
		User createdUser = userService.createUser(buildNewUser());

		assertEquals(TEST_USERNAME, createdUser.getUsername());
		assertEquals(TEST_BIO, createdUser.getBio());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
		assertNotNull(createdUser.getId());
		assertNotNull(createdUser.getToken());
		assertNotNull(createdUser.getCreationDate());
		assertNotEquals(RAW_PASSWORD, createdUser.getPassword());
		assertNotNull(createdUser.getPassword());
	}

	@Test
	public void createUser_duplicateUsername_throwsException() {
		userService.createUser(buildNewUser());

		assertThrows(ResponseStatusException.class, () -> userService.createUser(buildNewUser()));
	}


	// --- loginUser ---

	@Test
	public void loginUser_validCredentials_success() {
		userService.createUser(buildNewUser());

		User result = userService.loginUser(buildNewUser());

		assertEquals(TEST_USERNAME, result.getUsername());
		assertEquals(UserStatus.ONLINE, result.getStatus());
		assertNotNull(result.getToken());
	}


	// --- getUserById ---

	@Test
	public void getUserById_userExists_returnsUser() {
		User createdUser = userService.createUser(buildNewUser());

		User result = userService.getUserById(createdUser.getId());

		assertEquals(createdUser.getId(), result.getId());
		assertEquals(TEST_USERNAME, result.getUsername());
	}


	// --- changeUserInformation ---

	@Test
	public void changeUserInformation_validInputs_persistsChanges() {
		User createdUser = userService.createUser(buildNewUser());

		User input = new User();
		input.setUsername("updatedUsername");
		input.setBio("updated bio");

		User updatedUser = userService.changeUserInformation(createdUser, input);

		assertEquals("updatedUsername", updatedUser.getUsername());
		assertEquals("updated bio", updatedUser.getBio());
	}


	// --- deleteUserProfile ---

	@Test
	public void deleteUserProfile_correctPassword_userDeletedFromDB() {
		User createdUser = userService.createUser(buildNewUser());
		Long id = createdUser.getId();

		userService.deleteUserProfile(createdUser, RAW_PASSWORD);

		assertNull(userRepository.findByUsername(TEST_USERNAME));
		assertFalse(userRepository.findById(id).isPresent());
	}

}
