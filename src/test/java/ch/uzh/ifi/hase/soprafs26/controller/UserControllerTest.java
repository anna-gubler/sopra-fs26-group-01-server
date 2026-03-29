package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPatchDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutAvatarDTO;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	// define constants to not copypaste stuff all the time
	private static final String USERNAME = "firstname@lastname";
	private static final String BIO = "Hey there! I'm using Whatsapp.";
	private static final String TOKEN = "6dd696b4-83a2-42a6-8769-e2d755c6b8b8";
	private static final String PASSWORD = "Very_Safe_Password123!";

	// create new User instance
	private static User newUser() {
		User user = new User();
		user.setUsername(USERNAME);
		user.setBio(BIO);
		user.setStatus(UserStatus.OFFLINE);
		return user;
	}

	// create new UserPostDTO instance
	private static UserPostDTO newUserPostDTO() {
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername(USERNAME);
		userPostDTO.setBio(BIO);
		userPostDTO.setPassword(PASSWORD);
		return userPostDTO;
	}

	// create new UserPatchDTO instance
	private static UserPatchDTO newUserPatchDTO() {
		UserPatchDTO userPatchDTO = new UserPatchDTO();
		userPatchDTO.setUsername(USERNAME);
		userPatchDTO.setBio(BIO);
		return userPatchDTO;
	}

	// mock User authentication
	private void mockUserAuthentication(User user, boolean success) {
		if (success) {
			given(userService.checkToken(nullable(String.class)))
					.willReturn(user);

		} else {
			given(userService.checkToken(nullable(String.class)))
					.willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization header"));
		}
	}

	@Test
	public void givenValidUserPostDTO_whenAuthRegister_thenReturnCreatedUserWithToken() throws Exception {
		// given
		UserPostDTO userPostDTO = newUserPostDTO();
		User user = newUser();
		user.setId(1L);
		user.setToken(TOKEN);
		user.setStatus(UserStatus.ONLINE);

		// mocks
		given(userService.createUser(any())).willReturn(user);

		// define HTTP request
		MockHttpServletRequestBuilder postRequest = post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		// then
		mockMvc.perform(postRequest)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(user.getId().intValue())))
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.token", is(user.getToken())))
				.andExpect(jsonPath("$.bio", is(user.getBio())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())));
	}

	@Test
	public void givenInvalidUserPostDTO_whenAuthRegister_thenReturnBadRequest() throws Exception {
		// given: invalid DTO (Username fehlt)
		UserPostDTO invalidDto = newUserPostDTO();
		invalidDto.setUsername(null);

		MockHttpServletRequestBuilder postRequest = post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(invalidDto));

		mockMvc.perform(postRequest)
				.andExpect(status().isBadRequest());
	}

	@Test
	public void givenExistingUsername_whenAuthRegister_thenReturnConflict() throws Exception {
		UserPostDTO invalidDto = newUserPostDTO();

		// mocks
		given(userService.createUser(any())).willThrow(new ResponseStatusException(
				HttpStatus.CONFLICT, "Username already exists"));

		MockHttpServletRequestBuilder postRequest = post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(invalidDto));

		mockMvc.perform(postRequest)
				.andExpect(status().isConflict());
	}

	@Test
	public void givenValidLoginAttempt_whenAuthLogin_thenReturnUserWithToken() throws Exception {
		UserPostDTO userLoginData = newUserPostDTO();
		User user = newUser();
		user.setId(1L);
		user.setToken(TOKEN);
		user.setStatus(UserStatus.ONLINE);

		// mocks
		given(userService.loginUser(any())).willReturn(user);

		MockHttpServletRequestBuilder postRequest = post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userLoginData));

		mockMvc.perform(postRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(user.getId().intValue())))
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.token", is(user.getToken())))
				.andExpect(jsonPath("$.bio", is(user.getBio())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())));
	}

	@Test
	public void givenMissingCredentials_whenAuthLogin_thenReturnBadRequest() throws Exception {
		UserPostDTO invalidLoginData = newUserPostDTO();
		invalidLoginData.setUsername(null);

		// doesnt require mocking loginUser, since BadRequest already gets thrown with
		// @Valid and @NotBlank logic of UserPostDTO
		// but there would be an additional safeguard in loginUser anyway

		MockHttpServletRequestBuilder postRequest = post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(invalidLoginData));

		mockMvc.perform(postRequest)
				.andExpect(status().isBadRequest());
	}

	@Test
	public void givenInvalidCredentials_whenAuthLogin_thenReturnUnauthorized() throws Exception {
		UserPostDTO invalidLoginData = newUserPostDTO();

		given(userService.loginUser(any())).willThrow(new ResponseStatusException(
				HttpStatus.UNAUTHORIZED, "Invalid login"));

		MockHttpServletRequestBuilder postRequest = post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(invalidLoginData));

		mockMvc.perform(postRequest)
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void givenValidAuthentication_whenAuthLogout_thenReturnNoContent() throws Exception {
		User logoutUser = newUser();
		mockUserAuthentication(logoutUser, true);

		MockHttpServletRequestBuilder postRequest = post("/auth/logout")
				.contentType(MediaType.APPLICATION_JSON);

		mockMvc.perform(postRequest)
				.andExpect(status().isNoContent());
	}

	@Test // Tests both invalid token and user offline, since both is checked in
			// mockUserAuthentication (and not in logoutUser)
	public void givenInvalidAuthentication_whenAuthLogout_thenReturnUnauthorized() throws Exception {
		User logoutUser = newUser();
		mockUserAuthentication(logoutUser, false);

		MockHttpServletRequestBuilder postRequest = post("/auth/logout")
				.contentType(MediaType.APPLICATION_JSON);

		mockMvc.perform(postRequest)
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void givenValidAuthentication_whenGetUsersMe_thenReturnUser() throws Exception {
		User user = newUser();
		mockUserAuthentication(user, true);

		MockHttpServletRequestBuilder getRequest = get("/users/me")
				.contentType(MediaType.APPLICATION_JSON);

		mockMvc.perform(getRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.bio", is(user.getBio())))
				.andExpect(jsonPath("$.token").doesNotExist())
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())));
	}

	@Test
	public void givenValidAuthentication_whenPatchUsersMe_thenReturnUpdatedUser() throws Exception {
		// given: authenticated user
		User authUser = newUser();
		authUser.setId(1L);
		authUser.setToken(TOKEN);

		// request dto (what the client wants to change)
		UserPatchDTO dto = newUserPatchDTO();
		dto.setBio("Updated bio");

		// service returns updated user
		User updatedUser = newUser();
		updatedUser.setId(1L);
		updatedUser.setBio("Updated bio");
		updatedUser.setStatus(UserStatus.ONLINE);

		// mocks
		given(userService.checkToken(any())).willReturn(authUser);
		given(userService.changeUserInformation(any(User.class), any(User.class))).willReturn(updatedUser);

		// request
		MockHttpServletRequestBuilder patchRequest = patch("/users/me")
				.header("Authorization", "Bearer " + TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(dto));

		// then
		mockMvc.perform(patchRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.bio", is(updatedUser.getBio())));
	}

	@Test
	public void givenNoAuthorization_whenPatchUsersMe_thenReturnUnauthorized() throws Exception {
		// Mock
		mockUserAuthentication(newUser(), false);

		MockHttpServletRequestBuilder patchRequest = patch("/users/me")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(new UserPatchDTO()));

		mockMvc.perform(patchRequest)
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void givenDuplicateUsername_whenPatchUsersMe_thenReturnConflict() throws Exception {
		mockUserAuthentication(newUser(), true);

		given(userService.changeUserInformation(any(User.class), any(User.class))).willThrow(
				new ResponseStatusException(
						HttpStatus.CONFLICT, "Username already exists"));

		MockHttpServletRequestBuilder patchRequest = patch("/users/me")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(new UserPatchDTO()));

		mockMvc.perform(patchRequest)
				.andExpect(status().isConflict());
	}

	@Test
	public void givenValidAuthentication_whenDeleteUsersMe_thenReturnNoContent() throws Exception {
		mockUserAuthentication(newUser(), true);

		MockHttpServletRequestBuilder deleteRequest = delete("/users/me")
				.contentType(MediaType.APPLICATION_JSON)
				.content(PASSWORD);

		mockMvc.perform(deleteRequest)
				.andExpect(status().isNoContent());
	}

	@Test
	public void givenWrongPassword_whenDeleteUsersMe_thenReturnUnauthorized() throws Exception {
		mockUserAuthentication(newUser(), true);

		willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong password"))
				.given(userService).deleteUserProfile(any(User.class), eq(PASSWORD));

		MockHttpServletRequestBuilder deleteRequest = delete("/users/me")
				.contentType(MediaType.APPLICATION_JSON)
				.content(PASSWORD);

		mockMvc.perform(deleteRequest)
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void givenValidAuthentication_whenGetUsersById_thenReturnUserById() throws Exception {
		// given: authenticated user (der den Token besitzt)
		User authUser = newUser();
		authUser.setId(1L);
		authUser.setToken(TOKEN);

		// and second user (that we want to get)
		User requestedUser = newUser();
		requestedUser.setId(2L);

		// mocks
		mockUserAuthentication(authUser, true);
		given(userService.getUserById(2L)).willReturn(requestedUser);

		// request
		MockHttpServletRequestBuilder getRequest = get("/users/2")
				.header("Authorization", "Bearer " + TOKEN)
				.contentType(MediaType.APPLICATION_JSON);

		// then
		mockMvc.perform(getRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(requestedUser.getId().intValue())))
				.andExpect(jsonPath("$.username", is(requestedUser.getUsername())))
				.andExpect(jsonPath("$.bio", is(requestedUser.getBio())))
				.andExpect(jsonPath("$.status", is(requestedUser.getStatus().toString())))
				.andExpect(jsonPath("$.token").doesNotExist());
	}

	@Test
	public void givenNoAuthorization_whenGetUsersById_thenReturnUnauthorized() throws Exception {
		// Mock
		mockUserAuthentication(newUser(), false);

		MockHttpServletRequestBuilder getRequest = get("/users/1");

		mockMvc.perform(getRequest)
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void givenNonExistingUserId_whenGetUsersById_thenReturnNotFound() throws Exception {

		// given: authenticated user
		User authUser = newUser();
		authUser.setId(1L);
		authUser.setToken(TOKEN);

		// mocks
		mockUserAuthentication(authUser, true);

		given(userService.getUserById(999L))
				.willThrow(new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						"User not found"));

		// request
		MockHttpServletRequestBuilder getRequest = get("/users/999")
				.header("Authorization", "Bearer " + TOKEN)
				.contentType(MediaType.APPLICATION_JSON);

		// then
		mockMvc.perform(getRequest)
				.andExpect(status().isNotFound());
	}

	/*
	 * The following tests are for getUsers (List), and Endpoint from M1 that we
	 * dont have in specs. I'll leave them for now
	 * Would also require the following static imports:
	 * import java.util.Collections;
	 * import java.util.List;
	 * import static org.hamcrest.Matchers.hasSize;
	 * 
	 * @Test
	 * public void givenAuthenticatedUser_whenGetUsers_thenReturnUserList() throws
	 * Exception {
	 * // Define desired server response
	 * User user = newUser();
	 * List<User> allUsers = Collections.singletonList(user);
	 * 
	 * // this mocks the UserService, meaning "it would return this if it were used"
	 * given(userService.getUsers()).willReturn(allUsers);
	 * mockUserAuthentication(user, true);
	 * 
	 * // Define HTTP request
	 * MockHttpServletRequestBuilder getRequest = get("/users")
	 * .header("Authorization", "Bearer test-token")
	 * .contentType(MediaType.APPLICATION_JSON);
	 * 
	 * // then do requests
	 * mockMvc.perform(getRequest)
	 * .andExpect(status().isOk())
	 * .andExpect(jsonPath("$", hasSize(1)))
	 * .andExpect(jsonPath("$[0].username", is(user.getUsername())))
	 * .andExpect(jsonPath("$[0].bio", is(user.getBio())))
	 * .andExpect(jsonPath("$[0].token").doesNotExist())
	 * .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
	 * }
	 * 
	 * @Test
	 * public void givenNoAuthorization_whenGetUsers_thenReturnUnauthorized() throws
	 * Exception {
	 * // Mock
	 * mockUserAuthentication(newUser(), false);
	 * 
	 * // Define HTTP request
	 * MockHttpServletRequestBuilder getRequest = get("/users")
	 * .contentType(MediaType.APPLICATION_JSON);
	 * 
	 * // then do requests
	 * mockMvc.perform(getRequest)
	 * .andExpect(status().isUnauthorized());
	 * }
	 */

	/**
	 * Helper Method to convert userPostDTO into a JSON string such that the input
	 * can be processed
	 * Input will look like this: {"name": "Test User", "username": "testUsername"}
	 * 
	 * @param object
	 * @return string
	 */
	private String asJsonString(final Object object) {
		try {
			return new ObjectMapper().writeValueAsString(object);
		} catch (JacksonException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("The request body could not be created.%s", e.toString()));
		}
	}

	@Test
	public void givenUser_whenChangeAvatar_thenSuccessful() throws Exception {
		mockUserAuthentication(newUser(), true);

		given(userService.changeUserAvatar(any(User.class), any(User.class))).willReturn(newUser());

		MockHttpServletRequestBuilder putRequest = put("/users/me/avatar")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(new UserPutAvatarDTO()));

		mockMvc.perform(putRequest)
				.andExpect(status().isOk());
	}
}