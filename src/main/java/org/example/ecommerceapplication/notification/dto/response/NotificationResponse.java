package org.example.ecommerceapplication.notification.dto.response;



import org.example.ecommerceapplication.notification.entity.NotificationType;

import java.time.LocalDateTime;
public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        Long referenceId,
        String referenceType,
        boolean read,
        LocalDateTime createdAt
) {
}
