package org.example.ecommerceapplication.address.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.address.dto.request.AddressRequest;
import org.example.ecommerceapplication.address.dto.response.AddressResponse;
import org.example.ecommerceapplication.address.service.AddressService;
import org.example.ecommerceapplication.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;


    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @Valid @RequestBody AddressRequest request,
            Authentication authentication
    ) {

        // Get logged-in user's email from JWT
        String email = authentication.getName();


        // Call service
        AddressResponse addressResponse =
                addressService.createAddress(
                        email,
                        request
                );


        // Build API response
        ApiResponse<AddressResponse> response =
                ApiResponse.<AddressResponse>builder()
                        .success(true)
                        .message("Address created successfully")
                        .status(HttpStatus.CREATED.value())
                        .payload(addressResponse)
                        .timestamp(Instant.now())
                        .build();


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}