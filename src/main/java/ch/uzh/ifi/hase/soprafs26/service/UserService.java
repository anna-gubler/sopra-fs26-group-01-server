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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

	public User getUserById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
	}

	public User changeUserInformation(User user, User userInput) {
		User userDBEntry = userRepository.findById(userInput.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		// authenticate user
		if (!user.getId().equals(userDBEntry.getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User can only change his own Information");
		}

		if (userInput.getUsername() != null) {
			checkIfUserExists(userInput);
			userDBEntry.setUsername(userInput.getUsername());
		}

		if (userInput.getName() != null) {
			userDBEntry.setName(userInput.getName());
		}

		if (userInput.getBio() != null) {
			userDBEntry.setBio(userInput.getBio());
		}

		if (userInput.getPassword() != null) {
			userDBEntry.setPassword(userInput.getPassword());
		}

		return userDBEntry;
	}

	public User loginUser(User userLoginData) {
		User userDBEntry = userRepository.findByUsername(userLoginData.getUsername());

		if (userDBEntry == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}

		if (!userLoginData.getPassword().equals(userDBEntry.getPassword())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong password");
		}

		userDBEntry.setStatus(UserStatus.ONLINE);
		userDBEntry.setToken(UUID.randomUUID().toString());

		return userDBEntry;
	}

	public void logoutUser(User user) {
		user.setToken(null);
		user.setStatus(UserStatus.OFFLINE);
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
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					String.format("The username provided is not unique. Therefore, the user could not be created!"));
		}
	}

	public User checkToken(String authorizationHeader) {
		if (authorizationHeader == null || authorizationHeader.isBlank()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization header");
		}

		// erwartet: "Bearer <token>"
		final String prefix = "Bearer ";
		if (!authorizationHeader.startsWith(prefix)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Authorization header");
		}

		String token = authorizationHeader.substring(prefix.length()).trim();
		if (token.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token");
		}

		User user = userRepository.findByToken(token)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

		if (user.getStatus() != UserStatus.ONLINE) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not online");
		}

		return user;
	}
}
