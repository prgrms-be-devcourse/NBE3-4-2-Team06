package Funding.Startreum.domain.comment.dto.response;

import java.time.LocalDateTime;

public record CommentResponse(
        int commentId,
        int projectId,
        int userId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
