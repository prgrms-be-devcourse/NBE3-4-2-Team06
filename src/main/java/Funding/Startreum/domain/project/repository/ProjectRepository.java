package Funding.Startreum.domain.project.repository;

<<<<<<< HEAD
import Funding.Startreum.domain.project.entity.Project;
=======
import Funding.Startreum.domain.project.Project;
>>>>>>> feature/be/project-delete
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
}
