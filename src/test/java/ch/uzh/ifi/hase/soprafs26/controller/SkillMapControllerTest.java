package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;

import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMapMembership;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapJoinDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapPutDTO;
import ch.uzh.ifi.hase.soprafs26.service.SkillMapService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SkillMapController.class)
public class SkillMapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SkillMapService skillMapService;

    // AuthInterceptor runs before every protected request and calls userService.getUserByToken().
    // This bean must be present so the interceptor can be wired up in the test context.
    // The controller itself does not use UserService directly.
    @MockitoBean
    private UserService userService;

    private static final String TOKEN = "Bearer test-token";

    private static User buildUser() {
        User user = new User();
        user.setId(1L);
        return user;
    }

    // AuthInterceptor runs before every protected request and calls userService.getUserByToken().
    // This mock makes the interceptor pass and sets the resolved user as a request attribute,
    // which the controller then reads. UserService itself is not used by the controller directly.
    private void mockAuthentication(User user, boolean success) {
        if (success) {
            given(userService.getUserByToken(any())).willReturn(user);
        }
    }

    private static SkillMap newSkillMap() {
        User owner = new User();
        //owner.setId(1L);                  // to be uncommented after merging S2, now code i snot compiling because of missing setId in User entity
        owner.setUsername("owner");

        SkillMap skillMap = new SkillMap();
        skillMap.setId(1L);
        skillMap.setTitle("Test Map");
        skillMap.setDescription("Test Description");
        skillMap.setNumberOfLevels(3);
        skillMap.setIsPublic(false);
        //skillMap.setOwner(owner);
        return skillMap;
    }

    private static SkillMapPostDTO newSkillMapPostDTO() {
        SkillMapPostDTO dto = new SkillMapPostDTO();
        dto.setTitle("Test Map");
        dto.setDescription("Test Description");
        dto.setNumberOfLevels(3);
        dto.setIsPublic(false);
        return dto;
    }

    // GET /skillmaps
    // Test: valid token returns 200 with the list of skill maps the user is a member of
    @Test
    public void givenValidToken_whenGetAllSkillMaps_thenReturnOk() throws Exception {
        mockAuthentication(buildUser(), true);
        given(skillMapService.getSkillMaps(any())).willReturn(new ArrayList<>());

        mockMvc.perform(get("/skillmaps")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Test: invalid token is rejected with 401 Unauthorized
    @Test
    public void givenInvalidToken_whenGetAllSkillMaps_thenReturnUnauthorized() throws Exception {
        mockAuthentication(buildUser(), false);
        given(skillMapService.getSkillMaps(any()))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        mockMvc.perform(get("/skillmaps")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // POST /skillmaps
    // Test: valid token and body creates a skill map and returns 201 with the map title
    @Test
    public void givenValidToken_whenCreateSkillMap_thenReturnCreated() throws Exception {
        mockAuthentication(buildUser(), true);
        SkillMap skillMap = newSkillMap();
        given(skillMapService.createSkillMap(any(), any())).willReturn(skillMap);

        mockMvc.perform(post("/skillmaps")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(newSkillMapPostDTO())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is(skillMap.getTitle())));
    }

    // Test: invalid token is rejected with 401 Unauthorized
    @Test
    public void givenInvalidToken_whenCreateSkillMap_thenReturnUnauthorized() throws Exception {
        mockAuthentication(buildUser(), false);
        given(skillMapService.createSkillMap(any(), any()))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        mockMvc.perform(post("/skillmaps")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(newSkillMapPostDTO())))
                .andExpect(status().isUnauthorized());
    }

    // GET /skillmaps/{skillMapId}
    // Test: valid token and existing ID returns 200 with the skill map
    @Test
    public void givenValidToken_whenGetSkillMapById_thenReturnSkillMap() throws Exception {
        mockAuthentication(buildUser(), true);
        SkillMap skillMap = newSkillMap();
        given(skillMapService.getSkillMapById(eq(1L), any())).willReturn(skillMap);

        mockMvc.perform(get("/skillmaps/1")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(skillMap.getTitle())));
    }

    // Test: non-existent skill map ID returns 404 Not Found
    @Test
    public void givenInvalidId_whenGetSkillMapById_thenReturnNotFound() throws Exception {
        mockAuthentication(buildUser(), true);
        given(skillMapService.getSkillMapById(eq(999L), any()))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/skillmaps/999")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // PATCH /skillmaps/{skillMapId}
    // Test: owner can update the skill map and receives 200 with the updated title
    @Test
    public void givenValidToken_whenUpdateSkillMap_thenReturnUpdated() throws Exception {
        mockAuthentication(buildUser(), true);
        SkillMap skillMap = newSkillMap();
        skillMap.setTitle("Updated Title");
        given(skillMapService.updateSkillMap(eq(1L), any(), any())).willReturn(skillMap);

        SkillMapPutDTO dto = new SkillMapPutDTO();
        dto.setTitle("Updated Title");

        mockMvc.perform(patch("/skillmaps/1")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Title")));
    }

    // Test: non-owner trying to update a skill map is rejected with 403 Forbidden
    @Test
    public void givenNonOwner_whenUpdateSkillMap_thenReturnForbidden() throws Exception {
        mockAuthentication(buildUser(), true);
        given(skillMapService.updateSkillMap(eq(1L), any(), any()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(patch("/skillmaps/1")
                .header("Authorization", "Bearer other-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new SkillMapPutDTO())))
                .andExpect(status().isForbidden());
    }

    // DELETE /skillmaps/{skillMapId}
    // Test: owner can delete a skill map and receives 204 No Content
    @Test
    public void givenValidToken_whenDeleteSkillMap_thenReturnNoContent() throws Exception {
        mockAuthentication(buildUser(), true);
        mockMvc.perform(delete("/skillmaps/1")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    // Test: non-owner trying to delete a skill map is rejected with 403 Forbidden
    @Test
    public void givenNonOwner_whenDeleteSkillMap_thenReturnForbidden() throws Exception {
        mockAuthentication(buildUser(), true);
        willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .given(skillMapService).deleteSkillMap(eq(1L), any());

        mockMvc.perform(delete("/skillmaps/1")
                .header("Authorization", "Bearer other-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // POST /skillmaps/join
    // Test: valid invite code and skill map ID returns the created membership with 201
    @Test
    public void givenValidInviteCode_whenJoinSkillMap_thenReturnCreated() throws Exception {
        mockAuthentication(buildUser(), true);
        SkillMapMembership membership = new SkillMapMembership();
        given(skillMapService.joinSkillMap(any(), any())).willReturn(membership);

        SkillMapJoinDTO dto = new SkillMapJoinDTO();
        dto.setInviteCode("VALIDCODE1");

        mockMvc.perform(post("/skillmaps/join")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto)))
                .andExpect(status().isCreated());
    }

    // Test: wrong invite code is rejected with 403 Forbidden
    @Test
    public void givenWrongInviteCode_whenJoinSkillMap_thenReturnForbidden() throws Exception {
        mockAuthentication(buildUser(), true);
        given(skillMapService.joinSkillMap(any(), any()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        SkillMapJoinDTO dto = new SkillMapJoinDTO();
        dto.setInviteCode("WRONGCODE1");

        mockMvc.perform(post("/skillmaps/join")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto)))
                .andExpect(status().isForbidden());
    }

    // Test: joining a map the user is already a member of returns 409 Conflict
    @Test
    public void givenUserAlreadyMember_whenJoinSkillMap_thenReturnConflict() throws Exception {
        mockAuthentication(buildUser(), true);
        given(skillMapService.joinSkillMap(any(), any()))
                .willThrow(new ResponseStatusException(HttpStatus.CONFLICT));

        SkillMapJoinDTO dto = new SkillMapJoinDTO();
        dto.setInviteCode("VALIDCODE1");

        mockMvc.perform(post("/skillmaps/join")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto)))
                .andExpect(status().isConflict());
    }

    // Test: joining a skill map that does not exist returns 404 Not Found
    @Test
    public void givenNonExistentSkillMapId_whenJoinSkillMap_thenReturnNotFound() throws Exception {
        mockAuthentication(buildUser(), true);
        given(skillMapService.joinSkillMap(any(), any()))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        SkillMapJoinDTO dto = new SkillMapJoinDTO();
        dto.setInviteCode("anycode");

        mockMvc.perform(post("/skillmaps/join")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto)))
                .andExpect(status().isNotFound());
    }

    // GET /skillmaps/{skillMapId}/members
    // Test: valid token returns 200 with the list of members for a skill map
    @Test
    public void givenValidToken_whenGetMembers_thenReturnOk() throws Exception {
        mockAuthentication(buildUser(), true);
        given(skillMapService.getMembers(eq(1L), any())).willReturn(new ArrayList<>());

        mockMvc.perform(get("/skillmaps/1/members")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // DELETE /skillmaps/{skillMapId}/members/{userId}
    // Test: owner or the member themselves can remove a membership and receives 204 No Content
    @Test
    public void givenValidToken_whenRemoveMember_thenReturnNoContent() throws Exception {
        mockAuthentication(buildUser(), true);
        mockMvc.perform(delete("/skillmaps/1/members/2")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JacksonException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }
}