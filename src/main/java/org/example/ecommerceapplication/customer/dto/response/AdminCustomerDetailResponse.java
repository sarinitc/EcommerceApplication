package org.example.ecommerceapplication.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.ecommerceapplication.user.entity.CustomerType;
import org.example.ecommerceapplication.user.entity.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCustomerDetailResponse {

    private Long customerId;

    private String username;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private String profileImage;

    private boolean verified;

    private String accountStatus;

    private CustomerType customerType;

    private boolean taxExempt;

    private Gender gender;

    private String preferredLanguage;

    private String preferredCurrency;

    private LocalDate dateOfBirth;

    private boolean allowMarketingEmails;

    private String internalNotes;

    private LocalDateTime joinedAt;

    private CustomerStatsResponse stats;
}
