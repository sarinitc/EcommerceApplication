package org.example.ecommerceapplication.dashboard.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentOrderResponse {

    private Long orderId;

    private String customerName;

    private String email;

    private LocalDate orderDate;

    private BigDecimal total;

    private String status;
}