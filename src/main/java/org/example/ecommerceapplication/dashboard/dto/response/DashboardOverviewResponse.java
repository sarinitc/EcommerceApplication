package org.example.ecommerceapplication.dashboard.dto.response;
import lombok.*;
import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewResponse {

    private DashboardSummaryResponse summary;

    private List<SalesOverviewResponse> sales;

    private List<OrderStatusResponse> ordersByStatus;

    private List<RecentOrderResponse> recentOrders;

    private List<TopProductResponse> topProducts;
}