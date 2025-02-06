package Funding.Startreum.domain.admin;

import Funding.Startreum.domain.project.entity.Project;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectAdminService {

    private final ProjectAdminRepository projectAdminRepository;

    public ProjectAdminService(ProjectAdminRepository projectAdminRepository) {
        this.projectAdminRepository = projectAdminRepository;
    }

    /**
     * 🔹 프로젝트 승인 상태 변경
     */
    @Transactional
    public void updateApprovalStatus(Integer projectId, Project.ApprovalStatus isApproved) {
        int updatedRows = projectAdminRepository.updateApprovalStatus(projectId, isApproved);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("❌ 해당 프로젝트가 존재하지 않습니다.");
        }

        // 승인 거절(REJECTED) 상태라면 isDeleted = true
        if (isApproved == Project.ApprovalStatus.REJECTED) {
            projectAdminRepository.updateIsDeleted(projectId, true);
        }
    }

    /**
     * 🔹 프로젝트 진행 상태 변경
     */
    @Transactional
    public void updateProjectStatus(Integer projectId, Project.Status status) {
        int updatedRows = projectAdminRepository.updateProjectStatus(projectId, status);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("❌ 해당 프로젝트가 존재하지 않습니다.");
        }

        // 진행 실패(FAILED) 상태라면 isDeleted = true
        if (status == Project.Status.FAILED) {
            projectAdminRepository.updateIsDeleted(projectId, true);
        }
    }

    /**
     * 🔹 관리자용 상태 변경 (둘 다 변경 가능)
     */
    @Transactional
    public void updateProject(Integer projectId, ProjectAdminUpdateDto updateDto) {
        if (updateDto.getIsApproved() != null) {
            updateApprovalStatus(projectId, updateDto.getIsApproved());
        }
        if (updateDto.getStatus() != null) {
            updateProjectStatus(projectId, updateDto.getStatus());
        }
    }
}