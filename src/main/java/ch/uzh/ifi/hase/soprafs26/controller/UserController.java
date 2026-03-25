package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPatchDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 * 
 * Authorization works over RequestHeader. Required is set to false so we can control the error and 
 * throw 401 unauthorized with checkToken() instead of standard 400 bad request (for wrong header).
 */
@RestController
public class UserController {

	private final UserService userService;

	UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/auth/register")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public UserGetDTO createUser(@Valid @RequestBody UserPostDTO userPostDTO) { //@Valid checks if required fields of the userPostDTO are blank and throws an error 400 if they are
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
		User createdUser = userService.createUser(userInput);
		UserGetDTO entitiyDto = DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
		entitiyDto.setToken(createdUser.getToken()); // include token
		return entitiyDto;
	}

	@PutMapping("/auth/login")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserGetDTO login(@RequestBody UserPutDTO userLoginDTO) {
		User userLoginData = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userLoginDTO);
		User user = userService.loginUser(userLoginData);
		UserGetDTO userDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
		userDTO.setToken(user.getToken()); // include token
		return userDTO;
	}

	@PutMapping("/auth/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(@RequestHeader(value = "Authorization", required = false) String auth) {
		User user = userService.checkToken(auth);
		userService.logoutUser(user);
	}

	@GetMapping("/users/me")
	@ResponseStatus(HttpStatus.OK)
	public UserGetDTO getUser(@RequestHeader(value = "Authorization", required = false) String auth) {
		User user = userService.checkToken(auth);
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
	}
	
	@PatchMapping("/users/me")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserGetDTO changeUserProfile(@RequestBody UserPatchDTO dto,
			@RequestHeader(value = "Authorization", required = false) String auth) {
		User requestingUser = userService.checkToken(auth);
		User userInput = DTOMapper.INSTANCE.convertUserPatchDTOtoEntity(dto);
		User changedUser = userService.changeUserInformation(requestingUser, userInput);
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(changedUser);
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

}
