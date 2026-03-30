package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs26.entity.SkillMap;
import ch.uzh.ifi.hase.soprafs26.entity.SkillMapMembership;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapMembershipGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkillMapPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPatchDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutAvatarDTO;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
@Mapper
public interface DTOMapper {

	DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

	@Mapping(source = "username", target = "username")
	@Mapping(source = "bio", target = "bio")
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "token", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "creationDate", ignore = true)
	@Mapping(source = "password", target = "password")
	@Mapping(target = "style", ignore = true)
	@Mapping(target = "seed", ignore = true)
	User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "status", target = "status")
	@Mapping(source = "bio", target = "bio")
	@Mapping(source = "creationDate", target = "creationDate")
	@Mapping(source = "token", target = "token", ignore = true)
	@Mapping(source = "seed", target = "seed")
	@Mapping(source = "style", target = "style")
	UserGetDTO convertEntityToUserGetDTO(User user);

	@Mapping(source = "username", target = "username")
	@Mapping(source = "bio", target = "bio")
	@Mapping(source = "id", target = "id")
	@Mapping(source = "token", target = "token")
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "creationDate", ignore = true)
	@Mapping(source = "password", target = "password")
	@Mapping(target = "style", ignore = true)
	@Mapping(target = "seed", ignore = true)
	User convertUserPutDTOtoEntity(UserPutDTO userPutDTO);

	@Mapping(source = "username", target = "username")
	@Mapping(source = "bio", target = "bio")
	@Mapping(source = "id", target = "id")
	@Mapping(source = "token", target = "token")
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "creationDate", ignore = true)
	@Mapping(source = "password", target = "password")
	@Mapping(target = "style", ignore = true)
	@Mapping(target = "seed", ignore = true)
	User convertUserPatchDTOtoEntity(UserPatchDTO userPatchDTO);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "token", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "creationDate", ignore = true)
	@Mapping(target = "username", ignore = true)
	@Mapping(target = "bio", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(source = "style", target = "style")
	@Mapping(source = "seed", target = "seed")
	User convertUserPutAvatarDTOtoEntity(UserPutAvatarDTO userPutAvatarDTO);
	//Skillmap Mappings
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "isPublic", target = "isPublic")
    @Mapping(source = "numberOfLevels", target = "numberOfLevels")
    SkillMap convertSkillMapPostDTOtoEntity(SkillMapPostDTO skillMapPostDTO);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "isPublic", target = "isPublic")
    @Mapping(source = "numberOfLevels", target = "numberOfLevels")
    SkillMap convertSkillMapPutDTOtoEntity(SkillMapPutDTO skillMapPutDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "isPublic", target = "isPublic")
    @Mapping(source = "inviteCode", target = "inviteCode")
    @Mapping(source = "numberOfLevels", target = "numberOfLevels")
    @Mapping(source = "ownerId", target = "ownerId")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    SkillMapGetDTO convertEntityToSkillMapGetDTO(SkillMap skillMap);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "skillMapId", target = "skillMapId")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "joinedAt", target = "joinedAt")
    SkillMapMembershipGetDTO convertEntityToSkillMapMembershipGetDTO(SkillMapMembership membership);
}

