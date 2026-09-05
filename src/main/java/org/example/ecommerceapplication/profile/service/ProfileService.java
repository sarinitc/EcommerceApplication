package org.example.ecommerceapplication.profile.service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.profile.dto.response.ProfileResponse;
import org.example.ecommerceapplication.user.entity.Role;
import org.example.ecommerceapplication.user.entity.User;
import org.example.ecommerceapplication.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ProfileResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .verified(user.isVerified())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .sorted()
                        .toList())
                .build();
    }
}
