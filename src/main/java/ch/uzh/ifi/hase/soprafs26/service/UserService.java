package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.List;
import java.util.UUID;

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
	private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private final UserRepository userRepository;

	public UserService(@Qualifier("userRepository") UserRepository userRepository) {
		this.userRepository = userRepository;
	}




	// Basic Auth Functions first; register, login, logout, check token

	public User createUser(User newUser) {
		checkIfUserExists(newUser);

		newUser.setToken(UUID.randomUUID().toString());
		newUser.setStatus(UserStatus.ONLINE);
		newUser.setCreationDate(LocalDateTime.now());
		newUser.setPassword(hashPassword(newUser.getPassword()));
		newUser.setStyle("bottts-neutral");
		newUser.setSeed(newUser.getUsername());

		newUser = userRepository.save(newUser); // saves the given entity but data is only persisted in the database
												// once flush() is called
		userRepository.flush();
		log.debug("Created Information for User: {}", newUser);
		return newUser;
	}

	private void checkIfUserExists(User userToBeCreated) {
		userRepository.findByUsername(userToBeCreated.getUsername())
				.ifPresent(u -> { throw new ResponseStatusException(HttpStatus.CONFLICT,
						"The username provided is not unique. Therefore, the user could not be created!"); });
	}

	private String hashPassword(String password) {
		return passwordEncoder.encode(password);
	}

	public User loginUser(User userLoginData) {
		User userDBEntry = userRepository.findByUsername(userLoginData.getUsername())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or incomplete login request"));

		if (!passwordEncoder.matches(userLoginData.getPassword(), userDBEntry.getPassword())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
		}

		userDBEntry.setStatus(UserStatus.ONLINE);
		userDBEntry.setToken(UUID.randomUUID().toString());

		return userDBEntry;
	}

	public void logoutUser(User user) {
		user.setToken(null);
		user.setStatus(UserStatus.OFFLINE);
	}




	// Next: user related services that are offered

	public List<User> getUsers() {
		return this.userRepository.findAll();
	}


	public User getUserById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
	}

	public User getUserByUsername(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
	}

	public User getUserByToken(String token) {
		User user = userRepository.findByToken(token)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

		if (user.getStatus() != UserStatus.ONLINE) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not online");
		}

		return user;
	}

	public User changeUserInformation(User requestingUser, User userInput) {

		if (userInput.getUsername() != null) {
			checkIfUserExists(userInput);
			requestingUser.setUsername(userInput.getUsername());
		}

		if (userInput.getBio() != null) {
			requestingUser.setBio(userInput.getBio());
		}

		if (userInput.getPassword() != null) {
			requestingUser.setPassword(hashPassword(userInput.getPassword()));
		}

		return requestingUser;
	}

	public void deleteUserProfile(User user, String password) {
		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
		}
		userRepository.delete(user);
		userRepository.flush();
	}

	public User changeUserAvatar(User requestingUser, User userInput) {

		if (userInput.getStyle() != null) {
			requestingUser.setStyle(userInput.getStyle());
		}

		if (userInput.getSeed() != null) {
			requestingUser.setSeed(userInput.getSeed());
		}

		return requestingUser;
	}

}
