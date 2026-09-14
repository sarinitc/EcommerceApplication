package org.example.ecommerceapplication.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    @ToString.Exclude
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @ToString.Exclude
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    @ToString.Exclude
    private String confirmPassword;
}
