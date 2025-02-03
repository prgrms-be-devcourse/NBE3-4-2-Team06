package Funding.Startreum.domain.project.controller;

import Funding.Startreum.domain.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/beneficiary")
public class ProjectController {

    private final ProjectService projectService;

    @DeleteMapping("/delete/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable Integer projectId, @RequestHeader("Authorization") String token) {
        projectService.deleteProject(projectId, token);
        return ResponseEntity.noContent().build();
    }
}
