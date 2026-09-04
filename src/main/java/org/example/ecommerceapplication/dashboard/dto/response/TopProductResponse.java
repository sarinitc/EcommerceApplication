package org.example.ecommerceapplication.dashboard.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductResponse {

    private Long productId;

    private String productName;

    private String image;

    private long unitsSold;
}
