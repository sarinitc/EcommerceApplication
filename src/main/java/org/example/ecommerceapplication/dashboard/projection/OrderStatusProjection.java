package org.example.ecommerceapplication.dashboard.projection;

import org.example.ecommerceapplication.orders.entity.OrderStatus;

public interface OrderStatusProjection {

    OrderStatus getStatus();

    long getCount();
}
