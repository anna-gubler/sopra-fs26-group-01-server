package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMapMembership;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapGraphDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapJoinDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapMembershipGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.SkillMapService;

@RestController
@RequestMapping("/skillmaps")
public class SkillMapController {

    private final SkillMapService skillMapService;

    public SkillMapController(SkillMapService skillMapService) {
        this.skillMapService = skillMapService;
    }

    // 201 - GET /skillmaps - returns only maps the requester is a member of
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<SkillMapGetDTO> getAllSkillMaps(HttpServletRequest request) {
        User requester = (User) request.getAttribute("authenticatedUser");
        List<SkillMap> skillMaps = skillMapService.getSkillMaps(requester);
        List<SkillMapGetDTO> skillMapGetDTOs = new ArrayList<>();
        for (SkillMap skillmap : skillMaps) {
            skillMapGetDTOs.add(DTOMapper.INSTANCE.convertEntityToSkillMapGetDTO(skillmap));
        }
        return skillMapGetDTOs;
    }

    // 202 - POST /skillmaps
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SkillMapGetDTO createSkillMap(@RequestBody SkillMapPostDTO skillMapPostDTO, HttpServletRequest request) {
        User requester = (User) request.getAttribute("authenticatedUser");
        SkillMap newSkillMap = DTOMapper.INSTANCE.convertSkillMapPostDTOtoEntity(skillMapPostDTO);
        SkillMap created = skillMapService.createSkillMap(newSkillMap, requester);
        return DTOMapper.INSTANCE.convertEntityToSkillMapGetDTO(created);
    }

    // 203 - GET /skillmaps/{skillMapId}
    @GetMapping("/{skillMapId}")
    @ResponseStatus(HttpStatus.OK)
    public SkillMapGetDTO getSkillMapById(@PathVariable Long skillMapId, HttpServletRequest request) {
        User requester = (User) request.getAttribute("authenticatedUser");
        SkillMap skillMap = skillMapService.getSkillMapById(skillMapId, requester);
        return DTOMapper.INSTANCE.convertEntityToSkillMapGetDTO(skillMap);
    }

    // 204 - PATCH /skillmaps/{skillMapId} (spec 204 uses PATCH, not PUT)
    @PatchMapping("/{skillMapId}")
    @ResponseStatus(HttpStatus.OK)
    public SkillMapGetDTO updateSkillMap(@PathVariable Long skillMapId, @RequestBody SkillMapPutDTO skillMapPutDTO, HttpServletRequest request) {
        User requester = (User) request.getAttribute("authenticatedUser");
        SkillMap updates = DTOMapper.INSTANCE.convertSkillMapPutDTOtoEntity(skillMapPutDTO);
        SkillMap updated = skillMapService.updateSkillMap(skillMapId, updates, requester);
        return DTOMapper.INSTANCE.convertEntityToSkillMapGetDTO(updated);
    }

    // 205 - DELETE /skillmaps/{skillMapId}
    @DeleteMapping("/{skillMapId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSkillMap(@PathVariable Long skillMapId, HttpServletRequest request) {
        User requester = (User) request.getAttribute("authenticatedUser");
        skillMapService.deleteSkillMap(skillMapId, requester);
    }

    // 206 - POST /skillmaps/join
    @PostMapping("/join")
    @ResponseStatus(HttpStatus.CREATED)
    public SkillMapMembershipGetDTO joinSkillMap(@RequestBody SkillMapJoinDTO joinDTO, HttpServletRequest request) {
        User requester = (User) request.getAttribute("authenticatedUser");
        SkillMapMembership membership = skillMapService.joinSkillMap(joinDTO.getInviteCode(), requester);
        return DTOMapper.INSTANCE.convertEntityToSkillMapMembershipGetDTO(membership);
    }

    // 207 - GET /skillmaps/{skillMapId}/members
    @GetMapping("/{skillMapId}/members")
    @ResponseStatus(HttpStatus.OK)
    public List<SkillMapMembershipGetDTO> getMembers(@PathVariable Long skillMapId, HttpServletRequest request) {
        User requester = (User) request.getAttribute("authenticatedUser");
        return skillMapService.getMembers(skillMapId, requester)
            .stream()
            .map(DTOMapper.INSTANCE::convertEntityToSkillMapMembershipGetDTO)
            .toList();
    }


    // 208 - DELETE /skillmaps/{skillMapId}/members/{userId}
    @DeleteMapping("/{skillMapId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable Long skillMapId, @PathVariable Long userId, HttpServletRequest request) {
        User requester = (User) request.getAttribute("authenticatedUser");
        skillMapService.removeMember(skillMapId, userId, requester);
    }

    // 209 - GET /skillmaps/{skillMapId}/graph
    @GetMapping("/{skillMapId}/graph")
    @ResponseStatus(HttpStatus.OK)
    public SkillMapGraphDTO getSkillMapGraph(@PathVariable Long skillMapId, HttpServletRequest request) {
        User requester = (User) request.getAttribute("authenticatedUser");
        return skillMapService.getSkillMapGraph(skillMapId, requester);
    }
}
