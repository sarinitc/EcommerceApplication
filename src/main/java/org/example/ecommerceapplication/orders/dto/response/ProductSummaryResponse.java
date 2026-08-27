package org.example.ecommerceapplication.orders.dto.response;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryResponse {

    private Long productId;

    private String productName;

    private String image;
}