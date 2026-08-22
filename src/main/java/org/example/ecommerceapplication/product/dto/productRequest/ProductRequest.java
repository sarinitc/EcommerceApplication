package org.example.ecommerceapplication.product.dto.productRequest;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    private String productName;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal discount;
    private BigDecimal specialPrice;
    private String image;
    private Long categoryId;
    private Long sellerId;
}
