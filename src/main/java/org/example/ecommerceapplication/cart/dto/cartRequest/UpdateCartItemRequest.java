package org.example.ecommerceapplication.cart.dto.cartRequest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateCartItemRequest {

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be greater than zero")
    private Integer quantity;
}
