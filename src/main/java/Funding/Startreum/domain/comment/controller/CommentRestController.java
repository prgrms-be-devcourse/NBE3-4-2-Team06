package Funding.Startreum.domain.comment.controller;

import Funding.Startreum.domain.comment.dto.response.CommentResponse;
import Funding.Startreum.domain.comment.entity.Comment;
import Funding.Startreum.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentRestController {
    private final CommentService service;

    @GetMapping("/{projectId}")
    public ResponseEntity<?> getComment(
            @PathVariable("projectId") int projectId) {

        List<CommentResponse> comments = service.getComment(projectId);

        if(comments.isEmpty()) {
            return ResponseEntity.ok(
                    Map.of("status", "success",
                            "message", "댓글이 없습니다.",
                            "data", comments
                    )
            );
        } else {
            return ResponseEntity.ok(
                    Map.of("status", "success",
                            "message", "댓글 조회에 성공했습니다.",
                            "data", comments
                    )
            );
        }

    }
}
