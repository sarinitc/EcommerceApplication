package org.example.ecommerceapplication.dashboard.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusResponse {

    private String status;

    private long count;
}