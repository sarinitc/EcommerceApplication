package org.example.ecommerceapplication.customer.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerLocationResponse {

    private String city;

    private String state;

    private String country;
}