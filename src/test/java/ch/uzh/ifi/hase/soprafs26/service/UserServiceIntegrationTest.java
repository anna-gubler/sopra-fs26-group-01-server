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


	// createUser

	@Test
	public void createUser_validInputs_success() {
		User createdUser = userService.createUser(buildNewUser());

		assertEquals(TEST_USERNAME, createdUser.getUsername());
		assertEquals(TEST_BIO, createdUser.getBio());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
		assertEquals("bottts-neutral", createdUser.getStyle());
		assertEquals(TEST_USERNAME, createdUser.getSeed());
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


	// loginUser

	@Test
	public void loginUser_validCredentials_success() {
		userService.createUser(buildNewUser());

		User result = userService.loginUser(buildNewUser());

		assertEquals(TEST_USERNAME, result.getUsername());
		assertEquals(UserStatus.ONLINE, result.getStatus());
		assertNotNull(result.getToken());
	}

	@Test
	public void loginUser_wrongPassword_throwsException() {
		userService.createUser(buildNewUser());

		User loginInput = new User();
		loginInput.setUsername(TEST_USERNAME);
		loginInput.setPassword("wrongPassword");

		assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginInput));
	}

	@Test
	public void loginUser_unknownUsername_throwsException() {
		User loginInput = new User();
		loginInput.setUsername("nobody");
		loginInput.setPassword(RAW_PASSWORD);

		assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginInput));
	}


	// logoutUser

	@Test
	public void logoutUser_setsStatusOfflineAndClearsToken() {
		User createdUser = userService.createUser(buildNewUser());

		userService.logoutUser(createdUser);

		assertNull(createdUser.getToken());
		assertEquals(UserStatus.OFFLINE, createdUser.getStatus());
	}


	// getUserById

	@Test
	public void getUserById_userExists_returnsUser() {
		User createdUser = userService.createUser(buildNewUser());

		User result = userService.getUserById(createdUser.getId());

		assertEquals(createdUser.getId(), result.getId());
		assertEquals(TEST_USERNAME, result.getUsername());
	}


	// getUserByToken

	@Test
	public void getUserByToken_validToken_returnsUser() {
		User createdUser = userService.createUser(buildNewUser());

		User result = userService.getUserByToken(createdUser.getToken());

		assertEquals(createdUser.getId(), result.getId());
	}

	@Test
	public void getUserByToken_invalidToken_throwsException() {
		assertThrows(ResponseStatusException.class,
				() -> userService.getUserByToken("not-a-real-token"));
	}


	// getUserByUsername

	@Test
	public void getUserByUsername_userExists_returnsUser() {
		userService.createUser(buildNewUser());

		User result = userService.getUserByUsername(TEST_USERNAME);

		assertEquals(TEST_USERNAME, result.getUsername());
	}

	@Test
	public void getUserByUsername_notFound_throwsException() {
		assertThrows(ResponseStatusException.class,
				() -> userService.getUserByUsername("nobody"));
	}


	// changeUserInformation

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


	// changeUserAvatar

	@Test
	public void changeUserAvatar_persistsStyleAndSeed() {
		User createdUser = userService.createUser(buildNewUser());

		User input = new User();
		input.setStyle("pixel-art");
		input.setSeed("customSeed");

		User updated = userService.changeUserAvatar(createdUser, input);

		assertEquals("pixel-art", updated.getStyle());
		assertEquals("customSeed", updated.getSeed());
	}


	// changePassword

	@Test
	public void changePassword_validInput_updatesPassword() {
		User createdUser = userService.createUser(buildNewUser());
		String oldHashedPassword = createdUser.getPassword();

		userService.changePassword(createdUser, RAW_PASSWORD, "newPassword123", "newPassword123");

		User updatedUser = userService.getUserById(createdUser.getId());
		assertNotEquals(oldHashedPassword, updatedUser.getPassword());
	}

	@Test
	public void changePassword_wrongOldPassword_throwsException() {
		User createdUser = userService.createUser(buildNewUser());

		assertThrows(ResponseStatusException.class,
				() -> userService.changePassword(createdUser, "wrongOld", "new123", "new123"));
	}

	@Test
	public void changePassword_newPasswordSameAsOld_throwsException() {
		User createdUser = userService.createUser(buildNewUser());

		assertThrows(ResponseStatusException.class,
				() -> userService.changePassword(createdUser, RAW_PASSWORD, RAW_PASSWORD, RAW_PASSWORD));
	}

	@Test
	public void changePassword_confirmMismatch_throwsException() {
		User createdUser = userService.createUser(buildNewUser());

		assertThrows(ResponseStatusException.class,
				() -> userService.changePassword(createdUser, RAW_PASSWORD, "newPass1", "newPass2"));
	}


	// deleteUserProfile

	@Test
	public void deleteUserProfile_correctPassword_userDeletedFromDB() {
		User createdUser = userService.createUser(buildNewUser());
		Long id = createdUser.getId();

		userService.deleteUserProfile(createdUser, RAW_PASSWORD);

		assertFalse(userRepository.findByUsername(TEST_USERNAME).isPresent());
		assertFalse(userRepository.findById(id).isPresent());
	}

	@Test
	public void deleteUserProfile_wrongPassword_throwsException() {
		User createdUser = userService.createUser(buildNewUser());

		assertThrows(ResponseStatusException.class,
				() -> userService.deleteUserProfile(createdUser, "wrongPassword"));
	}

}
