package org.example.ecommerceapplication.dashboard.service;

import org.example.ecommerceapplication.dashboard.dto.response.DashboardOverviewResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class DashboardServiceIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

    @Test
    void retrievesAnOverviewForADateRange() {
        DashboardOverviewResponse overview = dashboardService.getOverview(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2026, 12, 31)
        );

        assertNotNull(overview);
        assertNotNull(overview.getSummary());
        assertNotNull(overview.getSales());
        assertNotNull(overview.getOrdersByStatus());
        assertNotNull(overview.getRecentOrders());
        assertNotNull(overview.getTopProducts());
    }
}
