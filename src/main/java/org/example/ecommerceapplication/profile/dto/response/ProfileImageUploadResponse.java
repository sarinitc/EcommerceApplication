package org.example.ecommerceapplication.profile.dto.response;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileImageUploadResponse {

    private String fileName;

    private String profileImage;
}