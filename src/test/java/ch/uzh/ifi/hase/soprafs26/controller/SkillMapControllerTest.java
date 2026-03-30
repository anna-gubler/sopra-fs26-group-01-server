package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;

import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapPutDTO;
import ch.uzh.ifi.hase.soprafs26.service.SkillMapService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
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

    private static final String TOKEN = "Bearer test-token";

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
    @Test
    public void givenValidToken_whenGetAllSkillMaps_thenReturnOk() throws Exception {
        given(skillMapService.getSkillMaps(any())).willReturn(new ArrayList<>());

        mockMvc.perform(get("/skillmaps")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void givenInvalidToken_whenGetAllSkillMaps_thenReturnUnauthorized() throws Exception {
        given(skillMapService.getSkillMaps(any()))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        mockMvc.perform(get("/skillmaps")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // POST /skillmaps
    @Test
    public void givenValidToken_whenCreateSkillMap_thenReturnCreated() throws Exception {
        SkillMap skillMap = newSkillMap();
        given(skillMapService.createSkillMap(any(), any())).willReturn(skillMap);

        mockMvc.perform(post("/skillmaps")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(newSkillMapPostDTO())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is(skillMap.getTitle())));
    }

    @Test
    public void givenInvalidToken_whenCreateSkillMap_thenReturnUnauthorized() throws Exception {
        given(skillMapService.createSkillMap(any(), any()))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        mockMvc.perform(post("/skillmaps")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(newSkillMapPostDTO())))
                .andExpect(status().isUnauthorized());
    }

    // GET /skillmaps/{skillMapId}
    @Test
    public void givenValidToken_whenGetSkillMapById_thenReturnSkillMap() throws Exception {
        SkillMap skillMap = newSkillMap();
        given(skillMapService.getSkillMapById(eq(1L), any())).willReturn(skillMap);

        mockMvc.perform(get("/skillmaps/1")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(skillMap.getTitle())));
    }

    @Test
    public void givenInvalidId_whenGetSkillMapById_thenReturnNotFound() throws Exception {
        given(skillMapService.getSkillMapById(eq(999L), any()))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/skillmaps/999")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // PATCH /skillmaps/{skillMapId}
    @Test
    public void givenValidToken_whenUpdateSkillMap_thenReturnUpdated() throws Exception {
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

    @Test
    public void givenNonOwner_whenUpdateSkillMap_thenReturnForbidden() throws Exception {
        given(skillMapService.updateSkillMap(eq(1L), any(), any()))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(patch("/skillmaps/1")
                .header("Authorization", "Bearer other-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new SkillMapPutDTO())))
                .andExpect(status().isForbidden());
    }

    // DELETE /skillmaps/{skillMapId}
    @Test
    public void givenValidToken_whenDeleteSkillMap_thenReturnNoContent() throws Exception {
        mockMvc.perform(delete("/skillmaps/1")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void givenNonOwner_whenDeleteSkillMap_thenReturnForbidden() throws Exception {
        willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .given(skillMapService).deleteSkillMap(eq(1L), any());

        mockMvc.perform(delete("/skillmaps/1")
                .header("Authorization", "Bearer other-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // GET /skillmaps/{skillMapId}/members
    @Test
    public void givenValidToken_whenGetMembers_thenReturnOk() throws Exception {
        given(skillMapService.getMembers(eq(1L), any())).willReturn(new ArrayList<>());

        mockMvc.perform(get("/skillmaps/1/members")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // DELETE /skillmaps/{skillMapId}/members/{userId}
    @Test
    public void givenValidToken_whenRemoveMember_thenReturnNoContent() throws Exception {
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