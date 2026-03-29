package ch.uzh.ifi.hase.soprafs26.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.SkillMapRole;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMapMembership;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapMembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SkillMapRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapGraphDTO;

@Service
@Transactional
public class SkillMapService {
    private final Logger log = LoggerFactory.getLogger(SkillMapService.class);

    private final SkillMapRepository skillMapRepository;
    private final SkillMapMembershipRepository skillMapMembershipRepository;
    private final UserService userService;

    public SkillMapService(
            @Qualifier("skillMapRepository") SkillMapRepository skillMapRepository,
            @Qualifier("skillMapMembershipRepository") SkillMapMembershipRepository skillMapMembershipRepository,
            UserService userService) {
        this.skillMapRepository = skillMapRepository;
        this.skillMapMembershipRepository = skillMapMembershipRepository;
        this.userService = userService;
    }

    // 201 - returns only maps the requester is a member of (spec 201.1)
    public List<SkillMap> getSkillMaps(String token) {
        User requester = userService.checkToken(token);
        List<SkillMapMembership> memberships = skillMapMembershipRepository.findByUserId(requester.getId());
        List<Long> skillMapIds = memberships.stream()
                .map(SkillMapMembership::getSkillMapId)
                .collect(Collectors.toList());
        return skillMapRepository.findAllById(skillMapIds);
    }

    private void checkIsOwner(SkillMap map, User requester) {
        if (!map.getOwnerId().equals(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can perform this action.");
        }
    }

    // 202 - creates a skill map and auto-adds the creator as OWNER member
    public SkillMap createSkillMap(SkillMap newSkillMap, String token) {
        User owner = userService.checkToken(token);
        if (newSkillMap.getTitle() == null || newSkillMap.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required.");
        }
        if (newSkillMap.getIsPublic() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "isPublic is required.");
        }
        if (newSkillMap.getNumberOfLevels() == null || newSkillMap.getNumberOfLevels() <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "numberOfLevels must be at least 1.");
        }
        newSkillMap.setOwnerId(owner.getId());
        newSkillMap.setInviteCode(generateInviteCode());
        newSkillMap = skillMapRepository.save(newSkillMap);
        skillMapRepository.flush();

        SkillMapMembership ownerMembership = new SkillMapMembership();
        ownerMembership.setUserId(owner.getId());
        ownerMembership.setSkillMapId(newSkillMap.getId());
        ownerMembership.setRole(SkillMapRole.OWNER);
        skillMapMembershipRepository.save(ownerMembership);
        skillMapMembershipRepository.flush();

        log.debug("Created SkillMap: {}", newSkillMap);
        return newSkillMap;
    }

    // 203 - access control: public maps are open to all, private maps require membership
    public SkillMap getSkillMapById(Long skillMapId, String token) {
        User requester = userService.checkToken(token);
        Optional<SkillMap> requestedSkillMap = skillMapRepository.findById(skillMapId);

        if (!requestedSkillMap.isPresent()) {
            log.debug("SkillMap with ID could not be found: {}", skillMapId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("The SkillMap with the ID %s could not be found.", skillMapId));
        }

        SkillMap map = requestedSkillMap.get();
        boolean isMember = skillMapMembershipRepository.existsBySkillMapIdAndUserId(map.getId(), requester.getId());
        if (!map.getIsPublic() && !isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no access to this private skill map.");
        }

        return map;
    }

    // 204 - only the owner can update; partial update (only non-null fields are applied)
    public SkillMap updateSkillMap(Long skillMapId, SkillMap updates, String token) {
        User requester = userService.checkToken(token);
        SkillMap existing = getSkillMapById(skillMapId, token);
        checkIsOwner(existing, requester);

        if (updates.getTitle() != null) existing.setTitle(updates.getTitle());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getIsPublic() != null) existing.setIsPublic(updates.getIsPublic());
        if (updates.getNumberOfLevels() != null) existing.setNumberOfLevels(updates.getNumberOfLevels());

        existing = skillMapRepository.save(existing);
        skillMapRepository.flush();
        log.debug("Updated SkillMap: {}", existing);
        return existing;
    }

    // 205 - only the owner can delete; memberships are cleaned up first
    public void deleteSkillMap(Long skillMapId, String token) {
        User requester = userService.checkToken(token);
        Optional<SkillMap> mapOpt = skillMapRepository.findById(skillMapId);
        if (!mapOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("The SkillMap with the ID %s could not be found.", skillMapId));
        }
        SkillMap map = mapOpt.get();
        checkIsOwner(map, requester);

        List<SkillMapMembership> memberships = skillMapMembershipRepository.findBySkillMapId(skillMapId);
        skillMapMembershipRepository.deleteAll(memberships);
        skillMapMembershipRepository.flush();

        skillMapRepository.deleteById(skillMapId);
        skillMapRepository.flush();
        log.debug("Deleted SkillMap with id: {}", skillMapId);
    }

    // 207 - returns all members; access is checked via getSkillMapById
    public List<User> getMembers(Long skillMapId, String token) {
        SkillMap map = getSkillMapById(skillMapId, token);
        List<SkillMapMembership> memberships = skillMapMembershipRepository.findBySkillMapId(map.getId());
        List<User> members = new ArrayList<>();
        for (SkillMapMembership membership : memberships) {
            members.add(userService.getUserById(membership.getUserId()));
        }
        return members;
    }

    // 208 - owner or the affected member themselves can remove a membership (spec 208.2)
    public void removeMember(Long skillMapId, Long userId, String token) {
        User requester = userService.checkToken(token);
        Optional<SkillMap> mapOpt = skillMapRepository.findById(skillMapId);
        if (!mapOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("The SkillMap with the ID %s could not be found.", skillMapId));
        }
        SkillMap map = mapOpt.get();

        Optional<SkillMapMembership> membershipOpt = skillMapMembershipRepository.findBySkillMapIdAndUserId(skillMapId, userId);
        if (!membershipOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership or skillmap not found.");
        }

        boolean isOwner = map.getOwnerId().equals(requester.getId());
        boolean isSelf = requester.getId().equals(userId);
        if (!isOwner && !isSelf) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the owner or the affected member can delete this membership.");
        }

        skillMapMembershipRepository.deleteById(membershipOpt.get().getId());
        skillMapMembershipRepository.flush();
        log.debug("Removed member {} from SkillMap {}", userId, skillMapId);
    }

    // 209 - stub: returns skillmap metadata; skills/deps/progress added once those entities exist
    public SkillMapGraphDTO getSkillMapGraph(Long skillMapId, String token) {
        SkillMap map = getSkillMapById(skillMapId, token);
        SkillMapGraphDTO graph = new SkillMapGraphDTO();
        graph.setSkillMapId(map.getId());
        graph.setTitle(map.getTitle());
        return graph;
    }

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
        } while (skillMapRepository.existsByInviteCode(code));
        return code;
    }
}
