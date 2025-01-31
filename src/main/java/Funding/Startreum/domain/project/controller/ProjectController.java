package Funding.Startreum.domain.project.controller;

import Funding.Startreum.domain.project.dto.ProjectCreateRequestDto;
import Funding.Startreum.domain.project.dto.ProjectCreateResponseDto;
import Funding.Startreum.domain.project.entity.Project;
import Funding.Startreum.domain.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/api/beneficiary/create")
    public ResponseEntity<?> createProject(@AuthenticationPrincipal Integer userId, @Valid @RequestBody ProjectCreateRequestDto projectCreateRequestDto) {
        ProjectCreateResponseDto pcrd = projectService.createProject(userId, projectCreateRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }
}
