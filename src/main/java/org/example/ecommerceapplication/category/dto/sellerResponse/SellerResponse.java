package org.example.ecommerceapplication.category.dto.sellerResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerResponse {

    private Long sellerId;
    private String username;
}