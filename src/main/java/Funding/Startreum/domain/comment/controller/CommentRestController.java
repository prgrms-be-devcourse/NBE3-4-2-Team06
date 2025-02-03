package Funding.Startreum.domain.comment.controller;

import Funding.Startreum.domain.comment.dto.request.CommentCreateRequest;
import Funding.Startreum.domain.comment.dto.response.CommentResponse;
import Funding.Startreum.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentRestController {
    private final CommentService service;

    @PostMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createComment(
            @PathVariable("projectId") int projectId,
            @Valid @RequestBody CommentCreateRequest request,
            UserDetails userDetails
    ) {
        CommentResponse response = service.createComment(projectId, request, userDetails.getUsername());

        return ResponseEntity.ok(
                Map.of("status", "success",
                        "message", "댓글 생성에 성공했습니다.",
                        "data", response
                )
        );
    }

}
