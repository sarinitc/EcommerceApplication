package org.example.ecommerceapplication.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.auth.dto.request.LoginRequest;
import org.example.ecommerceapplication.auth.dto.request.RegisterRequest;
import org.example.ecommerceapplication.auth.dto.response.AuthResponse;
import org.example.ecommerceapplication.security.JwtService;
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

import java.util.HashSet;
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


    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        Role role = roleRepository
                .findByName("CUSTOMER")
                .orElseThrow(() ->
                        new RuntimeException(
                                "CUSTOMER role not found"
                        )
                );

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
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

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        if (!user.isVerified()) {
            throw new RuntimeException(
                    "Please verify your email before login"
            );
        }

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
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
    public void verifyOtp(
            String email,
            String otp
    ) {

        boolean valid =
                otpService.verifyOtp(
                        email,
                        otp
                );

        if (!valid) {
            throw new RuntimeException(
                    "Invalid or expired OTP"
            );
        }

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );

        user.setVerified(true);

        userRepository.save(user);
    }
    public void resendOtp(String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );

        if (user.isVerified()) {
            throw new RuntimeException(
                    "Email already verified"
            );
        }

        otpService.sendOtp(
                user.getEmail()
        );
    }
}