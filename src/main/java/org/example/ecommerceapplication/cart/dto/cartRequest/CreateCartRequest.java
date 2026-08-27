package org.example.ecommerceapplication.cart.dto.cartRequest;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCartRequest {

    @NotNull(message = "userId is required")
    private Long userId;
}
