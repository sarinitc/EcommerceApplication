package org.example.ecommerceapplication.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.ecommerceapplication.orders.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerLastOrderResponse {

    private Long orderId;
    private LocalDate orderDate;
    private BigDecimal total;
    private OrderStatus status;
}
