package Funding.Startreum.domain.admin;

import Funding.Startreum.domain.project.Project;
import Funding.Startreum.domain.project.ProjectRepository;
import Funding.Startreum.domain.project.ProjectSearchDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/projects")
public class AdminProjectApiController {

    private final ProjectRepository projectRepository;

    public AdminProjectApiController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * 🔹 프로젝트 목록 조회 (is_approved 상태 필터링 가능)
     * - `status` 파라미터를 전달하면 특정 상태만 조회
     */
    @GetMapping
    public ResponseEntity<List<ProjectSearchDto>> getProjectsByApprovalStatus(
            @RequestParam(required = false) String status
    ) {
        List<Project> projects;

        // 상태 필터링 적용
        if (status != null && !status.isBlank()) {
            try {
                Project.ApprovalStatus approvalStatus = Project.ApprovalStatus.valueOf(status.toUpperCase());
                projects = projectRepository.findByIsApproved(approvalStatus);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            projects = projectRepository.findAll(); // 전체 조회
        }

        List<ProjectSearchDto> projectDtos = projects.stream()
                .map(ProjectSearchDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(projectDtos);
    }
}
