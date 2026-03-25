package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.SkillMapService;

@RestController
@RequestMapping("/skillmaps")
public class SkillMapController {
    
    private final SkillMapService skillMapService;

    public SkillMapController(SkillMapService skillMapService) {
        this.skillMapService = skillMapService;
    }

    // 201.1 - GET /skillmaps
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<SkillMapGetDTO> getAllSkillMaps(@RequestHeader("Authorization") String token) {
        List<SkillMap> skillMaps = skillMapService.getSkillMaps(); 
		List<SkillMapGetDTO> skillMapGetDTOs = new ArrayList<>();

        for (SkillMap skillmap : skillMaps) {
            skillMapGetDTOs.add(DTOMapper.INSTANCE.convertEntityToSkillMapGetDTO(skillmap));
        }
        return skillMapGetDTOs;
    }

    // 202.1 - POST /skillmaps
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SkillMapGetDTO createSkillMap(@RequestBody SkillMapPostDTO skillMapPostDTO, @RequestHeader("Authorization") String token) {
        SkillMap newSkillMap = DTOMapper.INSTANCE.convertSkillMapPostDTOtoEntity(skillMapPostDTO);
        SkillMap created = skillMapService.createSkillMap(newSkillMap, token);
        return DTOMapper.INSTANCE.convertEntityToSkillMapGetDTO(created);
    }

    // 203.1 - GET /skillmaps/{skillMapId}
    @GetMapping("/{skillMapId}")
    @ResponseStatus(HttpStatus.OK)
    public SkillMapGetDTO getSkillMapById(@PathVariable Long skillMapId, @RequestHeader("Authorization") String token) {
        SkillMap skillMap = skillMapService.getSkillMapById(skillMapId, token);
        return DTOMapper.INSTANCE.convertEntityToSkillMapGetDTO(skillMap);
    }

    // 204.x - PATCH /skillmaps/{skillMapId}
    // @PatchMapping("/{skillMapId}")
    // public <SkillMapDTO> updateSkillMap(@PathVariable Long skillMapId, @RequestBody SkillMapPatchDTO dto, ...) {

    // }

    // 205.x - DELETE /skillmaps/{skillMapId}
    // @DeleteMapping("/{skillMapId}")
    // public <Void> deleteSkillMap(@PathVariable Long skillMapId, ...) {

    // }

    // 206.x - POST /skillmaps/join
    // @PostMapping("/join")
    // public <SkillMapMembershipDTO> joinSkillMap(@RequestBody JoinDTO dto, ...) {

    // }

    // 207.x - GET /skillmaps/{skillMapId}/members
    // @GetMapping("/{skillMapId}/members")
    // public <List<UserDTO>> getMembers(@PathVariable Long skillMapId, ...) {

    // }

    // 208.x - DELETE /skillmaps/{skillMapId}/members/{userId}
    // @DeleteMapping("/{skillMapId}/members/{userId}")
    // public <Void> removeMember(@PathVariable Long skillMapId, @PathVariable Long userId, ...) {

    // }

    // 209.x - GET /skillmaps/{skillMapId}/graph
    // @GetMapping("/{skillMapId}/graph")
    // public <SkillMapGraphDTO> getGraph(@PathVariable Long skillMapId, ...) {

    // }
}
