package org.example.ecommerceapplication.orders.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long orderId) {
        super("Order not found with id: " + orderId);
    }
}
