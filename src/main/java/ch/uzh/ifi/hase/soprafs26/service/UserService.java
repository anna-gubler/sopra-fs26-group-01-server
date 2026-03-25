package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

import java.time.LocalDateTime;


/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

	private final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;

	public UserService(@Qualifier("userRepository") UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<User> getUsers() {
		return this.userRepository.findAll();
	}

	public User createUser(User newUser) {
		newUser.setToken(UUID.randomUUID().toString());
		newUser.setStatus(UserStatus.ONLINE);
		newUser.setCreationDate(LocalDateTime.now());
		checkIfUserExists(newUser);

		// saves the given entity but data is only persisted in the database once
		// flush() is called
		newUser = userRepository.save(newUser);
		userRepository.flush();

		log.debug("Created Information for User: {}", newUser);
		return newUser;
	}

	/**
	 * This is a helper method that will check the uniqueness criteria of the
	 * username and the name
	 * defined in the User entity. The method will do nothing if the input is unique
	 * and throw an error otherwise.
	 *
	 * @param userToBeCreated
	 * @throws org.springframework.web.server.ResponseStatusException
	 * @see User
	 */
	private void checkIfUserExists(User userToBeCreated) {
		User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

		if (userByUsername != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "The username provided is not unique. Therefore, the user could not be created!");
		}
	}

	/**
	 * This is a method that let's the user update their password and then logs them out of the platform.
	 */
	public void updateUser(User updatingUser, String newPassword) {
		updatingUser.setPassword(newPassword);
		updatingUser.setStatus(UserStatus.OFFLINE);
		updatingUser = userRepository.save(updatingUser);
		userRepository.flush();
	
		log.debug("Update Password for User: {}", updatingUser);
		// return updatingUser;
	}
	/**
	 * This is a method logs the user into the platform.
	 */
	public void loginUser(String username, String password) {
		User loginUser = getUserByUsername(username);
		if (loginUser.getPassword().equals(password)) {
			loginUser.setStatus(UserStatus.ONLINE);
			loginUser = userRepository.save(loginUser);
			userRepository.flush();
		
			log.debug("Logging User in: {}", loginUser);
		}
		else {
			log.debug("Given password is wrong: {}", loginUser.getId());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
					String.format("The User gave wrong password: %s.", password));		
		}
	}
	
	/**
	 * This is a method logs the user out of the platform.
	 */
	public void logoutUser(User logoutUser) {
		logoutUser.setStatus(UserStatus.OFFLINE);
		logoutUser = userRepository.save(logoutUser);
		userRepository.flush();
	
		log.debug("Logging User out: {}", logoutUser);
	}

	/**
	 * This is a method that searches for the user by its unique ID and that throws a 404 if the user can't be found.
	 */
	public User getUserById(Long userId) {
		Optional<User> requestedUser = userRepository.findById(userId);

		if (!requestedUser.isPresent()) {
			log.debug("User with ID could not be found by ID and 404 called: {}", userId, requestedUser);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					String.format("The User with the ID %s could not be found.", userId));
		} else {
			log.debug("User found by ID and returned: {}", requestedUser);	
			User confirmedUser = requestedUser.get();
			return confirmedUser;
		}
	}
	/**
	 * This is a method that searches for the user by its unique ID and that throws a 404 if the user can't be found.
	 */
	public User getUserByUsername(String username) {
		//I use User and not Optional<User> here, because in the UserRepository.java there this method is designed to give back type User. 
		//So I don't question that premade desicion. 
		User requestedUser = userRepository.findByUsername(username); 

		if (requestedUser == null) {
			log.debug("User with username could not be found by username and 404 called: {}", username);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					String.format("The User with the username %s could not be found.", username));
		} else {
			log.debug("User found by username and returned: {}", requestedUser);	
			return requestedUser;
		}
	}
	public User getUserByToken(String token) {
		//I use User and not Optional<User> here, because in the UserRepository.java there this method is designed to give back type User. 
		//So I don't question that premade desicion. 
		User requestedUser = userRepository.findByToken(token); 

		if (requestedUser == null) {
			log.debug("User with username could not be found by token and 404 called");
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					String.format("The User with this token could not be found."));
		} else {
			log.debug("User found by token and returned: {}", requestedUser);	
			return requestedUser;
		}
	}
}
