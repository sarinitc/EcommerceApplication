package org.example.ecommerceapplication.uploads.service;

import org.example.ecommerceapplication.uploads.dto.response.ImageUploadResponse;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductImageService {

    private final Path uploadDirectory =
            Paths.get("uploads", "products")
                    .toAbsolutePath()
                    .normalize();

    public ProductImageService() {
        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not create upload directory",
                    e
            );
        }
    }

    public ImageUploadResponse uploadImage(
            MultipartFile file
    ) {

        if (file.isEmpty()) {
            throw new RuntimeException(
                    "Image file is required"
            );
        }

        String contentType = file.getContentType();

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

        String extension = switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalStateException("Unsupported image type");
        };

        String fileName =
                UUID.randomUUID() + extension;

        Path targetLocation =
                uploadDirectory.resolve(fileName);

        try {

            Files.copy(
                    file.getInputStream(),
                    targetLocation,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not save image",
                    e
            );
        }

        String imageUrl =
                "http://localhost:8081/uploads/products/"
                        + fileName;

        return ImageUploadResponse.builder()
                .fileName(fileName)
                .imageUrl(imageUrl)
                .build();
    }

    public Optional<Resource> findImage(String fileName) {
        if (fileName == null || !fileName.matches("[a-zA-Z0-9][a-zA-Z0-9._-]*")) {
            return Optional.empty();
        }

        Path imagePath = uploadDirectory.resolve(fileName).normalize();

        if (!imagePath.startsWith(uploadDirectory) || !Files.isRegularFile(imagePath)) {
            return Optional.empty();
        }

        try {
            Resource image = new UrlResource(imagePath.toUri());
            return image.isReadable() ? Optional.of(image) : Optional.empty();
        } catch (MalformedURLException exception) {
            return Optional.empty();
        }
    }
}
