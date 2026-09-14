package org.example.ecommerceapplication.notification.service;

import org.example.ecommerceapplication.notification.dto.request.NotificationRequest;
import org.example.ecommerceapplication.notification.dto.response.NotificationResponse;
import org.example.ecommerceapplication.notification.entity.Notification;
import org.example.ecommerceapplication.notification.entity.NotificationType;
import org.example.ecommerceapplication.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public NotificationResponse createNotification(Long userId, String username, NotificationRequest request) {
        Notification notification = Notification.builder()
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .build();
        applyRequest(notification, request);
        return saveAndPublish(notification, username);
    }

    @Transactional
    public NotificationResponse updateNotification(Long id, Long userId, NotificationRequest request) {
        Notification notification = findOwnedNotification(id, userId);
        applyRequest(notification, request);
        return mapToResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void deleteNotification(Long id, Long userId) {
        notificationRepository.delete(findOwnedNotification(id, userId));
    }

    private Notification findOwnedNotification(Long id, Long userId) {
        return notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
    }

    private void applyRequest(Notification notification, NotificationRequest request) {
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setReferenceId(request.getReferenceId());
        notification.setReferenceType(request.getReferenceType());
        notification.setRead(request.isRead());
    }

    @Transactional
    public void sendNotification(
            Long userId,
            String username,
            NotificationType type,
            String title,
            String message,
            Long referenceId,
            String referenceType
    ) {

        // 1. Create notification
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        saveAndPublish(notification, username);
    }

    private NotificationResponse saveAndPublish(Notification notification, String username) {
        // 2. Save into database
        Notification saved =
                notificationRepository.save(notification);

        // 3. Convert entity → response
        NotificationResponse response =
                mapToResponse(saved);
        // 4. Push only after an active transaction commits successfully.
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                publish(username, response);
                            } catch (MessagingException exception) {
                                log.warn(
                                        "Failed to publish committed notification {}",
                                        saved.getId(),
                                        exception
                                );
                            }
                        }
                    }
            );
            return response;
        }

        publish(username, response);
        return response;
    }


    public List<NotificationResponse> getNotifications(
            Long userId
    ) {

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    private NotificationResponse mapToResponse(
            Notification notification
    ) {

        return new NotificationResponse(

                notification.getId(),

                notification.getType(),

                notification.getTitle(),

                notification.getMessage(),

                notification.getReferenceId(),

                notification.getReferenceType(),

                notification.isRead(),

                notification.getCreatedAt()
        );
    }
    private void publish(
            String username,
            NotificationResponse response
    ) {
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notifications",
                response
        );
    }
}
