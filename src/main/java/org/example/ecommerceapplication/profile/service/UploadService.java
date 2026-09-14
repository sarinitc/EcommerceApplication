package org.example.ecommerceapplication.profile.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.profile.dto.response.ProfileImageUploadResponse;
import org.example.ecommerceapplication.user.entity.User;
import org.example.ecommerceapplication.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final UserRepository userRepository;

    @Value("${app.uploads.public-base-url:http://localhost:8081}")
    private String publicBaseUrl = "http://localhost:8081";

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

        return uploadProfileImageForUser(file, user);
    }

    // The caller resolves and authorizes the target user before storing a file.
    @Transactional
    public ProfileImageUploadResponse uploadProfileImageForUser(MultipartFile file, User user) {

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


        // Never use any part of the client-supplied filename as a storage path.
        String extension = switch (file.getContentType()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException("Unsupported image type");
        };


        // 5. Generate unique filename
        String fileName =
                UUID.randomUUID() + extension;


        Path target =
                uploadDirectory.resolve(fileName);


        // 6. Save physical image
        try (InputStream input = file.getInputStream()) {

            Files.copy(
                    input,
                    target
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not save profile image",
                    e
            );
        }


        // 7. Generate public URL
        String imageUrl =
                publicBaseUrl.replaceAll("/+$", "") + "/uploads/profiles/"
                        + fileName;


        // 8. Update the resolved user
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

        // MIME headers are supplied by the client; also check the file signature.
        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(12);
            boolean valid = switch (contentType) {
                case "image/jpeg" -> startsWith(header, 0xff, 0xd8, 0xff);
                case "image/png" -> startsWith(header, 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a);
                case "image/webp" -> header.length == 12
                        && startsWith(header, 0x52, 0x49, 0x46, 0x46)
                        && header[8] == 'W' && header[9] == 'E'
                        && header[10] == 'B' && header[11] == 'P';
                default -> false;
            };
            if (!valid) {
                throw new IllegalArgumentException("Image content does not match its type");
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read profile image", e);
        }
    }

    private static boolean startsWith(byte[] bytes, int... signature) {
        if (bytes.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((bytes[i] & 0xff) != signature[i]) {
                return false;
            }
        }
        return true;
    }

}
