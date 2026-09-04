package org.example.ecommerceapplication.dashboard.service;


import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.dashboard.dto.response.*;
import org.example.ecommerceapplication.dashboard.projection.*;
import org.example.ecommerceapplication.orders.entity.Order;
import org.example.ecommerceapplication.orders.entity.OrderStatus;
import org.example.ecommerceapplication.orders.repository.OrderRepository;
import org.example.ecommerceapplication.orders.repository.OrderItemRepository;
import org.example.ecommerceapplication.product.repository.ProductRepository;
import org.example.ecommerceapplication.user.repository.UserRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;


    @Transactional(readOnly = true)
    public DashboardOverviewResponse getOverview(
            LocalDate from,
            LocalDate to
    ) {

        // Default:
        // first day of current month -> today

        if (to == null) {
            to = LocalDate.now();
        }

        if (from == null) {
            from = to.withDayOfMonth(1);
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "'from' date cannot be after 'to' date"
            );
        }


        // ==============================
        // SUMMARY
        // ==============================

        long totalProducts =
                productRepository.countByDeletedFalse();

        long totalCustomers =
                userRepository.countUsersByRole(
                        "CUSTOMER"
                );

        long totalOrders =
                orderRepository.countByOrderDateBetween(
                        from,
                        to
                );

        long pendingOrders =
                orderRepository
                        .countByOrderStatusAndOrderDateBetween(
                                OrderStatus.PENDING,
                                from,
                                to
                        );
        DashboardSummaryResponse summary =
                DashboardSummaryResponse.builder()
                        .totalProducts(totalProducts)
                        .totalCustomers(totalCustomers)
                        .totalOrders(totalOrders)
                        .pendingOrders(pendingOrders)
                        .build();


        // ==============================
        // SALES
        // ==============================

        List<SalesOverviewResponse> sales =
                orderRepository
                        .findSalesOverview(from, to)
                        .stream()
                        .map(item ->
                                SalesOverviewResponse.builder()
                                        .date(item.getDate())
                                        .orders(item.getOrders())
                                        .revenue(item.getRevenue())
                                        .build()
                        )
                        .toList();


        // ==============================
        // ORDERS BY STATUS
        // ==============================

        List<OrderStatusResponse> ordersByStatus =
                orderRepository
                        .findOrderCountsByStatus(from, to)
                        .stream()
                        .map(item ->
                                OrderStatusResponse.builder()
                                        .status(item.getStatus().name())
                                        .count(item.getCount())
                                        .build()
                        )
                        .toList();


        // ==============================
        // RECENT ORDERS
        // ==============================

        List<Order> recentOrderEntities =
                orderRepository
                        .findByOrderDateBetweenOrderByOrderIdDesc(
                                from,
                                to,
                                PageRequest.of(0, 5)
                        );


        List<RecentOrderResponse> recentOrders =
                recentOrderEntities
                        .stream()
                        .map(order ->
                                RecentOrderResponse.builder()
                                        .orderId(
                                                order.getOrderId()
                                        )
                                        .customerName(
                                                order.getUser()
                                                        .getUsername()
                                        )
                                        .email(
                                                order.getEmail()
                                        )
                                        .orderDate(
                                                order.getOrderDate()
                                        )
                                        .total(
                                                order.getTotalAmount()
                                        )
                                        .status(order.getOrderStatus().name())
                                        .build()
                        )
                        .toList();
        // ==============================
        // TOP PRODUCTS
        // ==============================

        List<TopProductResponse> topProducts =
                orderItemRepository
                        .findTopSellingProducts(
                                from,
                                to,
                                PageRequest.of(0, 5)
                        )
                        .stream()
                        .map(item ->
                                TopProductResponse.builder()
                                        .productId(
                                                item.getProductId()
                                        )
                                        .productName(
                                                item.getProductName()
                                        )
                                        .image(
                                                item.getImage()
                                        )
                                        .unitsSold(
                                                item.getUnitsSold()
                                        )
                                        .build()
                        )
                        .toList();


        // ==============================
        // FINAL RESPONSE
        // ==============================

        return DashboardOverviewResponse.builder()
                .summary(summary)
                .sales(sales)
                .ordersByStatus(ordersByStatus)
                .recentOrders(recentOrders)
                .topProducts(topProducts)
                .build();
    }
}
