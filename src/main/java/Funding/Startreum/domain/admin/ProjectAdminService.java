package Funding.Startreum.domain.admin;

import Funding.Startreum.domain.project.entity.Project;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectAdminService {

    private final ProjectAdminRepository projectAdminRepository;
    private final EntityManager entityManager;

    public ProjectAdminService(ProjectAdminRepository projectAdminRepository, EntityManager entityManager) {
        this.projectAdminRepository = projectAdminRepository;
        this.entityManager = entityManager;
    }

    /**
     * 🔹 프로젝트 승인 상태 변경
     */
    @Transactional
    public void updateApprovalStatus(Integer projectId, Project.ApprovalStatus isApproved) {
        System.out.println("🟠 updateApprovalStatus() 실행됨 - projectId: " + projectId + ", isApproved: " + isApproved);

        int updatedRows = projectAdminRepository.updateApprovalStatus(projectId, isApproved);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("❌ 해당 프로젝트가 존재하지 않습니다.");
        }

        entityManager.flush(); // 변경 사항 즉시 적용

        // 디버깅: 실제 저장된 isApproved 값 확인
        Project project = projectAdminRepository.findById(projectId).orElseThrow();
        System.out.println("🟠 DB 저장 후 isApproved 값: " + project.getIsApproved());

        if (project.getIsApproved().toString().equals("REJECTED")) {
            System.out.println("🟢 프로젝트 승인 거절 -> isDeleted 변경 실행");
            updateIsDeletedTransaction(projectId, true);
        }
    }

    /**
     * 🔹 프로젝트 진행 상태 변경
     */
    @Transactional
    public void updateProjectStatus(Integer projectId, Project.Status status) {
        System.out.println("🟠 updateProjectStatus() 실행됨 - projectId: " + projectId + ", status: " + status);

        int updatedRows = projectAdminRepository.updateProjectStatus(projectId, status);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("❌ 해당 프로젝트가 존재하지 않습니다.");
        }

        entityManager.flush(); // 변경 사항 즉시 적용

        // 디버깅: 실제 저장된 status 값 확인
        Project project = projectAdminRepository.findById(projectId).orElseThrow();
        System.out.println("🟠 DB 저장 후 status 값: " + project.getStatus());

        if (project.getStatus().toString().equals("FAILED")) {
            System.out.println("🟢 프로젝트 실패 -> isDeleted 변경 실행");
            updateIsDeletedTransaction(projectId, true);
        }
    }

    /**
     * 🔹 `isDeleted` 값을 변경하는 트랜잭션
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateIsDeletedTransaction(Integer projectId, Boolean isDeleted) {
        int deletedRows = projectAdminRepository.updateIsDeleted(projectId, isDeleted);
        entityManager.flush();
        System.out.println("🟠 업데이트 후 isDeleted 값 확인");
        Project projectAfterUpdate = projectAdminRepository.findById(projectId).orElseThrow();
        System.out.println("🟠 업데이트 후 isDeleted 값: " + projectAfterUpdate.getIsDeleted());
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