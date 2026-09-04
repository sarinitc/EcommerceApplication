package org.example.ecommerceapplication.promotion.dto.response;

import lombok.*;
import org.example.ecommerceapplication.promotion.entity.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionResponse {

    private Long promotionId;

    private String code;

    private DiscountType discountType;

    private BigDecimal discountValue;

    private BigDecimal minimumOrderAmount;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer usageLimit;

    private Integer usedCount;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
