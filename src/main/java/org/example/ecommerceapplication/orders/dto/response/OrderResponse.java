package org.example.ecommerceapplication.orders.dto.response;

import lombok.*;
import org.example.ecommerceapplication.orders.entity.OrderStatus;
import org.example.ecommerceapplication.payment.dto.reponse.PaymentResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long orderId;

    private String email;

    private LocalDate orderDate;

    private OrderStatus orderStatus;

    private BigDecimal totalAmount;

    private PaymentResponse payment;

    private List<OrderItemResponse> items;
}
