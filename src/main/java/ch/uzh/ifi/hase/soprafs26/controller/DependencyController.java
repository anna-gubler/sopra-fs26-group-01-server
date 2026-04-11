package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.Dependency;
import ch.uzh.ifi.hase.soprafs26.rest.dto.*;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.DependencyService;
@RestController
public class DependencyController {

    private final DependencyService dependencyService;

    public DependencyController(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    // 401
    @GetMapping("/skillmaps/{skillMapId}/dependencies")
    @ResponseStatus(HttpStatus.OK)
    public List<DependencyGetDTO> getDependenciesByMap(
            @PathVariable Long skillMapId,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring("Bearer ".length()).trim();
        return dependencyService.getDependenciesByMap(skillMapId, token)
            .stream()
            .map(DTOMapper.INSTANCE::convertDependencyEntityToGetDTO)
            .toList();
    }

    // 402
    @PostMapping("/skillmaps/{skillMapId}/dependencies")
    @ResponseStatus(HttpStatus.CREATED)
    public DependencyGetDTO createDependency(
            @PathVariable Long skillMapId,
            @RequestBody DependencyPostDTO dependencyPostDTO,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring("Bearer ".length()).trim();
        Dependency created = dependencyService.createDependency(skillMapId, dependencyPostDTO.getFromSkillId(), dependencyPostDTO.getToSkillId(), token);
        return DTOMapper.INSTANCE.convertDependencyEntityToGetDTO(created);
    }

    // 403
    @DeleteMapping("/dependencies/{dependencyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDependency(
            @PathVariable Long dependencyId,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring("Bearer ".length()).trim();
        dependencyService.deleteDependency(dependencyId, token);
    }
}
