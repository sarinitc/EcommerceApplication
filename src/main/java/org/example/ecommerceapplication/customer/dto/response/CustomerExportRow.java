package org.example.ecommerceapplication.customer.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerExportRow {
    private Long customerId;
    private String name;

    private String email;

    private String phoneNumber;

    private String accountStatus;

    private boolean verified;

    private String location;

    private long orderCount;

    private BigDecimal totalSpent;

    private LocalDate lastOrderDate;
}