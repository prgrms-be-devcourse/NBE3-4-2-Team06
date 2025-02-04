package Funding.Startreum.domain.comment.service;


import Funding.Startreum.domain.comment.dto.request.CommentCreateRequest;
import Funding.Startreum.domain.comment.dto.response.CommentResponse;
import Funding.Startreum.domain.comment.entity.Comment;
import Funding.Startreum.domain.comment.repository.CommentRepository;
import Funding.Startreum.domain.project.Project;
import Funding.Startreum.domain.project.ProjectRepository;
import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentService {

    final private UserRepository userRepository;
    final private ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public List<CommentResponse> getComment(int projectId) {
        List<Comment> comments = repository.findByProject_ProjectId(projectId);

        return comments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());     

    @Transactional
    public CommentResponse createComment(int projectId, CommentCreateRequest request, String username) {
        Comment comment = new Comment();

        User user = userRepository.findByName(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다 : " + username));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다 : " + projectId));

        comment.setProject(project);
        comment.setUser(user);
        comment.setContent(request.content());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        repository.save(comment);

        return mapToDto(comment);
    }
      
    @Transactional(readOnly = true)
    public List<CommentResponse> getComment(int projectId) {
        List<Comment> comments = repository.findByProject_ProjectId(projectId);

        return comments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());   

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
