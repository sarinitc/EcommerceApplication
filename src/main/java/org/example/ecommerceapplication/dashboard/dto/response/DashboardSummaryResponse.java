package org.example.ecommerceapplication.dashboard.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private long totalProducts;

    private long totalCustomers;

    private long totalOrders;

    private long pendingOrders;
}