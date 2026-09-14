package org.example.ecommerceapplication.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.auth.dto.request.*;
import org.example.ecommerceapplication.auth.dto.response.AuthResponse;
import org.example.ecommerceapplication.auth.dto.response.CurrentUserResponse;
import org.example.ecommerceapplication.auth.service.AuthService;
import org.example.ecommerceapplication.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    // =========================
    // REGISTER
    // =========================
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        AuthResponse authResponse =
                authService.register(request);

        ApiResponse<AuthResponse> response =
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message(
                                "Register successfully. OTP sent to your email."
                        )
                        .status(HttpStatus.CREATED.value())
                        .payload(authResponse)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // =========================
    // LOGIN
    // =========================
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {

        AuthResponse authResponse =
                authService.login(request);

        ApiResponse<AuthResponse> response =
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Login successfully")
                        .status(HttpStatus.OK.value())
                        .payload(authResponse)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CurrentUserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        CurrentUserResponse currentUser = authService
                .getCurrentUser(userDetails.getUsername());

        ApiResponse<CurrentUserResponse> response = ApiResponse
                .<CurrentUserResponse>builder()
                .success(true)
                .message("Current user retrieved successfully")
                .status(HttpStatus.OK.value())
                .payload(currentUser)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request
    ) {

        authService.verifyOtp(
                request.getEmail(),
                request.getOtp()
        );

        ApiResponse<String> response =
                ApiResponse.<String>builder()
                        .success(true)
                        .message("OTP verified successfully")
                        .status(200)
                        .payload("OTP verification successful")
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<String>> resendOtp(
            @Valid @RequestBody ResendOtpRequest request
    ) {

        authService.resendOtp(request.getEmail());

        ApiResponse<String> response =
                ApiResponse.<String>builder()
                        .success(true)
                        .message("OTP resent successfully")
                        .status(200)
                        .payload("Please check your email")
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {

        authService.forgotPassword(request);

        ApiResponse<Void> response =
                ApiResponse.<Void>builder()
                        .success(true)
                        .message(
                                "Password reset OTP sent successfully"
                        )
                        .status(HttpStatus.OK.value())
                        .payload(null)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {

        authService.resetPassword(request);

        ApiResponse<Void> response =
                ApiResponse.<Void>builder()
                        .success(true)
                        .message(
                                "Password reset successfully"
                        )
                        .status(HttpStatus.OK.value())
                        .payload(null)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(userDetails.getUsername(), request);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Password changed successfully")
                .status(HttpStatus.OK.value())
                .payload(null)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
