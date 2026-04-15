package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.Dependency;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.*;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.DependencyService;
import jakarta.servlet.http.HttpServletRequest;
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
            HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        return dependencyService.getDependenciesByMap(skillMapId, user)
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
            HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        Dependency created = dependencyService.createDependency(skillMapId, dependencyPostDTO.getFromSkillId(), dependencyPostDTO.getToSkillId(), user);
        return DTOMapper.INSTANCE.convertDependencyEntityToGetDTO(created);
    }

    // 403
    @DeleteMapping("/dependencies/{dependencyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDependency(
            @PathVariable Long dependencyId,
            HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        dependencyService.deleteDependency(dependencyId, user);
    }
}
