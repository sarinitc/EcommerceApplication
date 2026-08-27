package org.example.ecommerceapplication.orders.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.ecommerceapplication.orders.entity.OrderStatus;

@Data
public class UpdateOrderStatusRequest {

    @NotNull(message = "Order status is required")
    private OrderStatus orderStatus;
}
