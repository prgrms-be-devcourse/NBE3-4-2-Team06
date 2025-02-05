package Funding.Startreum.domain.project.repository;

import Funding.Startreum.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    List<Project> findByIsApproved(Project.ApprovalStatus approvalStatus);
}
