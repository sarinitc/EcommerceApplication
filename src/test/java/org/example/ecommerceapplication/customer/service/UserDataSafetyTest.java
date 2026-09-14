package org.example.ecommerceapplication.customer.service;

import org.example.ecommerceapplication.address.repository.AddressRepository;
import org.example.ecommerceapplication.auth.dto.request.RegisterRequest;
import org.example.ecommerceapplication.auth.dto.request.ResetPasswordRequest;
import org.example.ecommerceapplication.auth.service.*;
import org.example.ecommerceapplication.customer.dto.request.AdminCustomerUpdateRequest;
import org.example.ecommerceapplication.customer.dto.response.request.AdminCreateCustomerRequest;
import org.example.ecommerceapplication.orders.repository.OrderRepository;
import org.example.ecommerceapplication.profile.service.ProfileService;
import org.example.ecommerceapplication.security.JwtService;
import org.example.ecommerceapplication.user.entity.*;
import org.example.ecommerceapplication.user.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserDataSafetyTest {
    private final UserRepository users = mock(UserRepository.class);
    private final RoleRepository roles = mock(RoleRepository.class);
    private final OrderRepository orders = mock(OrderRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final EmailService mail = mock(EmailService.class);
    private final AuthService auth = new AuthService(users, roles, encoder,
            mock(AuthenticationManager.class), mock(JwtService.class), mock(OtpService.class),
            mail, mock(PasswordResetOtpService.class));
    private final AdminCustomerService admin = new AdminCustomerService(users, orders, roles,
            mock(AddressRepository.class), encoder, mail,
            mock(org.example.ecommerceapplication.profile.service.UploadService.class));

    private User customer() {
        Role role = Role.builder().id(1L).name("CUSTOMER").build();
        when(roles.findByName("CUSTOMER")).thenReturn(Optional.of(role));
        when(users.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
        User user = User.builder().id(42L).email("customer@example.com").username("customer")
                .password(encoder.encode("Original123!"))
                .roles(new HashSet<>(Set.of(role))).build();
        when(users.findById(42L)).thenReturn(Optional.of(user));
        when(users.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        return user;
    }

    @Test
    void registrationEncodesOnceAndUsesSafeDefaultsRegardlessOfSystemLocale() {
        customer();
        RegisterRequest request = new RegisterRequest();
        request.setUsername(" newcustomer ");
        request.setEmail(" I@EXAMPLE.COM ");
        request.setPassword("Password123!");
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            auth.register(request);
        } finally {
            Locale.setDefault(previous);
        }
        var capture = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(users).save(capture.capture());
        User saved = capture.getValue();
        assertEquals("i@example.com", saved.getEmail());
        assertTrue(encoder.matches("Password123!", saved.getPassword()));
        assertFalse(saved.isVerified());
        assertFalse(saved.isTaxExempt());
        assertFalse(saved.isAllowMarketingEmails());
        assertEquals(AccountStatus.ACTIVE, saved.getAccountStatus());
        assertNull(saved.getFirstName());
        assertNull(saved.getCustomerType());
        assertEquals("CUSTOMER", saved.getRoles().iterator().next().getName());
    }

    @Test
    void adminCreationEncodesPasswordAndHonorsVerificationAndStatus() {
        customer();
        AdminCreateCustomerRequest request = new AdminCreateCustomerRequest();
        request.setUsername("newcustomer");
        request.setEmail(" NEW@EXAMPLE.COM ");
        request.setTemporaryPassword("Temporary123!");
        request.setAccountStatus(AccountStatus.INVITED);
        request.setVerified(false);
        var response = admin.createCustomer(request);
        var capture = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(users).save(capture.capture());
        assertTrue(encoder.matches("Temporary123!", capture.getValue().getPassword()));
        assertEquals("new@example.com", response.getEmail());
        assertEquals("INVITED", response.getAccountStatus());
        assertFalse(response.isVerified());
        assertFalse(response.isTaxExempt());
        assertFalse(response.isAllowMarketingEmails());
        assertNull(response.getPhoneNumber());
    }

    @Test
    void resetEncodesPasswordWithoutChangingBlockedOrVerificationState() {
        User user = customer();
        user.setAccountStatus(AccountStatus.BLOCKED);
        user.setVerified(true);
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail(user.getEmail());
        request.setOtp("123456");
        request.setNewPassword("Replacement123!");
        auth.resetPassword(request);
        assertTrue(encoder.matches("Replacement123!", user.getPassword()));
        assertEquals(AccountStatus.BLOCKED, user.getAccountStatus());
        assertTrue(user.isVerified());
    }

    @Test
    void partialUpdatePreservesSecurityHistoryAndOmittedFields() {
        User user = customer();
        user.setFirstName("Existing");
        user.setAccountStatus(AccountStatus.BLOCKED);
        user.setVerified(true);
        user.setCreatedAt(LocalDateTime.of(2025, 1, 2, 3, 4));
        String password = user.getPassword();
        var originalRoles = new HashSet<>(user.getRoles());
        var originalAddresses = user.getAddresses();
        AdminCustomerUpdateRequest request = new AdminCustomerUpdateRequest();
        request.setLastName("Updated");
        var response = admin.updateCustomer(42L, request);
        assertEquals("Updated", response.getLastName());
        assertEquals("Existing", response.getFirstName());
        assertEquals("BLOCKED", response.getAccountStatus());
        assertTrue(response.isVerified());
        assertEquals(password, user.getPassword());
        assertEquals(originalRoles, user.getRoles());
        assertSame(originalAddresses, user.getAddresses());
        assertEquals(LocalDateTime.of(2025, 1, 2, 3, 4), response.getJoinedAt());
        verifyNoInteractions(orders);
    }

    @Test
    void nullOptionalFieldsLoadInProfileCustomerListAndDetailMapping() {
        User user = customer();
        assertNull(new ProfileService(users).getMyProfile(user.getEmail()).getProfileImage());
        when(users.findCustomers(isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));
        var response = admin.getCustomers(0, 10, null).getContent().get(0);
        assertNull(response.getPhoneNumber());
        assertNull(response.getProfileImage());
        assertNull(admin.updateCustomer(42L, new AdminCustomerUpdateRequest()).getFirstName());
    }

    @Test
    void bcryptSupportsExistingPrefixes() {
        String hash = encoder.encode("Existing123!");
        for (String prefix : List.of("$2a$", "$2b$", "$2y$")) {
            assertTrue(encoder.matches("Existing123!", prefix + hash.substring(4)));
        }
    }
}
