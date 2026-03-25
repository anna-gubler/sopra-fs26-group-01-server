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
        return this.skillMapRepository.findAll();
    }

    private void checkIsOwner(SkillMap map, User requester) {
        if (!map.getOwnerId().equals(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can perform this action.");
        }
    }

    public SkillMap createSkillMap(SkillMap newSkillMap, String token) {
        User owner = userService.getUserByToken(token);
        newSkillMap.setOwnerId(owner.getId()); // statt setOwner()
        newSkillMap.setInviteCode(generateInviteCode());
        newSkillMap = skillMapRepository.save(newSkillMap);
        skillMapRepository.flush();
        log.debug("Created SkillMap: {}", newSkillMap);
        return newSkillMap;
    }
    //TODO: nicht SkillMap updates sondern ein DTO SkillMapPUtDTO definieren:
    public SkillMap updateSkillMap(Long skillMapId, SkillMap updates, String token) {
        User requester = userService.getUserByToken(token);
        SkillMap existing = getSkillMapById(skillMapId); // 404 wenn nicht gefunden
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

	public SkillMap getSkillMapById(Long skillMapId) {
		Optional<SkillMap> requestedSkillMap = skillMapRepository.findById(skillMapId);

		if (!requestedSkillMap.isPresent()) {
			log.debug("SkillMap with ID could not be found by ID and 404 called: {}", skillMapId, requestedSkillMap);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					String.format("The SkillMap with the ID %s could not be found.", skillMapId));
		} else {
			log.debug("SkillMap found by ID and returned: {}", requestedSkillMap);	
			return requestedSkillMap.get();
		}
	}

    //this needs to be outside the method in order that a seed is set
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
