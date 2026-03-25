package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutDTO;
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

import java.util.Collections;
import java.util.List;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
		return userPostDTO;
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
	public void givenValidUserPostDTO_whenPostUsers_thenReturnCreatedUserAndToken() throws Exception {
		// given
		UserPostDTO userPostDTO = newUserPostDTO();
		User user = newUser();
		user.setId(1L);
		user.setToken(TOKEN);
		user.setStatus(UserStatus.ONLINE);

		// mocks
		given(userService.createUser(any())).willReturn(user);

		// define HTTP request
		MockHttpServletRequestBuilder postRequest = post("/users")
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
	public void givenAuthenticatedUser_whenGetUsers_thenReturnUserList() throws Exception {
		// Define desired server response
		User user = newUser();
		List<User> allUsers = Collections.singletonList(user);

		// this mocks the UserService, meaning "it would return this if it were used"
		given(userService.getUsers()).willReturn(allUsers);
		mockUserAuthentication(user, true);

		// Define HTTP request
		MockHttpServletRequestBuilder getRequest = get("/users")
				.header("Authorization", "Bearer test-token")
				.contentType(MediaType.APPLICATION_JSON);

		// then do requests
		mockMvc.perform(getRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].username", is(user.getUsername())))
				.andExpect(jsonPath("$[0].bio", is(user.getBio())))
				.andExpect(jsonPath("$[0].token").doesNotExist())
				.andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
	}

	@Test
	public void givenInvalidUserPostDTO_whenPostUsers_thenReturnBadRequest() throws Exception {
		// given: invalid DTO (Username fehlt)
		UserPostDTO invalidDto = newUserPostDTO();
		invalidDto.setUsername(null);

		// mock: service entscheidet bad request
		given(userService.createUser(any()))
				.willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user data"));

		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(invalidDto));

		mockMvc.perform(postRequest)
				.andExpect(status().isBadRequest());
	}

	@Test
	public void givenNoAuthorization_whenGetUsers_thenReturnUnauthorized() throws Exception {
		// Mock
		mockUserAuthentication(newUser(), false);

		// Define HTTP request
		MockHttpServletRequestBuilder getRequest = get("/users")
				.contentType(MediaType.APPLICATION_JSON);

		// then do requests
		mockMvc.perform(getRequest)
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void givenNoAuthorization_whenGetUsersId_thenReturnUnauthorized() throws Exception {
		// Mock
		mockUserAuthentication(newUser(), false);

		MockHttpServletRequestBuilder getRequest = get("/users/1");

		mockMvc.perform(getRequest)
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void givenNoAuthorization_whenPutUsers_thenReturnUnauthorized() throws Exception {
		// Mock
		mockUserAuthentication(newUser(), false);

		MockHttpServletRequestBuilder putRequest = put("/users/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(new UserPutDTO()));

		mockMvc.perform(putRequest)
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void givenValidAuthentication_whenGetUserById_thenReturnUserById() throws Exception {
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
	public void givenNonExistingUserId_whenGetUserById_thenReturnNotFound() throws Exception {

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

	@Test
	public void givenOwnId_whenPutUserById_thenReturnUser() throws Exception {
		// given: authenticated user (same person)
		User authUser = newUser();
		authUser.setId(1L);
		authUser.setToken(TOKEN);

		// request dto (what the client wants to change)
		UserPutDTO dto = new UserPutDTO();
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
		MockHttpServletRequestBuilder putRequest = put("/users/1")
				.header("Authorization", "Bearer " + TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(dto));

		// then
		mockMvc.perform(putRequest)
				.andExpect(status().isNoContent());
	}

	@Test
	public void givenIdOfOtherUser_whenPutUserById_thenReturnForbidden() throws Exception {

		// authenticated user (id=1)
		User authUser = newUser();
		authUser.setId(1L);
		authUser.setToken(TOKEN);

		// update request (irrelevant was genau drin ist, Hauptsache Body ist gültig)
		UserPutDTO dto = new UserPutDTO();
		dto.setBio("Not allowed");

		// mocks
		given(userService.checkToken(any()))
				.willReturn(authUser);

		given(userService.changeUserInformation(any(User.class), any(User.class)))
				.willThrow(new ResponseStatusException(
						HttpStatus.FORBIDDEN,
						"User can only change his own Information"));

		// request: trying to update someone else (id=2)
		MockHttpServletRequestBuilder putRequest = put("/users/2")
				.header("Authorization", "Bearer " + TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(dto));

		// then
		mockMvc.perform(putRequest)
				.andExpect(status().isForbidden());
	}

	/*
	 * Future Tests If I decide to stay with these endpoints for the group project
	 * 
	 * @Test
	 * public void givenValidPassword_whenPutLogin_thenReturnUserAndToken() throws
	 * Exception {
	 * 
	 * }
	 * 
	 * @Test
	 * public void givenInvalidPassword_whenPutLogin_thenReturnUnauthorized() throws
	 * Exception {
	 * 
	 * }
	 * 
	 * @Test
	 * public void givenValidCredentials_whenPutLogout_thenReturnNoContent() throws
	 * Exception {
	 * 
	 * }
	 * 
	 * @Test
	 * public void givenValidCredentials_whenPutPassword_thenReturnNoContent()
	 * throws Exception {
	 * 
	 * }
	 * 
	 * @Test
	 * public void givenInvalidCredentials_whenPutPassword_thenReturnUnauthorized()
	 * throws Exception {
	 * 
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
}