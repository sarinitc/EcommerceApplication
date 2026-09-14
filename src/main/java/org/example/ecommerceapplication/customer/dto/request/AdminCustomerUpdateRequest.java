package org.example.ecommerceapplication.customer.dto.request;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.example.ecommerceapplication.user.entity.AccountStatus;
import org.example.ecommerceapplication.user.entity.CustomerType;
import org.example.ecommerceapplication.user.entity.Gender;

import java.time.LocalDate;
@Getter
@Setter
public class AdminCustomerUpdateRequest {
    @Size(max = 100)
    private String firstName;
    @Size(max = 100)
    private String lastName;
    @Size(max = 30)
    private String phoneNumber;
    @Size(max = 500)
    @Pattern(regexp = "(?is)^(?!\\s*blob:).*$", message = "Profile image must be a persistent URL or path, not a browser blob URL")
    private String profileImage;
    private AccountStatus accountStatus;
    private CustomerType customerType;
    private Boolean taxExempt;
    private Boolean verified;
    private Gender gender;
    @Size(max = 50)
    private String preferredLanguage;
    @Size(max = 10)
    private String preferredCurrency;
    private LocalDate dateOfBirth;
    private Boolean allowMarketingEmails;
    private String internalNotes;
}
