package ch.uzh.ifi.hase.soprafs26.rest.dto;
import jakarta.validation.constraints.NotBlank;



public class UserPostDTO {

	@NotBlank
	private String bio;

	@NotBlank
	private String username;

	@NotBlank
	private String password;

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
