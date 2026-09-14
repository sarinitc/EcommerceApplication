package org.example.ecommerceapplication.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.ecommerceapplication.notification.entity.NotificationType;

@Getter
@Setter
public class NotificationRequest {
    @NotNull
    private NotificationType type;

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String message;

    @Positive
    private Long referenceId;

    @Size(max = 50)
    private String referenceType;

    private boolean read;
}
