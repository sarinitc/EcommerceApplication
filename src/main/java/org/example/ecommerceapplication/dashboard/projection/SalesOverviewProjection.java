package org.example.ecommerceapplication.dashboard.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface SalesOverviewProjection {

    LocalDate getDate();

    long getOrders();

    BigDecimal getRevenue();
}
