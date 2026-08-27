package org.example.ecommerceapplication.cart.dto.cartResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.ecommerceapplication.product.dto.productResponse.ProductResponse;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private Long cartItemId;

    private Integer quantity;

    private BigDecimal productPrice;

    private BigDecimal discount;

    private ProductResponse product;
}
