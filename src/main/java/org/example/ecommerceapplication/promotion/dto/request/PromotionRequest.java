package org.example.ecommerceapplication.promotion.dto.request;


import jakarta.validation.constraints.*;
import lombok.Data;
import org.example.ecommerceapplication.promotion.entity.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromotionRequest {

    @NotBlank(message = "Promotion code is required")
    private String code;


    @NotNull(message = "Discount type is required")
    private DiscountType discountType;


    @NotNull(message = "Discount value is required")
    @DecimalMin(
            value = "0.01",
            message = "Discount value must be greater than 0"
    )
    private BigDecimal discountValue;


    @DecimalMin(
            value = "0.00",
            message = "Minimum order amount cannot be negative"
    )
    private BigDecimal minimumOrderAmount;


    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;


    @NotNull(message = "End date is required")
    private LocalDateTime endDate;


    @Min(
            value = 1,
            message = "Usage limit must be at least 1"
    )
    private Integer usageLimit;


    private Boolean active;
}