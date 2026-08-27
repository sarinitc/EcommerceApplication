package org.example.ecommerceapplication.orders.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.ecommerceapplication.product.entity.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @Column(name = "discount")
    private BigDecimal discount;

    @Column(name = "ordered_product_price")
    private BigDecimal orderedProductPrice;

    @Column(name = "quantity")
    private Integer quantity;


    // Many order items belong to one order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private Order order;


    // Many order items can reference one product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            nullable = false
    )
    private Product product;
}
