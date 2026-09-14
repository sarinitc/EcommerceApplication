package org.example.ecommerceapplication.notification;

import org.example.ecommerceapplication.notification.controller.NotificationController;
import org.example.ecommerceapplication.notification.dto.response.NotificationResponse;
import org.example.ecommerceapplication.notification.entity.Notification;
import org.example.ecommerceapplication.notification.entity.NotificationType;
import org.example.ecommerceapplication.notification.repository.NotificationRepository;
import org.example.ecommerceapplication.notification.service.NotificationService;
import org.example.ecommerceapplication.user.entity.User;
import org.example.ecommerceapplication.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NotificationTest {
    private final NotificationRepository notifications = mock(NotificationRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final ExecutorSubscribableChannel channel = new ExecutorSubscribableChannel();
    private final NotificationService service =
            new NotificationService(notifications, new SimpMessagingTemplate(channel));

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void notificationEndpointReturnsOnlyTheAuthenticatedUsersNotifications() throws Exception {
        when(users.findByEmailIgnoreCase("Customer@example.com"))
                .thenReturn(Optional.of(User.builder().id(42L).email("customer@example.com").build()));
        when(notifications.findByUserIdOrderByCreatedAtDesc(42L)).thenReturn(List.of(
                Notification.builder().id(9L).userId(42L).type(NotificationType.ORDER_UPDATED)
                        .title("Order shipped").message("Your order is on its way")
                        .referenceId(85L).referenceType("ORDER").isRead(false)
                        .createdAt(LocalDateTime.of(2026, 9, 14, 10, 30)).build()));

        var mvc = MockMvcBuilders.standaloneSetup(new NotificationController(service, users)).build();
        mvc.perform(get("/api/v1/notifications")
                        .principal(new UsernamePasswordAuthenticationToken("Customer@example.com", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(9))
                .andExpect(jsonPath("$[0].type").value("ORDER_UPDATED"))
                .andExpect(jsonPath("$[0].title").value("Order shipped"))
                .andExpect(jsonPath("$[0].read").value(false));

        verify(notifications, never()).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void sendingNotificationPersistsUnreadRecordBeforePublishingSavedPayload() {
        List<Message<?>> messages = new ArrayList<>();
        channel.subscribe(messages::add);
        when(notifications.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            assertTrue(messages.isEmpty());
            assertEquals(42L, notification.getUserId());
            assertFalse(notification.isRead());
            assertNotNull(notification.getCreatedAt());
            notification.setId(17L);
            return notification;
        });

        service.sendNotification(42L, "customer@example.com", NotificationType.ORDER_CREATED,
                "Order received", "Your order was received", 85L, "ORDER");

        assertEquals(1, messages.size());
        Message<?> message = messages.get(0);
        assertEquals("/user/customer@example.com/queue/notifications",
                SimpMessageHeaderAccessor.getDestination(message.getHeaders()));
        NotificationResponse payload = assertInstanceOf(NotificationResponse.class, message.getPayload());
        assertEquals(17L, payload.id());
        assertEquals(NotificationType.ORDER_CREATED, payload.type());
        assertEquals("Order received", payload.title());
        assertEquals("Your order was received", payload.message());
        assertEquals(85L, payload.referenceId());
        assertEquals("ORDER", payload.referenceType());
        assertFalse(payload.read());
        assertNotNull(payload.createdAt());
    }

    @Test
    void sendingNotificationDefersPublishingUntilTransactionCommit() {
        List<Message<?>> messages = new ArrayList<>();
        channel.subscribe(messages::add);
        when(notifications.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(18L);
            return notification;
        });
        beginTransactionSynchronization();

        service.sendNotification(42L, "customer@example.com", NotificationType.COMMENT_ADDED,
                "New Comment", "Someone commented on your product", 86L, "COMMENT");

        assertTrue(messages.isEmpty());
        List<TransactionSynchronization> synchronizations =
                TransactionSynchronizationManager.getSynchronizations();
        assertEquals(1, synchronizations.size());

        synchronizations.get(0).afterCommit();

        assertEquals(1, messages.size());
    }

    @Test
    void sendingNotificationDoesNotPublishWhenTransactionRollsBack() {
        List<Message<?>> messages = new ArrayList<>();
        channel.subscribe(messages::add);
        when(notifications.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        beginTransactionSynchronization();

        service.sendNotification(42L, "customer@example.com", NotificationType.COMMENT_ADDED,
                "New Comment", "Someone commented on your product", 87L, "COMMENT");

        List<TransactionSynchronization> synchronizations =
                TransactionSynchronizationManager.getSynchronizations();
        synchronizations.get(0).afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);

        assertTrue(messages.isEmpty());
    }

    @Test
    void messagingFailureAfterCommitDoesNotEscapeTheCommitCallback() {
        SimpMessagingTemplate failingTemplate = mock(SimpMessagingTemplate.class);
        NotificationService serviceWithFailingMessaging =
                new NotificationService(notifications, failingTemplate);
        when(notifications.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(19L);
            return notification;
        });
        doThrow(new MessageDeliveryException("socket unavailable"))
                .when(failingTemplate)
                .convertAndSendToUser(eq("customer@example.com"), eq("/queue/notifications"), any());
        beginTransactionSynchronization();

        serviceWithFailingMessaging.sendNotification(
                42L, "customer@example.com", NotificationType.COMMENT_ADDED,
                "New Comment", "Someone commented on your product", 88L, "COMMENT");
        TransactionSynchronization synchronization =
                TransactionSynchronizationManager.getSynchronizations().get(0);

        assertDoesNotThrow(synchronization::afterCommit);
    }

    private void beginTransactionSynchronization() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
    }
}
