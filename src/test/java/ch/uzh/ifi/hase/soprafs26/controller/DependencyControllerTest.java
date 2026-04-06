package ch.uzh.ifi.hase.soprafs26.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.rest.dto.DependencyPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.DependencyService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

@WebMvcTest(DependencyController.class)
@AutoConfigureMockMvc(addFilters = false)
class DependencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DependencyService dependencyService;

    @MockitoBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Dependency buildDependency() {
        SkillMap skillMap = new SkillMap();
        skillMap.setId(10L);
        skillMap.setOwnerId(1L);

        Skill fromSkill = new Skill();
        fromSkill.setId(100L);
        fromSkill.setLevel(1);
        fromSkill.setSkillMap(skillMap);

        Skill toSkill = new Skill();
        toSkill.setId(200L);
        toSkill.setLevel(2);
        toSkill.setSkillMap(skillMap);

        Dependency d = new Dependency();
        d.setId(1000L);
        d.setFromSkill(fromSkill);
        d.setToSkill(toSkill);
        return d;
    }

    @BeforeEach
    void setupAuth() {
        User dummyUser = new User();
        dummyUser.setId(1L);
        given(userService.getUserByToken(any())).willReturn(dummyUser);
    }

    // 401 GET /skillmaps/{skillMapId}/dependencies

    @Test
    void getDependenciesByMap_returns200() throws Exception {
        when(dependencyService.getDependenciesByMap(eq(10L), any()))
            .thenReturn(List.of(buildDependency()));

        mockMvc.perform(get("/skillmaps/10/dependencies")
                .header("Authorization", "Bearer owner-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1000))
            .andExpect(jsonPath("$[0].fromSkillId").value(100))
            .andExpect(jsonPath("$[0].toSkillId").value(200));
    }

    @Test
    void getDependenciesByMap_forbidden_returns403() throws Exception {
        when(dependencyService.getDependenciesByMap(eq(10L), any()))
            .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(get("/skillmaps/10/dependencies")
                .header("Authorization", "Bearer wrong-token"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getDependenciesByMap_notFound_returns404() throws Exception {
        when(dependencyService.getDependenciesByMap(eq(99L), any()))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/skillmaps/99/dependencies")
                .header("Authorization", "Bearer owner-token"))
            .andExpect(status().isNotFound());
    }

    // 402 POST /skillmaps/{skillMapId}/dependencies 

    @Test
    void createDependency_returns201() throws Exception {
        DependencyPostDTO dto = new DependencyPostDTO();
        dto.setFromSkillId(100L);
        dto.setToSkillId(200L);

        when(dependencyService.createDependency(eq(10L), eq(100L), eq(200L), any()))
            .thenReturn(buildDependency());

        mockMvc.perform(post("/skillmaps/10/dependencies")
                .header("Authorization", "Bearer owner-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1000));
    }

    @Test
    void createDependency_badRequest_returns400() throws Exception {
        DependencyPostDTO dto = new DependencyPostDTO();
        dto.setFromSkillId(100L);
        dto.setToSkillId(200L);

        when(dependencyService.createDependency(eq(10L), eq(100L), eq(200L), any()))
            .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));

        mockMvc.perform(post("/skillmaps/10/dependencies")
                .header("Authorization", "Bearer owner-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createDependency_forbidden_returns403() throws Exception {
        DependencyPostDTO dto = new DependencyPostDTO();
        dto.setFromSkillId(100L);
        dto.setToSkillId(200L);

        when(dependencyService.createDependency(eq(10L), eq(100L), eq(200L), any()))
            .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(post("/skillmaps/10/dependencies")
                .header("Authorization", "Bearer wrong-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }

    @Test
    void createDependency_conflict_returns409() throws Exception {
        DependencyPostDTO dto = new DependencyPostDTO();
        dto.setFromSkillId(100L);
        dto.setToSkillId(200L);

        when(dependencyService.createDependency(eq(10L), eq(100L), eq(200L), any()))
            .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT));

        mockMvc.perform(post("/skillmaps/10/dependencies")
                .header("Authorization", "Bearer owner-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isConflict());
    }

    // 403 DELETE /dependencies/{dependencyId} 

    @Test
    void deleteDependency_returns204() throws Exception {
        doNothing().when(dependencyService).deleteSkill(eq(1000L), any());

        mockMvc.perform(delete("/dependencies/1000")
                .header("Authorization", "Bearer owner-token"))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteDependency_forbidden_returns403() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
            .when(dependencyService).deleteSkill(eq(1000L), any());

        mockMvc.perform(delete("/dependencies/1000")
                .header("Authorization", "Bearer wrong-token"))
            .andExpect(status().isForbidden());
    }

    @Test
    void deleteDependency_notFound_returns404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
            .when(dependencyService).deleteSkill(eq(99L), any());

        mockMvc.perform(delete("/dependencies/99")
                .header("Authorization", "Bearer owner-token"))
            .andExpect(status().isNotFound());
    }
}