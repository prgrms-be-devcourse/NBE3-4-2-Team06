package Funding.Startreum.domain.admin;

import Funding.Startreum.domain.project.Project;
import Funding.Startreum.domain.project.Project.ApprovalStatus;
import Funding.Startreum.domain.project.Project.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProjectAdminRepository extends JpaRepository<Project, Integer> {

    /**
     * 🔹 특정 프로젝트의 승인 상태(isApproved) 변경
     */
    @Transactional
    @Modifying
    @Query("UPDATE Project p SET p.isApproved = :isApproved WHERE p.projectId = :projectId")
    int updateApprovalStatus(Integer projectId, ApprovalStatus isApproved);

    /**
     * 🔹 특정 프로젝트의 진행 상태(status) 변경
     */
    @Transactional
    @Modifying
    @Query("UPDATE Project p SET p.status = :status WHERE p.projectId = :projectId")
    int updateProjectStatus(Integer projectId, Status status);

    /**
     * 🔹 승인 상태(isApproved)로 프로젝트 목록 조회 (관리자용)
     */
    List<Project> findByIsApproved(ApprovalStatus approvalStatus);
}
