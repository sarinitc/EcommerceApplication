package org.example.ecommerceapplication.orders.dto.response;


import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private Long orderItemId;

    private BigDecimal discount;

    private BigDecimal orderedProductPrice;

    private Integer quantity;

    private ProductSummaryResponse product;
}
