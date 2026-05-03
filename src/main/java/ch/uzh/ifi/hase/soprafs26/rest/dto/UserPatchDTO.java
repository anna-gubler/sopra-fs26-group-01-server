package ch.uzh.ifi.hase.soprafs26.rest.dto;


public class UserPatchDTO {

	private Long id;

	private String username;

	private String bio;

	private String password;

    private String token;

	private Boolean hasSeenDashboard;

	private Boolean hasSeenMap;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

	public Boolean getHasSeenDashboard() {
		return hasSeenDashboard;
	}

	public void setHasSeenDashboard(Boolean hasSeenDashboard) {
		this.hasSeenDashboard = hasSeenDashboard;
	}

	public Boolean getHasSeenMap() {
		return hasSeenMap;
	}

	public void setHasSeenMap(Boolean hasSeenMap) {
		this.hasSeenMap = hasSeenMap;
	}
}
