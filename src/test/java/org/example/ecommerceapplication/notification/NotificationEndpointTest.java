package org.example.ecommerceapplication.notification;

import org.example.ecommerceapplication.config.SecurityConfig;
import org.example.ecommerceapplication.notification.controller.NotificationController;
import org.example.ecommerceapplication.notification.dto.response.NotificationResponse;
import org.example.ecommerceapplication.notification.entity.Notification;
import org.example.ecommerceapplication.notification.entity.NotificationType;
import org.example.ecommerceapplication.notification.repository.NotificationRepository;
import org.example.ecommerceapplication.notification.service.NotificationService;
import org.example.ecommerceapplication.security.CustomUserDetailsService;
import org.example.ecommerceapplication.security.JwtAccessDeniedHandler;
import org.example.ecommerceapplication.security.JwtAuthenticationFilter;
import org.example.ecommerceapplication.security.JwtService;
import org.example.ecommerceapplication.user.entity.User;
import org.example.ecommerceapplication.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@SpringJUnitConfig(NotificationEndpointTest.Config.class)
class NotificationEndpointTest {
    private static final String CREATE_BODY = """
            {"type":"ORDER_CREATED","title":"Order received","message":"Your order was received",
             "referenceId":85,"referenceType":"ORDER"}
            """;

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, NotificationController.class, NotificationService.class})
    static class Config {
        @Bean NotificationRepository notifications() { return mock(NotificationRepository.class); }
        @Bean UserRepository users() { return mock(UserRepository.class); }
        @Bean List<Message<?>> messages() { return new ArrayList<>(); }
        @Bean SimpMessagingTemplate messaging(List<Message<?>> messages) {
            var channel = new ExecutorSubscribableChannel();
            channel.subscribe(messages::add);
            return new SimpMessagingTemplate(channel);
        }
        @Bean JwtAuthenticationFilter jwtFilter() {
            return new JwtAuthenticationFilter(mock(JwtService.class), mock(CustomUserDetailsService.class));
        }
        @Bean JwtAccessDeniedHandler deniedHandler() { return new JwtAccessDeniedHandler(); }
    }

    @Autowired WebApplicationContext context;
    @Autowired NotificationRepository notifications;
    @Autowired UserRepository users;
    @Autowired List<Message<?>> messages;
    private MockMvc mvc;

    @BeforeEach
    void prepare() {
        reset(notifications, users);
        messages.clear();
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        when(users.findByEmailIgnoreCase("Customer@example.com"))
                .thenReturn(Optional.of(User.builder().id(42L).email("customer@example.com").build()));
        when(notifications.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            if (notification.getId() == null) {
                notification.setId(17L);
            }
            return notification;
        });
    }

    @Test
    void createsNotificationForAuthenticatedUserAndPublishesSavedPayload() throws Exception {
        mvc.perform(post("/api/v1/notifications").with(user("Customer@example.com"))
                        .contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(17))
                .andExpect(jsonPath("$.type").value("ORDER_CREATED"))
                .andExpect(jsonPath("$.title").value("Order received"))
                .andExpect(jsonPath("$.message").value("Your order was received"))
                .andExpect(jsonPath("$.referenceId").value(85))
                .andExpect(jsonPath("$.referenceType").value("ORDER"))
                .andExpect(jsonPath("$.read").value(false))
                .andExpect(jsonPath("$.createdAt").exists());

        var saved = org.mockito.ArgumentCaptor.forClass(Notification.class);
        verify(notifications).save(saved.capture());
        assertEquals(42L, saved.getValue().getUserId());
        assertEquals(1, messages.size());
        assertEquals("/user/customer@example.com/queue/notifications",
                SimpMessageHeaderAccessor.getDestination(messages.get(0).getHeaders()));
        NotificationResponse payload = assertInstanceOf(NotificationResponse.class, messages.get(0).getPayload());
        assertEquals(17L, payload.id());
        assertEquals("Order received", payload.title());
        assertFalse(payload.read());
    }

    @Test
    void createsNotificationWithExplicitReadState() throws Exception {
        mvc.perform(post("/api/v1/notifications").with(user("Customer@example.com"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"type":"ORDER_UPDATED","title":"Delivered","message":"Delivered today","read":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void replacesOwnedNotificationWithoutChangingIdentityOrCreationTime() throws Exception {
        Notification existing = existingNotification();
        when(notifications.findByIdAndUserId(17L, 42L)).thenReturn(Optional.of(existing));

        mvc.perform(put("/api/v1/notifications/17").with(user("Customer@example.com"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"type":"ORDER_UPDATED","title":"Order shipped","message":"On the way",
                                 "referenceId":86,"referenceType":"SHIPMENT","read":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(17))
                .andExpect(jsonPath("$.type").value("ORDER_UPDATED"))
                .andExpect(jsonPath("$.title").value("Order shipped"))
                .andExpect(jsonPath("$.message").value("On the way"))
                .andExpect(jsonPath("$.referenceId").value(86))
                .andExpect(jsonPath("$.referenceType").value("SHIPMENT"))
                .andExpect(jsonPath("$.read").value(true));

        var saved = org.mockito.ArgumentCaptor.forClass(Notification.class);
        verify(notifications).save(saved.capture());
        assertEquals(17L, saved.getValue().getId());
        assertEquals(42L, saved.getValue().getUserId());
        assertEquals(LocalDateTime.of(2026, 9, 14, 10, 30), saved.getValue().getCreatedAt());
        assertTrue(messages.isEmpty());
    }

    @Test
    void replacementClearsOmittedOptionalFieldsAndDefaultsReadToFalse() throws Exception {
        Notification existing = existingNotification();
        existing.setRead(true);
        when(notifications.findByIdAndUserId(17L, 42L)).thenReturn(Optional.of(existing));

        mvc.perform(put("/api/v1/notifications/17").with(user("Customer@example.com"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"type":"ORDER_UPDATED","title":"Delivered","message":"Delivered today"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(false));

        var saved = org.mockito.ArgumentCaptor.forClass(Notification.class);
        verify(notifications).save(saved.capture());
        assertNull(saved.getValue().getReferenceId());
        assertNull(saved.getValue().getReferenceType());
        assertFalse(saved.getValue().isRead());
    }

    @Test
    void deletesOnlyTheOwnedNotificationAndReturnsNoContent() throws Exception {
        Notification existing = existingNotification();
        when(notifications.findByIdAndUserId(17L, 42L)).thenReturn(Optional.of(existing));

        mvc.perform(delete("/api/v1/notifications/17").with(user("Customer@example.com")))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(notifications).delete(existing);
        verify(notifications, never()).deleteAll();
        assertTrue(messages.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PUT", "DELETE"})
    void cannotModifyAnotherUsersNotification(String method) throws Exception {
        Notification existing = existingNotification();
        // A lookup for the owner succeeds; the attacker's scoped lookup must not.
        when(notifications.findByIdAndUserId(17L, 42L)).thenReturn(Optional.of(existing));
        when(notifications.findById(17L)).thenReturn(Optional.of(existing));
        when(users.findByEmailIgnoreCase("other@example.com"))
                .thenReturn(Optional.of(User.builder().id(7L).email("other@example.com").build()));
        var request = method.equals("PUT") ? put("/api/v1/notifications/17") : delete("/api/v1/notifications/17");

        mvc.perform(request.with(user("other@example.com"))
                        .contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
                .andExpect(status().isNotFound());

        verify(notifications, never()).save(any(Notification.class));
        verify(notifications, never()).delete(any(Notification.class));
        verify(notifications, never()).deleteById(any(Long.class));
        assertEquals("Original title", existing.getTitle());
        assertEquals(42L, existing.getUserId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PUT", "DELETE"})
    void missingNotificationReturnsNotFoundWithoutWriting(String method) throws Exception {
        var request = method.equals("PUT") ? put("/api/v1/notifications/999") : delete("/api/v1/notifications/999");
        mvc.perform(request.with(user("Customer@example.com"))
                        .contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
                .andExpect(status().isNotFound());

        verify(notifications, never()).save(any(Notification.class));
        verify(notifications, never()).delete(any(Notification.class));
        verify(notifications, never()).deleteById(any(Long.class));
    }

    @Test
    void rejectsInvalidCreateRequestsWithoutWritingOrPublishing() throws Exception {
        for (String body : List.of(
                "{}",
                "{\"type\":\"UNKNOWN\",\"title\":\"Title\",\"message\":\"Message\"}",
                "{\"type\":\"ORDER_CREATED\",\"title\":\"   \",\"message\":\"Message\"}",
                "{\"type\":\"ORDER_CREATED\",\"title\":\"Title\",\"message\":\"   \"}",
                "{\"type\":\"ORDER_CREATED\",\"title\":\"" + "a".repeat(201) + "\",\"message\":\"Message\"}",
                "{\"type\":\"ORDER_CREATED\",\"title\":\"Title\",\"message\":\"Message\",\"referenceId\":0}",
                "{\"type\":\"ORDER_CREATED\",\"title\":\"Title\",\"message\":\"Message\",\"referenceType\":\""
                        + "a".repeat(51) + "\"}"
        )) {
            mvc.perform(post("/api/v1/notifications").with(user("Customer@example.com"))
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(notifications);
        assertTrue(messages.isEmpty());
    }

    @Test
    void requiresAuthenticationForEveryNotificationMethod() throws Exception {
        for (var request : List.of(get("/api/v1/notifications"), post("/api/v1/notifications"),
                put("/api/v1/notifications/17"), delete("/api/v1/notifications/17"))) {
            mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
                    .andExpect(status().is4xxClientError());
        }
        verifyNoInteractions(notifications, users);
        assertTrue(messages.isEmpty());
    }

    @Test
    void incompleteReplacementIsRejectedWithoutWriting() throws Exception {
        mvc.perform(put("/api/v1/notifications/17").with(user("Customer@example.com"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"read\":true}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(notifications);
    }

    @Test
    void unknownAuthenticatedUserIsRejectedWithoutAccessingNotifications() throws Exception {
        for (var request : List.of(get("/api/v1/notifications"), post("/api/v1/notifications"),
                put("/api/v1/notifications/17"), delete("/api/v1/notifications/17"))) {
            mvc.perform(request.with(user("deleted@example.com"))
                            .contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
                    .andExpect(status().isUnauthorized());
        }
        verifyNoInteractions(notifications);
    }

    private Notification existingNotification() {
        return Notification.builder().id(17L).userId(42L).type(NotificationType.ORDER_CREATED)
                .title("Original title").message("Original message").referenceId(85L).referenceType("ORDER")
                .isRead(false).createdAt(LocalDateTime.of(2026, 9, 14, 10, 30)).build();
    }
}
