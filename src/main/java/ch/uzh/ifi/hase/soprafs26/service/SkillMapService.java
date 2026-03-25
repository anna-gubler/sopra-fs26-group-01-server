package ch.uzh.ifi.hase.soprafs26.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.User;

import ch.uzh.ifi.hase.soprafs26.repository.SkillMapRepository;

@Service
@Transactional
public class SkillMapService {
	private final Logger log = LoggerFactory.getLogger(SkillMapService.class);

    private final SkillMapRepository skillMapRepository;
    private final UserService userService;

    public SkillMapService(@Qualifier("skillMapRepository") SkillMapRepository skillMapRepository, UserService userService) {
        this.skillMapRepository = skillMapRepository;
        this.userService = userService;
    }

    public List<SkillMap> getSkillMaps() {
        // TODO: Modifiy so that it only gives back a lits of 
        // skillmaps I am a member of once the member functionality 
        // is implemented currently returns all SkillMaps ever created 
        // no matter if public or not (comment by Anna)
        return this.skillMapRepository.findAll();
    }

    private void checkIsOwner(SkillMap map, User requester) {
        if (!map.getOwnerId().equals(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can perform this action.");
        }
    }

    public SkillMap createSkillMap(SkillMap newSkillMap, String token) {
        User owner = userService.getUserByToken(token);
        // REST Spec Endpoint: 202.2 
        // Invalid or incomplete skillmap object
        if (newSkillMap.getTitle() == null || newSkillMap.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required.");
        }
        if (newSkillMap.getIsPublic() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "isPublic is required.");
        }
        if (newSkillMap.getNumberOfLevels() == null || newSkillMap.getNumberOfLevels() <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "numberOfLevels must be at least 1.");
        }
        newSkillMap.setOwnerId(owner.getId()); // statt setOwner()
        newSkillMap.setInviteCode(generateInviteCode());
        newSkillMap = skillMapRepository.save(newSkillMap);
        skillMapRepository.flush();
        log.debug("Created SkillMap: {}", newSkillMap);
        return newSkillMap;
    }
    //TODO: nicht SkillMap updates sondern ein DTO SkillMapPUtDTO definieren (comment by Anna)
    public SkillMap updateSkillMap(Long skillMapId, SkillMap updates, String token) {
        User requester = userService.getUserByToken(token);
        SkillMap existing = getSkillMapById(skillMapId, token); // 404 wenn nicht gefunden
        checkIsOwner(existing, requester); // 403 wenn nicht Owner

        // nur diese Felder dürfen geändert werden
        if (updates.getTitle() != null) existing.setTitle(updates.getTitle());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getIsPublic() != null) existing.setIsPublic(updates.getIsPublic());
        if (updates.getNumberOfLevels() != null) existing.setNumberOfLevels(updates.getNumberOfLevels());

        existing = skillMapRepository.save(existing);
        skillMapRepository.flush();
        log.debug("Updated SkillMap: {}", existing);
        return existing;
    }
    //muss void zurückgeben, ist falsch im Class diagram
	public void deleteSkillMap(SkillMap deletedSkillMap, String token) {
        User requester = userService.getUserByToken(token);
        checkIsOwner(deletedSkillMap, requester);
		skillMapRepository.deleteById(deletedSkillMap.getId());
		skillMapRepository.flush();
	
		log.debug("Deleted SkillMap: {}", deletedSkillMap);
	}

    public SkillMap getSkillMapById(Long skillMapId, String token) {
        User requester = userService.getUserByToken(token);

        Optional<SkillMap> requestedSkillMap = skillMapRepository.findById(skillMapId);

        // 203.3
        if (!requestedSkillMap.isPresent()) {
            log.debug("SkillMap with ID could not be found: {}", skillMapId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("The SkillMap with the ID %s could not be found.", skillMapId));
        }

        SkillMap map = requestedSkillMap.get();

        // 203.2
        //TODO: as soon as membership is implemented one can change this one to be also allowed when 
        // subscribed to a skill map (comment by Anna)
        if (!map.getIsPublic() && !map.getOwnerId().equals(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no access to this private skill map.");
        }

        return map;
    }

    //Comment Anna: this needs to be outside the method in order that a seed is set
    SecureRandom random = new SecureRandom();
    private String generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            code = sb.toString();
        } while (skillMapRepository.existsByInviteCode(code)); // brauchst du in deinem Repo
        return code;
    }
}
