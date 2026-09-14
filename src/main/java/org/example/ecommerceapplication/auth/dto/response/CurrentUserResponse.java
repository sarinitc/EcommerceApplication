package org.example.ecommerceapplication.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import org.example.ecommerceapplication.user.entity.AccountStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserResponse {

    private Long userId;
    private AccountStatus accountStatus;
    private String username;
    private String email;
    private String profileImage;
    private List<String> roles;
}
