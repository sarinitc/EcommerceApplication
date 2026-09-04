package org.example.ecommerceapplication.promotion.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyPromotionResponse {

    private String code;

    private BigDecimal subtotal;

    private BigDecimal discountAmount;

    private BigDecimal finalTotal;
}