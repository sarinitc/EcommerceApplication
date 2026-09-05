package org.example.ecommerceapplication.profile.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.profile.dto.response.ProfileImageUploadResponse;
import org.example.ecommerceapplication.user.entity.User;
import org.example.ecommerceapplication.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final UserRepository userRepository;

    private static final long MAX_FILE_SIZE =
            5 * 1024 * 1024; // 5MB


    @Transactional
    public ProfileImageUploadResponse uploadProfileImage(
            MultipartFile file,
            String email
    ) {

        // 1. Find logged-in user
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );


        // 2. Validate file
        validateImage(file);


        // 3. Create profiles directory
        Path uploadDirectory =
                Paths.get(
                                "uploads",
                                "profiles"
                        )
                        .toAbsolutePath()
                        .normalize();

        try {
            Files.createDirectories(
                    uploadDirectory
            );
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not create profile upload directory",
                    e
            );
        }


        // 4. Get original extension
        String originalFilename =
                file.getOriginalFilename();

        String extension = "";

        if (originalFilename != null &&
                originalFilename.contains(".")) {

            extension =
                    originalFilename.substring(
                            originalFilename.lastIndexOf(".")
                    );
        }


        // 5. Generate unique filename
        String fileName =
                UUID.randomUUID() + extension;


        Path target =
                uploadDirectory.resolve(fileName);


        // 6. Save physical image
        try {

            Files.copy(
                    file.getInputStream(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not save profile image",
                    e
            );
        }


        // 7. Generate public URL
        String imageUrl =
                "http://localhost:8081/uploads/profiles/"
                        + fileName;


        // 8. Update current user
        user.setProfileImage(imageUrl);

        userRepository.save(user);


        // 9. Return response
        return ProfileImageUploadResponse.builder()
                .fileName(fileName)
                .profileImage(imageUrl)
                .build();
    }

    private void validateImage(
            MultipartFile file
    ) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException(
                    "Image file is required"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException(
                    "Image must not exceed 5MB"
            );
        }

        String contentType =
                file.getContentType();

        if (contentType == null ||
                !(
                        contentType.equals("image/jpeg") ||
                                contentType.equals("image/png") ||
                                contentType.equals("image/webp")
                )) {

            throw new RuntimeException(
                    "Only JPG, PNG and WEBP images are allowed"
            );
        }
    }

}
