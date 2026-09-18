package org.example.ecommerceapplication.customer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.common.response.ApiResponse;
import org.example.ecommerceapplication.customer.dto.response.AdminCustomerDetailResponse;
import org.example.ecommerceapplication.customer.dto.response.CustomerAddressResponse;
import org.example.ecommerceapplication.customer.dto.response.AdminCustomerResponse;
import org.example.ecommerceapplication.customer.dto.response.request.AdminCreateCustomerRequest;
import org.example.ecommerceapplication.customer.dto.request.AdminCustomerUpdateRequest;
import org.example.ecommerceapplication.customer.service.AdminCustomerService;
import org.example.ecommerceapplication.profile.dto.response.ProfileImageUploadResponse;
import org.example.ecommerceapplication.user.entity.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/api/v1/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {
    private final AdminCustomerService adminCustomerService;

    @PostMapping(value = "/{customerId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProfileImageUploadResponse>> uploadCustomerImage(
            @PathVariable Long customerId,
            @RequestPart("file") MultipartFile file
    ) {
        ProfileImageUploadResponse image = adminCustomerService.uploadCustomerImage(customerId, file);
        return ResponseEntity.ok(ApiResponse.<ProfileImageUploadResponse>builder()
                .success(true)
                .message("Customer photo updated successfully")
                .status(HttpStatus.OK.value())
                .payload(image)
                .timestamp(Instant.now())
                .build());
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AdminCustomerResponse>>>
    getCustomers(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size,

            @RequestParam(required = false)
            String search
    ) {

        Page<AdminCustomerResponse> customers =
                adminCustomerService.getCustomers(
                        page,
                        size,
                        search
                );

        ApiResponse<Page<AdminCustomerResponse>> response =
                ApiResponse
                        .<Page<AdminCustomerResponse>>builder()
                        .success(true)
                        .message("Customers retrieved successfully")
                        .status(200)
                        .payload(customers)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(
            @PathVariable Long customerId
    ) {
        adminCustomerService.deleteCustomer(customerId);
        ApiResponse<Void> response =
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Customer deleted successfully")
                        .status(HttpStatus.OK.value())
                        .payload(null)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{customerId}/addresses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CustomerAddressResponse>>> getCustomerAddresses(
            @PathVariable Long customerId
    ) {
        List<CustomerAddressResponse> addresses =
                adminCustomerService.getCustomerAddresses(customerId);

        ApiResponse<List<CustomerAddressResponse>> response =
                ApiResponse.<List<CustomerAddressResponse>>builder()
                        .success(true)
                        .message("Customer addresses retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .payload(addresses)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminCustomerDetailResponse>>
    createCustomer(
            @Valid @RequestBody AdminCreateCustomerRequest request
    ) {
        AdminCustomerDetailResponse customer =
                adminCustomerService.createCustomer(request);
        ApiResponse<AdminCustomerDetailResponse> response =
                ApiResponse
                        .<AdminCustomerDetailResponse>builder()
                        .success(true)
                        .message("Customer created successfully")
                        .status(HttpStatus.CREATED.value())
                        .payload(customer)
                        .timestamp(Instant.now())
                        .build();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @PatchMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminCustomerDetailResponse>> updateCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody AdminCustomerUpdateRequest request
    ) {
        AdminCustomerDetailResponse customer =
                adminCustomerService.updateCustomer(customerId, request);

        ApiResponse<AdminCustomerDetailResponse> response =
                ApiResponse.<AdminCustomerDetailResponse>builder()
                        .success(true)
                        .message("Customer updated successfully")
                        .status(HttpStatus.OK.value())
                        .payload(customer)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }
    @GetMapping(
            value = "/export",
            produces = "text/csv"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AccountStatus status
    ) {

        byte[] csv =
                adminCustomerService.exportCustomers(
                        search,
                        status
                );

        String fileName =
                "customers-" + LocalDate.now() + ".csv";

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\""
                )
                .contentType(
                        MediaType.parseMediaType("text/csv")
                )
                .body(csv);
    }
}
