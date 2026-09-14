package org.example.ecommerceapplication.comment.dto.response;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long productId,
        Long authorId,
        String content,
        LocalDateTime createdAt
) {
}
