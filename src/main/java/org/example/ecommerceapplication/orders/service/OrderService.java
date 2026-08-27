package org.example.ecommerceapplication.orders.service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.cart.entity.Cart;
import org.example.ecommerceapplication.cart.repository.CartRepository;
import org.example.ecommerceapplication.orders.dto.request.CheckoutRequest;
import org.example.ecommerceapplication.orders.dto.response.OrderItemResponse;
import org.example.ecommerceapplication.orders.dto.response.OrderResponse;
import org.example.ecommerceapplication.orders.dto.response.ProductSummaryResponse;
import org.example.ecommerceapplication.orders.entity.Order;
import org.example.ecommerceapplication.orders.entity.OrderItem;
import org.example.ecommerceapplication.orders.exception.OrderNotFoundException;
import org.example.ecommerceapplication.orders.entity.OrderStatus;
import org.example.ecommerceapplication.orders.repository.OrderRepository;
import org.example.ecommerceapplication.payment.dto.reponse.PaymentResponse;
import org.example.ecommerceapplication.payment.entity.Payment;
import org.example.ecommerceapplication.payment.repository.PaymentRepository;
import org.example.ecommerceapplication.user.entity.User;
import org.example.ecommerceapplication.user.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;


    // =========================================================
    // CREATE ORDER / CHECKOUT
    // =========================================================

    @Transactional
    public OrderResponse createOrder(
            String email,
            CheckoutRequest request
    ) {

        // -----------------------------------------------------
        // 1. Find logged-in user
        // -----------------------------------------------------

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );


        // -----------------------------------------------------
        // 2. Find cart
        // -----------------------------------------------------

        Cart cart = cartRepository
                .findById(request.getCartId())
                .orElseThrow(() ->
                        new RuntimeException("Cart not found")
                );


        // -----------------------------------------------------
        // 3. Check cart ownership
        // -----------------------------------------------------

        if (!cart.getUser().getId().equals(user.getId())) {
            throw new RuntimeException(
                    "You do not own this cart"
            );
        }


        // -----------------------------------------------------
        // 4. Check cart has products
        // -----------------------------------------------------

        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException(
                    "Cart is empty"
            );
        }


        // -----------------------------------------------------
        // 5. Find selected payment method
        // -----------------------------------------------------

        Payment payment = paymentRepository
                .findById(request.getPaymentId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Payment method not found"
                        )
                );


        // -----------------------------------------------------
        // 6. Create Order
        // -----------------------------------------------------

        Order order = Order.builder()
                .email(user.getEmail())
                .orderDate(LocalDate.now())
                .orderStatus(OrderStatus.PENDING)
                .totalAmount(cart.getTotalPrice())
                .user(user)
                .payment(payment)
                .build();


        // -----------------------------------------------------
        // 7. Convert CartItems into OrderItems
        // -----------------------------------------------------

        List<OrderItem> orderItems = new ArrayList<>();

        cart.getCartItems().forEach(cartItem -> {

            OrderItem orderItem = OrderItem.builder()

                    .quantity(
                            cartItem.getQuantity()
                    )

                    .discount(
                            cartItem.getDiscount()
                    )

                    .orderedProductPrice(
                            cartItem.getProductPrice()
                    )

                    .product(
                            cartItem.getProduct()
                    )

                    .order(order)

                    .build();

            orderItems.add(orderItem);
        });


        // -----------------------------------------------------
        // 8. Put OrderItems inside Order
        // -----------------------------------------------------

        order.setOrderItems(orderItems);


        // -----------------------------------------------------
        // 9. Save Order
        // Cascade also saves OrderItems
        // -----------------------------------------------------

        Order savedOrder =
                orderRepository.save(order);


        // -----------------------------------------------------
        // 10. Clear cart after successful checkout
        // -----------------------------------------------------

        cart.getCartItems().clear();

        cart.setTotalPrice(
                BigDecimal.ZERO
        );

        cartRepository.save(cart);


        // -----------------------------------------------------
        // 11. Convert saved Order -> OrderResponse
        // -----------------------------------------------------

        return mapToOrderResponse(savedOrder);
    }


    // =========================================================
    // ENTITY -> RESPONSE DTO
    // =========================================================

    private OrderResponse mapToOrderResponse(
            Order order
    ) {

        // -----------------------------------------------------
        // Convert OrderItems -> OrderItemResponse
        // -----------------------------------------------------

        List<OrderItemResponse> items =
                order.getOrderItems()
                        .stream()
                        .map(item ->

                                OrderItemResponse.builder()

                                        .orderItemId(
                                                item.getOrderItemId()
                                        )

                                        .discount(
                                                item.getDiscount()
                                        )

                                        .orderedProductPrice(
                                                item.getOrderedProductPrice()
                                        )

                                        .quantity(
                                                item.getQuantity()
                                        )

                                        .product(
                                                ProductSummaryResponse
                                                        .builder()

                                                        .productId(
                                                                item.getProduct()
                                                                        .getProductId()
                                                        )

                                                        .productName(
                                                                item.getProduct()
                                                                        .getProductName()
                                                        )

                                                        .image(
                                                                item.getProduct()
                                                                        .getImage()
                                                        )

                                                        .build()
                                        )

                                        .build()
                        )
                        .toList();


        // -----------------------------------------------------
        // Convert Payment -> PaymentResponse
        // -----------------------------------------------------

        PaymentResponse paymentResponse =
                PaymentResponse.builder()

                        .paymentId(
                                order.getPayment()
                                        .getPaymentId()
                        )

                        .paymentMethod(
                                order.getPayment()
                                        .getPaymentMethod()
                        )

                        .build();


        // -----------------------------------------------------
        // Convert Order -> OrderResponse
        // -----------------------------------------------------

        return OrderResponse.builder()

                .orderId(
                        order.getOrderId()
                )

                .email(
                        order.getEmail()
                )

                .orderDate(
                        order.getOrderDate()
                )

                .orderStatus(
                        order.getOrderStatus()
                )

                .totalAmount(
                        order.getTotalAmount()
                )

                .payment(
                        paymentResponse
                )

                .items(
                        items
                )

                .build();
    }
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {

        List<Order> orders = orderRepository.findAll(
                Sort.by(
                        Sort.Direction.DESC,
                        "orderId"
                )
        );

        return orders.stream()
                .map(this::mapToOrderResponse)
                .toList();
    }
    @Transactional
    public OrderResponse getOrderById(Long orderId) {

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(orderId)
                );

        return mapToOrderResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus orderStatus) {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.setOrderStatus(orderStatus);

        return mapToOrderResponse(order);
    }

    @Transactional
    public void deleteOrderById(Long orderId) {

        // 1. Find order
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(orderId)
                );

        // 2. Delete order
        orderRepository.delete(order);
    }
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(
            String email
    ) {

        // 1. Find logged-in user
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );


        // 2. Get only orders belonging to this user
        List<Order> orders =
                orderRepository
                        .findByUserEmailOrderByOrderIdDesc(
                                user.getEmail()
                        );


        // 3. Convert Order entities -> OrderResponse DTOs
        return orders.stream()
                .map(this::mapToOrderResponse)
                .toList();
    }
}
