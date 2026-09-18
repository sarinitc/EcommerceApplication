package org.example.ecommerceapplication.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.comment.dto.request.CommentRequest;
import org.example.ecommerceapplication.comment.dto.response.CommentResponse;
import org.example.ecommerceapplication.comment.service.CommentService;
import org.example.ecommerceapplication.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @Valid @RequestBody CommentRequest request,
            Authentication authentication
    ) {
        CommentResponse comment = commentService.createComment(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true, "Comment created successfully", HttpStatus.CREATED.value(), comment, Instant.now()
        ));
    }
}
