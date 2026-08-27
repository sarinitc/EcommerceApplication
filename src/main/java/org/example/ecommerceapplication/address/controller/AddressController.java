package org.example.ecommerceapplication.address.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.address.dto.request.AddressRequest;
import org.example.ecommerceapplication.address.dto.response.AddressResponse;
import org.example.ecommerceapplication.address.entity.Address;
import org.example.ecommerceapplication.address.service.AddressService;
import org.example.ecommerceapplication.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

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
    @GetMapping("/{addressId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressById(
            @PathVariable Long addressId
    ) {

        AddressResponse addressResponse =
                addressService.getAddressById(addressId);

        ApiResponse<AddressResponse> response =
                ApiResponse.<AddressResponse>builder()
                        .success(true)
                        .message("Address retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .payload(addressResponse)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAllAddresses() {

        List<AddressResponse> addresses =
                addressService.getAllAddresses();

        ApiResponse<List<AddressResponse>> response =
                ApiResponse.<List<AddressResponse>>builder()
                        .success(true)
                        .message("Addresses retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .payload(addresses)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }
    @PutMapping("/{addressId}")
    public  ResponseEntity<ApiResponse<AddressResponse>> updateAddressById(
            @PathVariable (name = "addressId") Long addressId,
           @Valid @RequestBody AddressRequest addressRequest
    ) {
        AddressResponse addressResponse = addressService.updateAddress(addressId, addressRequest);
        ApiResponse<AddressResponse> apiResponse = ApiResponse.<AddressResponse>builder()
                .success(true)
                .message("Address update successfully")
                .status(HttpStatus.OK.value())
                .payload(addressResponse)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }
    @DeleteMapping("/{addressId}")
    public  ResponseEntity<ApiResponse<Void>> deleteAddressById(
            @PathVariable (name = "addressId")
            Long addressId
    ){
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .success(true)
                .message("Address deleted successfully")
                .status(HttpStatus.OK.value())
                .payload(null)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(apiResponse);

    }

}
