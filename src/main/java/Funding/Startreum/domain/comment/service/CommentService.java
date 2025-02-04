package Funding.Startreum.domain.comment.service;

import Funding.Startreum.domain.comment.dto.response.CommentResponse;
import Funding.Startreum.domain.comment.entity.Comment;
import Funding.Startreum.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    final private CommentRepository repository;

    @Transactional(readOnly = true)
    public List<CommentResponse> getComment(int projectId) {
        List<Comment> comments = repository.findByProject_ProjectId(projectId);

        return comments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private CommentResponse mapToDto(Comment comment) {
        return new CommentResponse(
                comment.getCommentId(),
                comment.getProject().getProjectId(),
                comment.getUser().getUserId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
