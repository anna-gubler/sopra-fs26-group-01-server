package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs26.entity.StudentProgress;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.StudentProgressPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.StudentProgressGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.StudentProgressService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

@RestController
public class StudentProgressController {

    private final StudentProgressService studentProgressService;
    private final UserService userService;

    @Autowired
    public StudentProgressController(StudentProgressService studentProgressService, UserService userService) {
        this.studentProgressService = studentProgressService;
        this.userService = userService;
    }

    @PutMapping("/skills/{skillId}/progress/me")
    @ResponseStatus(HttpStatus.OK)
    public StudentProgressGetDTO updateProgress(
            @PathVariable Long skillId,
            @RequestBody StudentProgressPutDTO dto,
            @RequestHeader("Authorization") String token) {

        User user = userService.getUserByToken(token);
        StudentProgress progress = studentProgressService.updateProgress(skillId, dto.getIsUnderstood(), user);
        return DTOMapper.INSTANCE.convertEntityToStudentProgressGetDTO(progress);
    }
}