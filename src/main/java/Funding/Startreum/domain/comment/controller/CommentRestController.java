package Funding.Startreum.domain.comment.controller;

import Funding.Startreum.domain.comment.dto.request.CommentRequest;
import Funding.Startreum.domain.comment.dto.response.CommentResponse;
import Funding.Startreum.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentRestController {
    private final CommentService service;

    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> modifyComment(
            @PathVariable("commentId") int commentId,
            @RequestBody @Valid CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
            ) {
        CommentResponse response = service.modifyComment(request, commentId, userDetails.getUsername());

        return ResponseEntity.ok(
                Map.of("status", "success",
                        "message", "댓글 수정에 성공했습니다",
                        "data", response
                )
        );
    }
}
