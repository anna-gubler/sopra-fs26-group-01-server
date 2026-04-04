package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Skill;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.SkillService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    private String extractToken(String authHeader) {
        return authHeader.substring("Bearer ".length()).trim();
    }

    // 301 - GET /skillmaps/{skillMapId}/skills
    @GetMapping("/skillmaps/{skillMapId}/skills")
    @ResponseStatus(HttpStatus.OK)
    public List<SkillGetDTO> getSkillsByMap(@PathVariable Long skillMapId,
                                             @RequestHeader("Authorization") String authHeader) {
        return skillService.getSkillsByMap(skillMapId, extractToken(authHeader))
            .stream()
            .map(DTOMapper.INSTANCE::convertEntityToSkillGetDTO)
            .collect(Collectors.toList());
    }

    // 302 - POST /skillmaps/{skillMapId}/skills
    @PostMapping("/skillmaps/{skillMapId}/skills")
    @ResponseStatus(HttpStatus.CREATED)
    public SkillGetDTO createSkill(@PathVariable Long skillMapId,
                                   @RequestBody SkillPostDTO skillPostDTO,
                                   @RequestHeader("Authorization") String authHeader) {
        Skill skill = DTOMapper.INSTANCE.convertSkillPostDTOtoEntity(skillPostDTO);
        Skill created = skillService.createSkill(skillMapId, skill, extractToken(authHeader));
        return DTOMapper.INSTANCE.convertEntityToSkillGetDTO(created);
    }

    // 303 - GET /skills/{skillId}
    @GetMapping("/skills/{skillId}")
    @ResponseStatus(HttpStatus.OK)
    public SkillGetDTO getSkillById(@PathVariable Long skillId,
                                    @RequestHeader("Authorization") String authHeader) {
        Skill skill = skillService.getSkillById(skillId, extractToken(authHeader));
        return DTOMapper.INSTANCE.convertEntityToSkillGetDTO(skill);
    }

    // 304 - PATCH /skills/{skillId}
    @PatchMapping("/skills/{skillId}")
    @ResponseStatus(HttpStatus.OK)
    public SkillGetDTO updateSkill(@PathVariable Long skillId,
                                   @RequestBody SkillPutDTO skillPutDTO,
                                   @RequestHeader("Authorization") String authHeader) {
        Skill incoming = DTOMapper.INSTANCE.convertSkillPutDTOtoEntity(skillPutDTO);
        Skill updated = skillService.updateSkill(skillId, incoming, extractToken(authHeader));
        return DTOMapper.INSTANCE.convertEntityToSkillGetDTO(updated);
    }

    // 305 - DELETE /skills/{skillId}
    @DeleteMapping("/skills/{skillId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSkill(@PathVariable Long skillId,
                             @RequestHeader("Authorization") String authHeader) {
        skillService.deleteSkill(skillId, extractToken(authHeader));
    }
}