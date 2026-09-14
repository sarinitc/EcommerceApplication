package org.example.ecommerceapplication.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.auth.dto.request.ChangePasswordRequest;
import org.example.ecommerceapplication.auth.dto.request.ForgotPasswordRequest;
import org.example.ecommerceapplication.auth.dto.request.LoginRequest;
import org.example.ecommerceapplication.auth.dto.request.RegisterRequest;
import org.example.ecommerceapplication.auth.dto.request.ResetPasswordRequest;
import org.example.ecommerceapplication.auth.exception.OtpException;
import org.example.ecommerceapplication.auth.dto.response.AuthResponse;
import org.example.ecommerceapplication.auth.dto.response.CurrentUserResponse;
import org.example.ecommerceapplication.security.JwtService;
import org.example.ecommerceapplication.user.entity.AccountStatus;
import org.example.ecommerceapplication.user.entity.Role;
import org.example.ecommerceapplication.user.entity.User;
import org.example.ecommerceapplication.user.repository.RoleRepository;
import org.example.ecommerceapplication.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordResetOtpService passwordResetOtpService;


    @Transactional
    public AuthResponse register(RegisterRequest request) {

        String email = normalizeEmail(request.getEmail());
        String username = request.getUsername().trim();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new OtpException("Email already exists", HttpStatus.CONFLICT);
        }

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new OtpException("Username already exists", HttpStatus.CONFLICT);
        }

        Role role = roleRepository
                .findByName("CUSTOMER")
                .orElseThrow(() ->
                        new RuntimeException(
                                "CUSTOMER role not found"
                        )
                );

        User user = User.builder()
                .username(username)
                .email(email)
                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .verified(false)
                .roles(
                        new HashSet<>(Set.of(role))
                )
                .build();

        userRepository.save(user);

        // Redis + Real Email
        otpService.sendOtp(user.getEmail());

        return AuthResponse.builder()
                .username(user.getUsername())
                .token(null)
                .build();
    }
    public AuthResponse login(LoginRequest request) {

        String email = normalizeEmail(request.getEmail());

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        if (user.getAccountStatus() == AccountStatus.BLOCKED) {
            throw new OtpException("Your account has been blocked. Please contact support.", HttpStatus.FORBIDDEN);
        }

        if (!user.isVerified()) {
            throw new RuntimeException(
                    "Please verify your email before login"
            );
        }

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                request.getPassword()
                        )
                );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        String token =
                jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .username(user.getUsername())
                .token(token)
                .build();
    }

    public CurrentUserResponse getCurrentUser(String email) {

        User user = userRepository
                .findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        return CurrentUserResponse.builder()
                .userId(user.getId())
                .accountStatus(user.getAccountStatus())
                .username(user.getUsername())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .sorted()
                        .toList())
                .build();
    }
    public void verifyOtp(
            String email,
            String otp
    ) {

        String normalizedEmail = normalizeEmail(email);
        boolean valid = otpService.verifyOtp(normalizedEmail, otp);
        if (!valid) {
            throw new OtpException("Invalid or expired OTP", HttpStatus.BAD_REQUEST);
        }
        User user = userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new OtpException(
                                "Email not found.",
                                HttpStatus.NOT_FOUND
                        )
                );
        user.setVerified(true);

        userRepository.save(user);
    }
    public void resendOtp(String email) {

        String normalizedEmail = normalizeEmail(email);

        User user = userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new OtpException(
                                "Email not found.",
                                HttpStatus.NOT_FOUND
                        )
                );
        if (user.isVerified()) {
            throw new OtpException(
                    "Email is already verified. Use forgot-password to reset a password.",
                    HttpStatus.BAD_REQUEST
            );
        }
        otpService.sendOtp(
                user.getEmail()
        );
    }
    @Transactional
    public void forgotPassword(
            ForgotPasswordRequest request
    ) {

        String email = normalizeEmail(request.getEmail());
        // 1. Find user
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new OtpException(
                                "Email not found.",
                                HttpStatus.NOT_FOUND
                        )
                );
        if (!user.isVerified()) {
            throw new OtpException(
                    "Verify your email before resetting a password.",
                    HttpStatus.BAD_REQUEST
            );
        }
        // 2. Generate OTP
        String otp =
                passwordResetOtpService
                        .generateOtp(
                                user.getEmail()
                        );


        // 3. Send OTP to email
        emailService.sendPasswordResetOtp(
                user.getEmail(),
                otp
        );
    }
    @Transactional
    public void resetPassword(
            ResetPasswordRequest request
    ) {
        String email = normalizeEmail(request.getEmail());

        // The reset OTP is purpose-specific and must be supplied here.
        passwordResetOtpService.verifyOtp(email, request.getOtp());

        // 2. Find user
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new OtpException(
                                "Email not found.",
                                HttpStatus.NOT_FOUND
                        )
                );
        // 3. Encode new password
        String encodedPassword =
                passwordEncoder.encode(
                        request.getNewPassword()
                );

        // 4. Update user's password
        user.setPassword(
                encodedPassword
        );
        // 5. Save user
        userRepository.save(user);

        // 6. Delete OTP so it cannot be reused
        passwordResetOtpService
                .deleteOtp(email);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new OtpException("New password and confirm password do not match", HttpStatus.BAD_REQUEST);
        }

        // BCrypt accepts at most 72 bytes, including multibyte UTF-8 characters.
        if (request.getCurrentPassword().getBytes(StandardCharsets.UTF_8).length > 72
                || request.getNewPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new OtpException("Passwords must not exceed 72 UTF-8 bytes", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository
                .findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new OtpException("User not found", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new OtpException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
