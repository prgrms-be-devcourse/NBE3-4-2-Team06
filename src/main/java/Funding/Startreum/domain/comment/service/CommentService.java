package Funding.Startreum.domain.comment.service;

import Funding.Startreum.domain.comment.entity.Comment;
import Funding.Startreum.domain.comment.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    final private CommentRepository repository;

    @Transactional
    public void deleteComment(int commentId, String username) {
        Comment comment = repository.findByCommentId(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다 : " + commentId));

        if (!comment.getUser().getName().equals(username)) {
            throw new AccessDeniedException("댓글 삭제 권한이 없습니다.");
        }

        repository.delete(comment);
    }

}
