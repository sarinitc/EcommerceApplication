package org.example.ecommerceapplication.promotion.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplyPromotionRequest {

    @NotBlank(message = "Promotion code is required")
    private String code;

    @NotNull(message = "Cart ID is required")
    private Long cartId;
}