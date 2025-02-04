package Funding.Startreum.domain.comment.controller;

import Funding.Startreum.domain.comment.dto.request.CommentRequest;
import Funding.Startreum.domain.comment.dto.response.CommentResponse;
import Funding.Startreum.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createComment(
            @PathVariable("projectId") int projectId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CommentResponse response = service.createComment(projectId, request, userDetails.getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                    Map.of(
                        "status", "success",
                        "message", "댓글 생성에 성공했습니다.",
                        "data", response
                )
        );
    }
  
    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> modifyComment(
            @PathVariable("commentId") int commentId,
            @RequestBody @Valid CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
            ) {
        CommentResponse response = service.modifyComment(request, commentId, userDetails.getUsername());

        return ResponseEntity.ok(                
                    Map.of(
                        "status", "success",
                        "message", "댓글 에 성공했습니다.",
                        "data", response
                )
        );
    }       
                       
}
