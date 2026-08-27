package org.example.ecommerceapplication.address.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressRequest {

    @NotBlank(message = "Street is required")
    @Size(max = 255, message = "Street must not exceed 255 characters")
    private String street;


    @Size(max = 150, message = "Building name must not exceed 150 characters")
    private String buildingName;


    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;


    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;


    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;


    @NotBlank(message = "Pincode is required")
    @Size(max = 20, message = "Pincode must not exceed 20 characters")
    private String pincode;
}