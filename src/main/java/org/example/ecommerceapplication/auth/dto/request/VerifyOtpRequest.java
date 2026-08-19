package org.example.ecommerceapplication.auth.dto.request;

import lombok.Data;

@Data
public class VerifyOtpRequest {

    private String email;
    private String otp;
}