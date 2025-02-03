package Funding.Startreum.domain.project.controller;


import Funding.Startreum.domain.project.Project;
import Funding.Startreum.domain.project.dto.ProjectUpdateRequestDto;
import Funding.Startreum.domain.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/beneficiary")
public class ProjectController {

    private final ProjectService projectService;

    @PutMapping("/modify/{projectId}")
    @PreAuthorize("hasRole('BENEFICIARY')") //수혜자만 수정 가능
    public ResponseEntity<?> modifyProject(@PathVariable Integer projectId, @RequestHeader("Authorization") String token, @RequestBody ProjectUpdateRequestDto projectUpdateRequestDto) {
        Project updatedProject = projectService.modifyProject(projectId, projectUpdateRequestDto, token);

        return ResponseEntity.ok(Map.of(
                "statusCode", 200,
                "message", "프로젝트 수정에 성공하였습니다.",
                "data", updatedProject
        ));
    }
}
