package ch.uzh.ifi.hase.soprafs26.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPatchDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPasswordChangeDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutAvatarDTO;
import jakarta.validation.Valid;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 *
 * Authorization is handled by AuthInterceptor for all protected endpoints.
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

	@PostMapping("/auth/login")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserGetDTO login(@Valid @RequestBody UserPostDTO userLoginDTO) {
		User userLoginData = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userLoginDTO);
		User user = userService.loginUser(userLoginData);
		UserGetDTO userDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
		userDTO.setToken(user.getToken()); // include token
		return userDTO;
	}

	@PostMapping("/auth/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(HttpServletRequest request) {
		User user = (User) request.getAttribute("authenticatedUser");
		userService.logoutUser(user);
	}

	@GetMapping("/users/me")
	@ResponseStatus(HttpStatus.OK)
	public UserGetDTO getUser(HttpServletRequest request) {
		User user = (User) request.getAttribute("authenticatedUser");
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
	}

	@PatchMapping("/users/me")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserGetDTO changeUserProfile(@RequestBody UserPatchDTO dto, HttpServletRequest request) {
		User requestingUser = (User) request.getAttribute("authenticatedUser");
		User userInput = DTOMapper.INSTANCE.convertUserPatchDTOtoEntity(dto);
		User changedUser = userService.changeUserInformation(requestingUser, userInput);
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(changedUser);
	}

	@DeleteMapping("/users/me")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ResponseBody
	public void deleteUserProfile(@RequestBody String password, HttpServletRequest request) {
		User user = (User) request.getAttribute("authenticatedUser");
		userService.deleteUserProfile(user, password);
	}

	@GetMapping("/users/{id}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserGetDTO getUserById(@PathVariable Long id) {
		User requestedUser = userService.getUserById(id);
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(requestedUser);
	}

	@PatchMapping("/users/me/password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void changePassword(@RequestBody UserPasswordChangeDTO dto, HttpServletRequest request) {
		User user = (User) request.getAttribute("authenticatedUser");
		userService.changePassword(user, dto.getOldPassword(), dto.getNewPassword(), dto.getConfirmPassword());
	}

	@PutMapping("/users/me/avatar")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserGetDTO changeUserAvatar(@RequestBody UserPutAvatarDTO dto, HttpServletRequest request) {
		User requestingUser = (User) request.getAttribute("authenticatedUser");
		User userInput = DTOMapper.INSTANCE.convertUserPutAvatarDTOtoEntity(dto);
		User changedUser = userService.changeUserAvatar(requestingUser, userInput);
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(changedUser);
	}

}
