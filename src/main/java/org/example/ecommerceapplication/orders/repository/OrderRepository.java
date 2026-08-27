package org.example.ecommerceapplication.orders.repository;

import org.example.ecommerceapplication.orders.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository
        extends JpaRepository<Order, Long> {
    List<Order> findAllByOrderByOrderIdDesc();
    // Customer: get only their own orders
    List<Order> findByUserEmailOrderByOrderIdDesc(
            String email
    );
}