package org.example.ecommerceapplication.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderResponse {

    private Long orderId;

    private LocalDate orderDate;

    private BigDecimal totalAmount;

    private String status;
}
