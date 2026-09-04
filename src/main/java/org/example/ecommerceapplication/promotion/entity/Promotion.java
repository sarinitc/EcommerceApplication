package org.example.ecommerceapplication.promotion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    private Long promotionId;


    @Column(
            name = "code",
            nullable = false,
            unique = true,
            length = 100
    )
    private String code;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "discount_type",
            nullable = false,
            length = 30
    )
    private DiscountType discountType;


    @Column(
            name = "discount_value",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal discountValue;


    @Column(
            name = "minimum_order_amount",
            precision = 12,
            scale = 2
    )
    private BigDecimal minimumOrderAmount;


    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;


    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;


    @Column(name = "usage_limit")
    private Integer usageLimit;


    @Builder.Default
    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;


    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    @PrePersist
    public void prePersist() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (usedCount == null) {
            usedCount = 0;
        }

        if (active == null) {
            active = true;
        }
    }


    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}