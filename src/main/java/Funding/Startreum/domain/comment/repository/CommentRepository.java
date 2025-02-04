package Funding.Startreum.domain.comment.repository;

import Funding.Startreum.domain.comment.entity.Comment;
import Funding.Startreum.domain.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByCommentId(Integer commentId);
    List<Comment> findByProject_ProjectId(int projectId);
}
