package org.example.ecommerceapplication.customer.dto.response.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.example.ecommerceapplication.address.dto.request.AddressRequest;
import org.example.ecommerceapplication.user.entity.AccountStatus;

import java.util.List;

@Data
public class AdminCreateCustomerRequest {

    @NotBlank(message = "Username is required")
    @Size(max = 20)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 50)
    private String email;

    @Size(max = 30)
    private String phoneNumber;

    @Size(max = 500)
    @Pattern(regexp = "(?is)^(?!\\s*blob:).*$", message = "Profile image must be a persistent URL or path, not a browser blob URL")
    private String profileImage;

    @NotBlank(message = "Temporary password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @lombok.ToString.Exclude
    private String temporaryPassword;

    private boolean verified;

    private AccountStatus accountStatus;

    private List<@Valid AddressRequest> addresses;
}
