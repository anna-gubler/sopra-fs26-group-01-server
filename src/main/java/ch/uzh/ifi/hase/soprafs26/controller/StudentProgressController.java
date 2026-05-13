package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.StudentProgress;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.StudentProgressDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.StudentProgressPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.StudentProgressService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class StudentProgressController {

    private final StudentProgressService studentProgressService;

    public StudentProgressController(StudentProgressService studentProgressService) {
        this.studentProgressService = studentProgressService;
    }

    @PutMapping("/skills/{skillId}/progress/me")
    @ResponseStatus(HttpStatus.OK)
    public StudentProgressDTO setProgress(@PathVariable Long skillId,
                                          @RequestBody StudentProgressPostDTO body,
                                          HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        StudentProgress progress = studentProgressService.setProgress(skillId, user, body.getIsUnderstood());
        return DTOMapper.INSTANCE.convertStudentProgressToDTO(progress);
    }
}
