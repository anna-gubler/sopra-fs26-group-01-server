package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.service.SkillService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;


import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import ch.uzh.ifi.hase.soprafs26.entity.User;

@WebMvcTest(SkillController.class)
@AutoConfigureMockMvc(addFilters = false)
class SkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SkillService skillService;
    
    @MockitoBean
    private UserService userService;
    
    @BeforeEach
    void setupMocks() {
        User dummyUser = new User();
        dummyUser.setId(1L);
        given(userService.getUserByToken(any())).willReturn(dummyUser);}

    private Skill buildSkill(Long id, String name, int level) {
        Skill s = new Skill();
        s.setId(id);
        s.setName(name);
        s.setLevel(level);
        return s;
    }

    // 301  GET /skillmaps/{skillMapId}/skills
    // 301.1
    @Test
    void getSkillsByMap_validToken_returnsSkillList() throws Exception {
        Skill s1 = buildSkill(1L, "Variables", 1);
        Skill s2 = buildSkill(2L, "Loops",     2);

        given(skillService.getSkillsByMap(10L, "valid-token"))
                .willReturn(List.of(s1, s2));

        MockHttpServletRequestBuilder request = get("/skillmaps/10/skills")
                .header("Authorization", "Bearer valid-token")
                .header("token", "valid-token")
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Variables")))
                .andExpect(jsonPath("$[1].name", is("Loops")));
    }
    // 301.2
    @Test
    void getSkillsByMap_invalidToken_returns403() throws Exception {
        given(skillService.getSkillsByMap(10L, "terrifying-bad-token"))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized"));

        mockMvc.perform(get("/skillmaps/10/skills")
                        .header("Authorization", "Bearer terrifying-bad-token")
                        .header("token", "terrifying-bad-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
    // 301.3
    @Test
    void getSkillsByMap_skillMapNotFound_returns404() throws Exception {
        given(skillService.getSkillsByMap(99L, "valid-token"))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill map not found"));

        mockMvc.perform(get("/skillmaps/99/skills")
                        .header("Authorization", "Bearer valid-token")
                        .header("token", "valid-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // 302  POST /skillmaps/{skillMapId}/skills
    // 302.1
    @Test
    void createSkill_validInput_returnsCreated() throws Exception {
        Skill created = buildSkill(3L, "Davinci", 2);

        given(skillService.createSkill(
                Mockito.eq(10L),
                Mockito.any(Skill.class),
                Mockito.eq("valid-token")))
                .willReturn(created);

        String body = """
                { "name": "Davinci", "level": 2 }
                """;

        mockMvc.perform(post("/skillmaps/10/skills")
                        .header("Authorization", "Bearer valid-token")
                        .header("token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id",    is(3)))
                .andExpect(jsonPath("$.name",  is("Davinci")))
                .andExpect(jsonPath("$.level", is(2)));
    }
    // 302.3
    @Test
    void createSkill_notOwner_returns403() throws Exception {
        given(skillService.createSkill(
                Mockito.eq(10L),
                Mockito.any(Skill.class),
                Mockito.eq("other-token")))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not the owner"));

        mockMvc.perform(post("/skillmaps/10/skills")
                        .header("Authorization", "Bearer other-token")
                        .header("token", "other-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"X\", \"level\": 1 }"))
                .andExpect(status().isForbidden());
    }
    // 302.4
    @Test
    void createSkill_skillMapNotFound_returns404() throws Exception {
        given(skillService.createSkill(
                Mockito.eq(99L),
                Mockito.any(Skill.class),
                Mockito.eq("valid-token")))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill map not found"));

        mockMvc.perform(post("/skillmaps/99/skills")
                        .header("Authorization", "Bearer valid-token")
                        .header("token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"X\", \"level\": 1 }"))
                .andExpect(status().isNotFound());
    }

    // 303  GET /skills/{skillId} 
    // 303.1
    @Test
    void getSkillById_exists_returnsSkill() throws Exception {
        Skill skill = buildSkill(5L, "Recursion", 3);

        given(skillService.getSkillById(5L, "valid-token")).willReturn(skill);

        mockMvc.perform(get("/skills/5")
                        .header("Authorization", "Bearer valid-token")
                        .header("token", "valid-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id",   is(5)))
                .andExpect(jsonPath("$.name", is("Recursion")));
    }
    // 303.3
    @Test
    void getSkillById_notFound_returns404() throws Exception {
        given(skillService.getSkillById(99L, "valid-token"))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found"));

        mockMvc.perform(get("/skills/99")
                        .header("Authorization", "Bearer valid-token")
                        .header("token", "valid-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // 304  PATCH /skills/{skillId} 
    // 304.1
    @Test
    void updateSkill_validInput_returnsUpdatedSkill() throws Exception {
        Skill updated = buildSkill(5L, "Recursion (updated)", 3);

        given(skillService.updateSkill(
                Mockito.eq(5L),
                Mockito.any(Skill.class),
                Mockito.eq("valid-token")))
                .willReturn(updated);

        String body = """
                { "name": "Recursion (updated)" }
                """;

        mockMvc.perform(patch("/skills/5")
                        .header("Authorization", "Bearer valid-token")
                        .header("token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Recursion (updated)")));
    }
    // 304.3
    @Test
    void updateSkill_notOwner_returns403() throws Exception {
        given(skillService.updateSkill(
                Mockito.eq(5L),
                Mockito.any(Skill.class),
                Mockito.eq("other-token")))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not the owner"));

        mockMvc.perform(patch("/skills/5")
                        .header("Authorization", "Bearer other-token")
                        .header("token", "other-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"X\" }"))
                .andExpect(status().isForbidden());
    }
    // 304.4
    @Test
    void updateSkill_notFound_returns404() throws Exception {
        given(skillService.updateSkill(
                Mockito.eq(99L),
                Mockito.any(Skill.class),
                Mockito.eq("valid-token")))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found"));

        mockMvc.perform(patch("/skills/99")
                        .header("Authorization", "Bearer valid-token")
                        .header("token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"X\" }"))
                .andExpect(status().isNotFound());
    }

    // 305  DELETE /skills/{skillId}
    // 305.1
    @Test
    void deleteSkill_validRequest_returns204() throws Exception {
        doNothing().when(skillService).deleteSkill(5L, "valid-token");

        mockMvc.perform(delete("/skills/5")
                        .header("Authorization", "Bearer valid-token")
                        .header("token", "valid-token"))
                .andExpect(status().isNoContent());
    }
    // 305.2
    @Test
    void deleteSkill_notOwner_returns403() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not the owner"))
                .when(skillService).deleteSkill(5L, "other-token");

        mockMvc.perform(delete("/skills/5")
                        .header("Authorization", "Bearer other-token")
                        .header("token", "other-token"))
                .andExpect(status().isForbidden());
    }
    // 305.3
    @Test
    void deleteSkill_notFound_returns404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found"))
                .when(skillService).deleteSkill(99L, "valid-token");

        mockMvc.perform(delete("/skills/99")
                        .header("Authorization", "Bearer valid-token")
                        .header("token", "valid-token"))
                .andExpect(status().isNotFound());
    }

}