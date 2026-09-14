package org.example.ecommerceapplication.comment.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {
    @NotBlank
    @Size(max = 2000)
    private String content;

    @NotNull
    @Positive
    @JsonAlias("postId")
    private Long productId;
}
