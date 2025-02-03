package Funding.Startreum.domain.comment.controller;

import Funding.Startreum.domain.comment.service.CommentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentRestController {
    private final CommentService service;

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteComment(
            @PathVariable("commentId") int commentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            service.deleteComment(commentId, userDetails.getUsername());
            return ResponseEntity.ok(
                    Map.of(
                            "status", "success",
                            "message", "댓글 삭제에 성공했습니다.",
                            "code", HttpStatus.OK.value()
                    )
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "status", "error",
                                    "message", "댓글을 찾을 수 없습니다.",
                                    "code", HttpStatus.NOT_FOUND.value()
                            )
                    );
        }
    }
}
