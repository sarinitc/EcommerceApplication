package org.example.ecommerceapplication.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCustomerResponse {

    private Long customerId;

    private String username;

    private String email;

    private String phoneNumber;

    private String profileImage;

    private boolean verified;

    private String accountStatus;

    private CustomerLocationResponse location;

    private long orderCount;

    private BigDecimal totalSpent;

    private CustomerLastOrderResponse lastOrder;
}
