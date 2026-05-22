package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

	private static final Long   TEST_ID       = 1L;
	private static final String TEST_USERNAME = "testUsername";
	private static final String TEST_BIO      = "very fancy and impressive bio";
	private static final String TEST_TOKEN    = "some-uuid-token";
	// Pre-computed bcrypt hash for RAW_PASSWORD, so tests that simulate a
	// "DB user" (login, checkToken, delete) have a properly hashed password
	// without mutating objects across tests.
	private static final String RAW_PASSWORD    = "test28234876";
	private static final String HASHED_PASSWORD = new BCryptPasswordEncoder().encode(RAW_PASSWORD);

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this); // initializes @Mock and @InjectMocks fields
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

	// Returns a User as it would look coming out of the DB:
	// hashed password, token set, status ONLINE.
	private User buildPersistedUser() {
		User user = new User();
		user.setId(TEST_ID);
		user.setUsername(TEST_USERNAME);
		user.setPassword(HASHED_PASSWORD);
		user.setBio(TEST_BIO);
		user.setToken(TEST_TOKEN);
		user.setStatus(UserStatus.ONLINE);
		return user;
	}


	// --- getUsers ---

	@Test
	public void getUsers_returnsAllUsers() {
		User user1 = buildPersistedUser();
		User user2 = buildPersistedUser();
		user2.setId(2L);
		user2.setUsername("otherUser");

		Mockito.when(userRepository.findAll()).thenReturn(List.of(user1, user2));

		List<User> result = userService.getUsers();

		assertEquals(2, result.size());
		Mockito.verify(userRepository, Mockito.times(1)).findAll();
	}


	// --- createUser ---

	@Test
	public void createUser_validInputs_success() {
		User input = buildNewUser();

		Mockito.when(userRepository.save(Mockito.any())).thenReturn(input);

		User createdUser = userService.createUser(input);

		Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
		assertEquals(TEST_USERNAME, createdUser.getUsername());
		assertEquals(TEST_BIO, createdUser.getBio());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
		assertNotNull(createdUser.getCreationDate());
		assertNotNull(createdUser.getToken());
		assertNotEquals(RAW_PASSWORD, createdUser.getPassword());
		assertNotNull(createdUser.getPassword());
		assertFalse(createdUser.isHasSeenDashboard());
		assertFalse(createdUser.isHasSeenMapAsMember());
		assertFalse(createdUser.isHasSeenMapAsOwner());
	}

	@Test
	public void createUser_duplicateUsername_throwsException() {
		User input = buildNewUser();

		Mockito.when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(buildPersistedUser()));

		assertThrows(ResponseStatusException.class, () -> userService.createUser(input));
	}


	// --- loginUser ---

	@Test
	public void loginUser_validCredentials_success() {
		Mockito.when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(buildPersistedUser()));

		User result = userService.loginUser(buildNewUser());

		assertEquals(UserStatus.ONLINE, result.getStatus());
		assertNotNull(result.getToken());
	}

	@Test
	public void loginUser_userNotFound_throwsException() {
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> userService.loginUser(buildNewUser()));
	}

	@Test
	public void loginUser_wrongPassword_throwsException() {
		Mockito.when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(buildPersistedUser()));

		User loginInput = new User();
		loginInput.setPassword("wrongPassword");

		assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginInput));
	}


	// --- logoutUser ---

	@Test
	public void logoutUser_success() {
		User user = buildPersistedUser();

		userService.logoutUser(user);

		assertNull(user.getToken());
		assertEquals(UserStatus.OFFLINE, user.getStatus());
	}


	// --- getUserByToken ---

	@Test
	public void getUserByToken_validToken_returnsUser() {
		Mockito.when(userRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.of(buildPersistedUser()));

		User result = userService.getUserByToken(TEST_TOKEN);

		assertEquals(TEST_ID, result.getId());
	}

	@Test
	public void getUserByToken_tokenNotFound_throwsException() {
		Mockito.when(userRepository.findByToken(Mockito.any())).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> userService.getUserByToken("invalid-token"));
	}

	@Test
	public void getUserByToken_userOffline_throwsException() {
		User offlineUser = buildPersistedUser();
		offlineUser.setStatus(UserStatus.OFFLINE);
		Mockito.when(userRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.of(offlineUser));

		assertThrows(ResponseStatusException.class, () -> userService.getUserByToken(TEST_TOKEN));
	}


	// --- getUserById ---

	@Test
	public void getUserById_userExists_returnsUser() {
		Mockito.when(userRepository.findById(TEST_ID)).thenReturn(Optional.of(buildPersistedUser()));

		User result = userService.getUserById(TEST_ID);

		assertEquals(TEST_ID, result.getId());
		assertEquals(TEST_USERNAME, result.getUsername());
	}

	@Test
	public void getUserById_userNotFound_throwsException() {
		Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> userService.getUserById(TEST_ID));
	}


	// --- changeUserInformation ---

	@Test
	public void changeUserInformation_newUsername_updatesUsername() {
		User requestingUser = buildPersistedUser();

		User input = new User();
		input.setUsername("newUsername");

		Mockito.when(userRepository.findByUsername("newUsername")).thenReturn(Optional.empty());
		Mockito.when(userRepository.saveAndFlush(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

		User result = userService.changeUserInformation(requestingUser, input);

		assertEquals("newUsername", result.getUsername());
	}

	@Test
	public void changeUserInformation_duplicateUsername_throwsException() {
		User requestingUser = buildPersistedUser();

		User input = new User();
		input.setUsername("takenUsername");

		Mockito.when(userRepository.findByUsername("takenUsername")).thenReturn(Optional.of(buildPersistedUser()));

		assertThrows(ResponseStatusException.class, () -> userService.changeUserInformation(requestingUser, input));
	}

	@Test
	public void changeUserInformation_newBio_updatesBio() {
		User requestingUser = buildPersistedUser();

		User input = new User();
		input.setBio("updated bio");

		Mockito.when(userRepository.saveAndFlush(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

		User result = userService.changeUserInformation(requestingUser, input);

		assertEquals("updated bio", result.getBio());
	}

	@Test
	public void changeUserInformation_newPassword_updatesHashedPassword() {
		User requestingUser = buildPersistedUser();

		User input = new User();
		input.setPassword("newPassword123");

		Mockito.when(userRepository.saveAndFlush(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

		User result = userService.changeUserInformation(requestingUser, input);

		assertNotEquals("newPassword123", result.getPassword());
		assertNotNull(result.getPassword());
	}


	// --- deleteUserProfile ---

	@Test
	public void deleteUserProfile_correctPassword_deletesUser() {
		User user = buildPersistedUser();

		userService.deleteUserProfile(user, RAW_PASSWORD);

		Mockito.verify(userRepository, Mockito.times(1)).delete(user);
	}

	@Test
	public void deleteUserProfile_wrongPassword_throwsException() {
		User user = buildPersistedUser();

		assertThrows(ResponseStatusException.class, () -> userService.deleteUserProfile(user, "wrongPassword"));
	}

	// --- AvatarGeneration ---

	@Test
	public void createUser_avatarGeneration_success() {
    	User input = buildNewUser();

    	Mockito.when(userRepository.save(Mockito.any())).thenReturn(input);

    	User createdUser = userService.createUser(input);

    	assertEquals(TEST_USERNAME, createdUser.getSeed());
    	assertEquals("bottts-neutral", createdUser.getStyle());
	}


	//getUserByUsername

	@Test
	public void getUserByUsername_userExists_returnsUser() {
		Mockito.when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(buildPersistedUser()));

		User result = userService.getUserByUsername(TEST_USERNAME);

		assertEquals(TEST_USERNAME, result.getUsername());
		assertEquals(TEST_ID, result.getId());
	}

	@Test
	public void getUserByUsername_notFound_throwsException() {
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> userService.getUserByUsername("nobody"));
	}


	//changeUserAvatar

	@Test
	public void changeUserAvatar_updatesStyleAndSeed() {
		User requestingUser = buildPersistedUser();

		User input = new User();
		input.setStyle("pixel-art");
		input.setSeed("customSeed");

		Mockito.when(userRepository.saveAndFlush(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

		User result = userService.changeUserAvatar(requestingUser, input);

		assertEquals("pixel-art", result.getStyle());
		assertEquals("customSeed", result.getSeed());
	}


	@Test
	public void changeUserAvatar_styleOnly_doesNotChangeSeed() {
		User requestingUser = buildPersistedUser();
		requestingUser.setStyle("bottts-neutral");
		requestingUser.setSeed("originalSeed");

		User input = new User();
		input.setStyle("pixel-art");
		input.setSeed(null);

		Mockito.when(userRepository.saveAndFlush(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

		User result = userService.changeUserAvatar(requestingUser, input);

		assertEquals("pixel-art", result.getStyle());
		assertEquals("originalSeed", result.getSeed());
	}

	@Test
	public void changeUserAvatar_seedOnly_doesNotChangeStyle() {
		User requestingUser = buildPersistedUser();
		requestingUser.setStyle("bottts-neutral");
		requestingUser.setSeed("originalSeed");

		User input = new User();
		input.setStyle(null);
		input.setSeed("newSeed");

		Mockito.when(userRepository.saveAndFlush(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

		User result = userService.changeUserAvatar(requestingUser, input);

		assertEquals("bottts-neutral", result.getStyle());
		assertEquals("newSeed", result.getSeed());
	}

	// changePassword

	@Test
	public void changePassword_validInput_updatesPassword() {
		User managedUser = buildPersistedUser();
		Mockito.when(userRepository.findById(TEST_ID)).thenReturn(Optional.of(managedUser));
		Mockito.when(userRepository.saveAndFlush(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

		userService.changePassword(managedUser, RAW_PASSWORD, "newPassword123", "newPassword123");

		Mockito.verify(userRepository).saveAndFlush(managedUser);
	}

	@Test
	public void changePassword_wrongOldPassword_throwsException() {
		User managedUser = buildPersistedUser();
		Mockito.when(userRepository.findById(TEST_ID)).thenReturn(Optional.of(managedUser));

		assertThrows(ResponseStatusException.class,
				() -> userService.changePassword(managedUser, "wrongOld", "newPassword123", "newPassword123"));
	}

	@Test
	public void changePassword_newPasswordSameAsOld_throwsException() {
		User managedUser = buildPersistedUser();
		Mockito.when(userRepository.findById(TEST_ID)).thenReturn(Optional.of(managedUser));

		assertThrows(ResponseStatusException.class,
				() -> userService.changePassword(managedUser, RAW_PASSWORD, RAW_PASSWORD, RAW_PASSWORD));
	}

	@Test
	public void changePassword_confirmMismatch_throwsException() {
		User managedUser = buildPersistedUser();
		Mockito.when(userRepository.findById(TEST_ID)).thenReturn(Optional.of(managedUser));

		assertThrows(ResponseStatusException.class,
				() -> userService.changePassword(managedUser, RAW_PASSWORD, "newPass1", "newPass2"));
	}

	@Test
	public void changePassword_userNotFound_throwsNotFoundException() {
		User managedUser = buildPersistedUser();
		Mockito.when(userRepository.findById(TEST_ID)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class,
				() -> userService.changePassword(managedUser, RAW_PASSWORD, "newPass1", "newPass1"));
	}

}
