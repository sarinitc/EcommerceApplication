package org.example.ecommerceapplication.orders.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutRequest {

    @NotNull(message = "Cart ID is required")
    private Long cartId;

    @NotNull(message = "Payment ID is required")
    private Long paymentId;
}
