package org.example.ecommerceapplication.orders.repository;

import org.example.ecommerceapplication.orders.entity.Order;
import org.example.ecommerceapplication.orders.entity.OrderStatus;
import org.example.ecommerceapplication.dashboard.projection.OrderStatusProjection;
import org.example.ecommerceapplication.dashboard.projection.SalesOverviewProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OrderRepository
        extends JpaRepository<Order, Long> {
    List<Order> findAllByOrderByOrderIdDesc();
    // Customer: get only their own orders
    List<Order> findByUserEmailOrderByOrderIdDesc(
            String email
    );
    long countByOrderDateBetween(
            LocalDate from,
            LocalDate to
    );

    long countByOrderStatusAndOrderDateBetween(
            OrderStatus orderStatus,
            LocalDate from,
            LocalDate to
    );

    @Query("""
            select o.orderDate as date,
                   count(o) as orders,
                   coalesce(sum(o.totalAmount), 0) as revenue
            from Order o
            where o.orderDate between :from and :to
            group by o.orderDate
            order by o.orderDate
            """)
    List<SalesOverviewProjection> findSalesOverview(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
            select o.orderStatus as status, count(o) as count
            from Order o
            where o.orderDate between :from and :to
            group by o.orderStatus
            order by o.orderStatus
            """)
    List<OrderStatusProjection> findOrderCountsByStatus(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    List<Order> findByOrderDateBetweenOrderByOrderIdDesc(
            LocalDate from,
            LocalDate to,
            org.springframework.data.domain.Pageable pageable
    );
}
