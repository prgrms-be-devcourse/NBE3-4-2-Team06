package Funding.Startreum.domain.project.controller;

import Funding.Startreum.domain.project.dto.ProjectApprovalResponseDto;
import Funding.Startreum.domain.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/beneficiary")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/requestApprove/{projectId}")
    @PreAuthorize("hasRole('BENEFICIARY')")
    public ResponseEntity<ProjectApprovalResponseDto> requestApprove(@PathVariable("projectId") Integer projectId, @RequestHeader("Authorization") String token) {
        ProjectApprovalResponseDto responseDto = projectService.requestApprove(projectId, token);

        return ResponseEntity.ok(responseDto);
    }
}
