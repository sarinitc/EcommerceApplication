package org.example.ecommerceapplication.orders.controller;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.example.ecommerceapplication.orders.dto.request.CheckoutRequest;
import org.example.ecommerceapplication.orders.dto.request.UpdateOrderStatusRequest;
import org.example.ecommerceapplication.orders.dto.response.OrderResponse;
import org.example.ecommerceapplication.orders.service.OrderService;
import org.example.ecommerceapplication.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CheckoutRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();

        OrderResponse orderResponse =
                orderService.createOrder(
                        email,
                        request
                );
        ApiResponse<OrderResponse> response =
                ApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order created successfully")
                        .status(HttpStatus.CREATED.value())
                        .payload(orderResponse)
                        .timestamp(Instant.now())
                        .build();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {

        List<OrderResponse> orders =
                orderService.getAllOrders();

        ApiResponse<List<OrderResponse>> response =
                ApiResponse.<List<OrderResponse>>builder()
                        .success(true)
                        .message("Orders retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .payload(orders)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }
    @GetMapping("/{orderId:\\d+}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long orderId
    ) {

        OrderResponse order =
                orderService.getOrderById(orderId);

        ApiResponse<OrderResponse> response =
                ApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .payload(order)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        OrderResponse order = orderService.updateOrderStatus(
                orderId,
                request.getOrderStatus()
        );

        ApiResponse<OrderResponse> response = ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order status updated successfully")
                .status(HttpStatus.OK.value())
                .payload(order)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(
            @PathVariable Long orderId
    ) {

        orderService.deleteOrderById(orderId);

        ApiResponse<Void> response =
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Order deleted successfully")
                        .status(HttpStatus.OK.value())
                        .payload(null)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }
    @GetMapping("/my-orders")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            Authentication authentication
    ) {

        // Get logged-in user's email from JWT
        String email = authentication.getName();


        // Get this user's orders
        List<OrderResponse> orders =
                orderService.getMyOrders(email);


        ApiResponse<List<OrderResponse>> response =
                ApiResponse.<List<OrderResponse>>builder()
                        .success(true)
                        .message("My orders retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .payload(orders)
                        .timestamp(Instant.now())
                        .build();


        return ResponseEntity.ok(response);
    }

}
