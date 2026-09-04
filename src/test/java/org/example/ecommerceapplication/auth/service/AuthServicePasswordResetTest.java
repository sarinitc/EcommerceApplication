package org.example.ecommerceapplication.auth.service;

import org.example.ecommerceapplication.auth.dto.request.ForgotPasswordRequest;
import org.example.ecommerceapplication.auth.dto.request.ResetPasswordRequest;
import org.example.ecommerceapplication.auth.exception.OtpException;
import org.example.ecommerceapplication.security.JwtService;
import org.example.ecommerceapplication.user.entity.User;
import org.example.ecommerceapplication.user.repository.RoleRepository;
import org.example.ecommerceapplication.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServicePasswordResetTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private EmailService emailService;
    private PasswordResetOtpService passwordResetOtpService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        RoleRepository roleRepository = mock(RoleRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtService jwtService = mock(JwtService.class);
        OtpService otpService = mock(OtpService.class);
        emailService = mock(EmailService.class);
        passwordResetOtpService = mock(PasswordResetOtpService.class);
        authService = new AuthService(
                userRepository, roleRepository, passwordEncoder, authenticationManager,
                jwtService, otpService, emailService, passwordResetOtpService
        );
    }

    @Test
    void resetPasswordRequiresVerifiedOtpAndStoresOnlyEncodedPassword() {
        User user = User.builder().email("user@gmail.com").password("old-hash").build();
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("user@gmail.com");
        request.setOtp("123456");
        request.setNewPassword("new-password");
        when(userRepository.findByEmailIgnoreCase("user@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-password");

        authService.resetPassword(request);

        verify(passwordResetOtpService).verifyOtp("user@gmail.com", "123456");
        assertEquals("encoded-password", user.getPassword());
        verify(userRepository).save(user);
        verify(passwordResetOtpService).deleteOtp("user@gmail.com");
    }

    @Test
    void resetPasswordDoesNotUpdatePasswordBeforeOtpVerification() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("user@gmail.com");
        request.setNewPassword("new-password");
        doThrow(new OtpException("Invalid OTP.", HttpStatus.BAD_REQUEST))
                .when(passwordResetOtpService).verifyOtp("user@gmail.com", null);

        assertThrows(OtpException.class, () -> authService.resetPassword(request));

        verify(userRepository, never()).findByEmailIgnoreCase(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void forgotPasswordRejectsUnknownEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("missing@gmail.com");
        when(userRepository.findByEmailIgnoreCase("missing@gmail.com")).thenReturn(Optional.empty());

        OtpException exception = assertThrows(OtpException.class, () -> authService.forgotPassword(request));

        assertEquals("Email not found.", exception.getMessage());
        verify(passwordResetOtpService, never()).generateOtp(any());
    }

    @Test
    void resendRejectsAnAlreadyVerifiedUser() {
        User user = User.builder().email("user@gmail.com").verified(true).build();
        when(userRepository.findByEmailIgnoreCase("user@gmail.com")).thenReturn(Optional.of(user));

        assertThrows(OtpException.class, () -> authService.resendOtp("user@gmail.com"));

        verify(passwordResetOtpService, never()).generateOtp(any());
        verify(emailService, never()).sendPasswordResetOtp(any(), any());
    }
}
