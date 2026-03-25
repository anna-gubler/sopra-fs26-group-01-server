package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

	private final UserService userService;

	UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/users")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<UserGetDTO> getAllUsers(@RequestHeader(value = "Authorization", required = false) String auth) {
		userService.checkToken(auth);

		// fetch all users in the internal representation
		List<User> users = userService.getUsers();
		List<UserGetDTO> userGetDTOs = new ArrayList<>();

		// convert each user to the API representation
		for (User user : users) {
			userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
		}
		return userGetDTOs;
	}

	@PostMapping("/users")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
		User createdUser = userService.createUser(userInput);
		UserGetDTO entitiyDto = DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
		entitiyDto.setToken(createdUser.getToken()); // include token
		return entitiyDto;
	}

	@GetMapping("/users/{id}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserGetDTO getUserById(@PathVariable("id") Long id,
			@RequestHeader(value = "Authorization", required = false) String auth) {
		userService.checkToken(auth);
		User RequestedUser = userService.getUserById(id);
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(RequestedUser);
	}


	@PutMapping("/users/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ResponseBody
	public void changeUserProfile(@PathVariable("id") Long id, @RequestBody UserPutDTO dto,
			@RequestHeader(value = "Authorization", required = false) String auth) {
		User user = userService.checkToken(auth);
		User userInput = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(dto);
		user = userService.changeUserInformation(user, userInput);
	}

	@PutMapping("/login")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserGetDTO login(@RequestBody UserPutDTO userLoginDTO) {
		User userLoginData = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userLoginDTO);
		User user = userService.loginUser(userLoginData);
		UserGetDTO userDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
		userDTO.setToken(user.getToken()); // include token
		return userDTO;
	}

	@PutMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(@RequestHeader(value = "Authorization", required = false) String auth) {
		User user = userService.checkToken(auth);
		userService.logoutUser(user);
	}
}
