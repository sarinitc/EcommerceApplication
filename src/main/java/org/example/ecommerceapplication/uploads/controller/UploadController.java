package org.example.ecommerceapplication.uploads.controller;
import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.response.ApiResponse;
import org.example.ecommerceapplication.uploads.dto.response.ImageUploadResponse;
import org.example.ecommerceapplication.uploads.service.ProductImageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class UploadController {
    private final ProductImageService productImageService;
    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadProductImage(
            @RequestPart("file") MultipartFile file
    ) {
        ImageUploadResponse image = productImageService.uploadImage(file);
        ApiResponse<ImageUploadResponse> response = ApiResponse
                .<ImageUploadResponse>builder()
                .success(true)
                .message("Image uploaded successfully")
                .status(HttpStatus.CREATED.value())
                .payload(image)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping("/products/{fileName:.+}")
    public ResponseEntity<Resource> getProductImage(
            @PathVariable String fileName
    ) {
        return productImageService.findImage(fileName)
                .map(image -> ResponseEntity.ok()
                        .contentType(MediaTypeFactory.getMediaType(fileName)
                                .orElse(MediaType.APPLICATION_OCTET_STREAM))
                        .body(image))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
