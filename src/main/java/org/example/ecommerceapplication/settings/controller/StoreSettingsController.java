package org.example.ecommerceapplication.settings.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.common.response.ApiResponse;
import org.example.ecommerceapplication.settings.dto.request.GeneralSettingsRequest;
import org.example.ecommerceapplication.settings.dto.request.LocalSettingsRequest;
import org.example.ecommerceapplication.settings.dto.request.NotificationSettingsRequest;
import org.example.ecommerceapplication.settings.dto.request.StorefrontSettingsRequest;
import org.example.ecommerceapplication.settings.dto.response.StoreSettingsResponse;
import org.example.ecommerceapplication.settings.service.StoreSettingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/admin/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StoreSettingsController {

    private final StoreSettingService service;

    @GetMapping
    public ResponseEntity<ApiResponse<StoreSettingsResponse>> getSettings() {
        return ok("Store settings retrieved successfully", service.getSettings());
    }

    @PatchMapping("/general")
    public ResponseEntity<ApiResponse<StoreSettingsResponse>> updateGeneral(
            @Valid @RequestBody GeneralSettingsRequest request) {
        return ok("General settings updated successfully", service.updateGeneral(request));
    }

    @PatchMapping("/locale")
    public ResponseEntity<ApiResponse<StoreSettingsResponse>> updateLocale(
            @Valid @RequestBody LocalSettingsRequest request) {
        return ok("Locale settings updated successfully", service.updateLocale(request));
    }

    @PatchMapping("/storefront")
    public ResponseEntity<ApiResponse<StoreSettingsResponse>> updateStorefront(
            @Valid @RequestBody StorefrontSettingsRequest request) {
        return ok("Storefront settings updated successfully", service.updateStorefront(request));
    }

    @PatchMapping("/notifications")
    public ResponseEntity<ApiResponse<StoreSettingsResponse>> updateNotifications(
            @Valid @RequestBody NotificationSettingsRequest request) {
        return ok("Notification settings updated successfully", service.updateNotifications(request));
    }

    private ResponseEntity<ApiResponse<StoreSettingsResponse>> ok(
            String message,
            StoreSettingsResponse payload) {
        ApiResponse<StoreSettingsResponse> response = ApiResponse
                .<StoreSettingsResponse>builder()
                .success(true)
                .message(message)
                .status(HttpStatus.OK.value())
                .payload(payload)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(response);
    }
}
