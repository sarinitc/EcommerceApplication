package org.example.ecommerceapplication.orders.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.ecommerceapplication.payment.entity.Payment;
import org.example.ecommerceapplication.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "email")
    private String email;

    @Column(name = "order_date")
    private LocalDate orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    // Many orders can reference one payment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;


    // One order has many order items
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();
}
