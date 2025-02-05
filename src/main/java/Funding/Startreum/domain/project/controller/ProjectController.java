package Funding.Startreum.domain.project.controller;

import Funding.Startreum.common.util.JwtUtil;
import Funding.Startreum.domain.project.dto.ProjectCreateRequestDto;
import Funding.Startreum.domain.project.dto.ProjectCreateResponseDto;
import Funding.Startreum.domain.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create/projects")
    public ResponseEntity<ProjectCreateResponseDto> createProject(
            @RequestHeader("Authorization") String token,
            @RequestBody ProjectCreateRequestDto projectRequest) {

        // 1. "Bearer " 문자열 제거 후 JWT에서 email 추출
        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));

        // 2. 프로젝트 생성 서비스 호출
        ProjectCreateResponseDto response = projectService.createProject(projectRequest, email);

        return ResponseEntity.created(URI.create("/api/create/projects/" + response.projectId())).body(response);
    }
}
