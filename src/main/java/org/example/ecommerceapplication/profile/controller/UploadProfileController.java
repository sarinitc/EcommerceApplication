package org.example.ecommerceapplication.profile.controller;

import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.profile.dto.response.ProfileResponse;
import org.example.ecommerceapplication.response.ApiResponse;
import org.example.ecommerceapplication.profile.dto.response.ProfileImageUploadResponse;
import org.example.ecommerceapplication.profile.service.ProfileService;
import org.example.ecommerceapplication.profile.service.UploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UploadProfileController {

    private final UploadService uploadService;
    private final ProfileService profileService;


    @PostMapping(
            value = "/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<
                ApiResponse<ProfileImageUploadResponse>
                >
    uploadProfileImage(
            @RequestPart("file")
            MultipartFile file,

            Authentication authentication
    ) {

        String email =
                authentication.getName();


        ProfileImageUploadResponse result =
                uploadService.uploadProfileImage(
                        file,
                        email
                );


        ApiResponse<ProfileImageUploadResponse> response =
                ApiResponse
                        .<ProfileImageUploadResponse>builder()
                        .success(true)
                        .message(
                                "Profile image uploaded successfully"
                        )
                        .status(HttpStatus.OK.value())
                        .payload(result)
                        .timestamp(Instant.now())
                        .build();


        return ResponseEntity.ok(response);
    }
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile(
            Authentication authentication
    ) {

        String email = authentication.getName();

        ProfileResponse profile =
                profileService.getMyProfile(email);

        ApiResponse<ProfileResponse> response =
                ApiResponse
                        .<ProfileResponse>builder()
                        .success(true)
                        .message("Profile retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .payload(profile)
                        .timestamp(Instant.now())
                        .build();
        return ResponseEntity.ok(response);
    }
}
