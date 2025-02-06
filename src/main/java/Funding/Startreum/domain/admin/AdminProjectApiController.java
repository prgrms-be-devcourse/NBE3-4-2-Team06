package Funding.Startreum.domain.admin;

import Funding.Startreum.domain.project.entity.Project;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/projects")
public class AdminProjectApiController {

    private final ProjectAdminRepository projectAdminRepository;

    public AdminProjectApiController(ProjectAdminRepository projectAdminRepository) {
        this.projectAdminRepository = projectAdminRepository;
    }

    /**
     * 🔹 현재 로그인한 사용자의 권한 확인 API
     */
    @GetMapping("/check-admin")
    public ResponseEntity<String> checkAdminRole(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("❌ 사용자가 로그인되지 않았습니다.");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (isAdmin) {
            return ResponseEntity.ok("✅ 현재 사용자는 ROLE_ADMIN 권한을 가지고 있습니다.");
        } else {
            return ResponseEntity.status(403).body("❌ 현재 사용자는 ROLE_ADMIN이 아닙니다.");
        }
    }

    /**
     * 🔹 프로젝트 목록 조회 (is_approved 상태 필터링 가능)
     */
    @GetMapping
    public ResponseEntity<List<ProjectAdminSearchDto>> getProjectsByApprovalStatus(
            @RequestParam(required = false) String status,
            Authentication authentication
    ) {
        // 🔍 현재 로그인한 사용자의 권한 확인
        if (authentication == null || authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .noneMatch(role -> role.equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).body(null);
        }

        List<Project> projects;

        // 상태 필터링 적용
        if (status != null && !status.isBlank()) {
            try {
                Project.ApprovalStatus approvalStatus = Project.ApprovalStatus.valueOf(status.toUpperCase());
                projects = projectAdminRepository.findByIsApproved(approvalStatus);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            projects = projectAdminRepository.findAll(); // 전체 조회
        }

        // ✅ 관리자용 DTO로 변환하여 반환
        List<ProjectAdminSearchDto> projectDtos = projects.stream()
                .map(ProjectAdminSearchDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(projectDtos);
    }

    /**
     * 🔹 프로젝트 승인 및 진행 상태 변경 API
     */
    @PatchMapping("/{projectId}/update")
    public ResponseEntity<String> updateProjectStatus(
            @PathVariable Integer projectId,
            @RequestBody ProjectAdminUpdateDto updateDto,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .noneMatch(role -> role.equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).body("❌ 권한 없음");
        }

        if (updateDto.getIsApproved() != null) {
            projectAdminRepository.updateApprovalStatus(projectId, updateDto.getIsApproved());
        }

        if (updateDto.getStatus() != null) {
            projectAdminRepository.updateProjectStatus(projectId, updateDto.getStatus());
        }

        return ResponseEntity.ok("✅ 프로젝트 상태가 변경되었습니다.");
    }
}