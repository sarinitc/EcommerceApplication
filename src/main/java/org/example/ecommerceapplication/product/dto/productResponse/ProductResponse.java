package org.example.ecommerceapplication.product.dto.productResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;
import org.example.ecommerceapplication.category.dto.response.CategoryResponse;
import org.example.ecommerceapplication.category.dto.sellerResponse.SellerResponse;

import java.math.BigDecimal;

@Data
@Builder
@JsonPropertyOrder({
        "productId", "productName", "description", "price", "discount",
        "specialPrice", "quantity", "image", "category", "seller"
})
public class ProductResponse {

    private Long productId;
    private String productName;
    private String description;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal specialPrice;
    private Integer quantity;
    private String image;
    private CategoryResponse category;
    private SellerResponse seller;
}
