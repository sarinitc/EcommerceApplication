package org.example.ecommerceapplication.address.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private Long addressId;

    private String street;

    private String buildingName;

    private String city;

    private String state;

    private String country;

    private String pincode;
}