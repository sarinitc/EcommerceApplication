package org.example.ecommerceapplication.auth.controller;

import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.auth.dto.request.LoginRequest;
import org.example.ecommerceapplication.auth.dto.request.RegisterRequest;
import org.example.ecommerceapplication.auth.dto.request.ResendOtpRequest;
import org.example.ecommerceapplication.auth.dto.request.VerifyOtpRequest;
import org.example.ecommerceapplication.auth.dto.response.AuthResponse;
import org.example.ecommerceapplication.auth.service.AuthService;
import org.example.ecommerceapplication.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestBody RegisterRequest request
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
            @RequestBody LoginRequest request
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
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(
            @RequestBody VerifyOtpRequest request
    ) {

        authService.verifyOtp(
                request.getEmail(),
                request.getOtp()
        );

        ApiResponse<String> response =
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Email verified successfully")
                        .status(200)
                        .payload("Verification successful")
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<String>> resendOtp(
            @RequestBody ResendOtpRequest request
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
}