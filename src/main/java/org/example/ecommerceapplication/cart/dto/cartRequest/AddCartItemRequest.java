package org.example.ecommerceapplication.cart.dto.cartRequest;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddCartItemRequest {
    @NotNull(message = "cartId is required")
    private Long cartId;

    @NotNull(message = "productId is required")
    private Long productId;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be greater than zero")
    private Integer quantity;
}
