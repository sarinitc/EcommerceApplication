package org.example.ecommerceapplication.customer.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.example.ecommerceapplication.user.entity.AccountStatus;
import org.example.ecommerceapplication.user.entity.CustomerType;
import org.example.ecommerceapplication.user.entity.Gender;

import java.time.LocalDate;

@Getter
@Setter
public class AdminCustomerUpdateRequest {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String profileImage;
    private AccountStatus accountStatus;
    private CustomerType customerType;
    private Boolean taxExempt;
    private Boolean verified;
    private Gender gender;
    private String preferredLanguage;
    private String preferredCurrency;
    private LocalDate dateOfBirth;
    private Boolean allowMarketingEmails;
    private String internalNotes;
}
