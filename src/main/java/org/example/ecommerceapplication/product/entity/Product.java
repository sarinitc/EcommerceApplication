package org.example.ecommerceapplication.product.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.ecommerceapplication.category.entity.Category;
import org.example.ecommerceapplication.user.entity.User;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "description")
    private String description;

    @Column(name = "discount", precision = 5, scale = 2)
    private BigDecimal discount;

    @Column(name = "image")
    private String image;

    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "special_price", precision = 12, scale = 2)
    private BigDecimal specialPrice;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;


    // Many products can belong to one category
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;


    // Many products can belong to one seller
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
}
