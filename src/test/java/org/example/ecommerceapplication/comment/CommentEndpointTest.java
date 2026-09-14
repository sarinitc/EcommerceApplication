package org.example.ecommerceapplication.comment;

import org.example.ecommerceapplication.comment.controller.CommentController;
import org.example.ecommerceapplication.comment.entity.Comment;
import org.example.ecommerceapplication.comment.repository.CommentRepository;
import org.example.ecommerceapplication.comment.service.CommentService;
import org.example.ecommerceapplication.config.SecurityConfig;
import org.example.ecommerceapplication.notification.entity.Notification;
import org.example.ecommerceapplication.notification.entity.NotificationType;
import org.example.ecommerceapplication.notification.repository.NotificationRepository;
import org.example.ecommerceapplication.notification.service.NotificationService;
import org.example.ecommerceapplication.product.entity.Product;
import org.example.ecommerceapplication.product.repository.ProductRepository;
import org.example.ecommerceapplication.security.CustomUserDetailsService;
import org.example.ecommerceapplication.security.JwtAccessDeniedHandler;
import org.example.ecommerceapplication.security.JwtAuthenticationFilter;
import org.example.ecommerceapplication.security.JwtService;
import org.example.ecommerceapplication.user.entity.User;
import org.example.ecommerceapplication.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@SpringJUnitConfig(CommentEndpointTest.Config.class)
class CommentEndpointTest {
    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, CommentController.class, CommentService.class, NotificationService.class})
    static class Config {
        @Bean CommentRepository comments() { return mock(CommentRepository.class); }
        @Bean ProductRepository products() { return mock(ProductRepository.class); }
        @Bean UserRepository users() { return mock(UserRepository.class); }
        @Bean NotificationRepository notifications() { return mock(NotificationRepository.class); }
        @Bean SimpMessagingTemplate messaging() {
            return new SimpMessagingTemplate(new ExecutorSubscribableChannel());
        }
        @Bean JwtAuthenticationFilter jwtFilter() {
            return new JwtAuthenticationFilter(mock(JwtService.class), mock(CustomUserDetailsService.class));
        }
        @Bean JwtAccessDeniedHandler deniedHandler() { return new JwtAccessDeniedHandler(); }
    }

    @Autowired WebApplicationContext context;
    @Autowired CommentRepository comments;
    @Autowired ProductRepository products;
    @Autowired UserRepository users;
    @Autowired NotificationRepository notifications;
    private MockMvc mvc;

    @BeforeEach
    void prepare() {
        reset(comments, products, users, notifications);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        User author = User.builder().id(42L).email("customer@example.com").build();
        User seller = User.builder().id(7L).email("seller@example.com").build();
        when(users.findByEmailIgnoreCase("Customer@example.com")).thenReturn(Optional.of(author));
        when(products.findByProductIdAndDeletedFalse(85L)).thenReturn(Optional.of(
                Product.builder().productId(85L).productName("Shirt").seller(seller).build()));
        when(comments.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(123L);
            comment.setCreatedAt(LocalDateTime.of(2026, 9, 14, 12, 0));
            return comment;
        });
        when(notifications.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(456L);
            return notification;
        });
    }

    @Test
    void createsCommentForAuthenticatedAuthorAndNotifiesProductSeller() throws Exception {
        mvc.perform(post("/api/v1/comments").with(user("Customer@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":85,\"content\":\"Is this available in blue?\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.payload.id").value(123))
                .andExpect(jsonPath("$.payload.productId").value(85))
                .andExpect(jsonPath("$.payload.authorId").value(42))
                .andExpect(jsonPath("$.payload.content").value("Is this available in blue?"))
                .andExpect(jsonPath("$.payload.createdAt").exists())
                .andExpect(jsonPath("$.payload.author").doesNotExist());

        var commentCaptor = org.mockito.ArgumentCaptor.forClass(Comment.class);
        verify(comments).save(commentCaptor.capture());
        assertEquals(85L, commentCaptor.getValue().getProduct().getProductId());
        assertEquals(42L, commentCaptor.getValue().getAuthor().getId());
        var notificationCaptor = org.mockito.ArgumentCaptor.forClass(Notification.class);
        verify(notifications).save(notificationCaptor.capture());
        Notification notification = notificationCaptor.getValue();
        assertEquals(7L, notification.getUserId());
        assertEquals(NotificationType.COMMENT_ADDED, notification.getType());
        assertEquals(123L, notification.getReferenceId());
        assertEquals("COMMENT", notification.getReferenceType());
        assertFalse(notification.isRead());
    }

    @Test
    void acceptsPostIdFromTheSuppliedExampleAsProductId() throws Exception {
        mvc.perform(post("/api/v1/comments").with(user("Customer@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"postId\":85,\"content\":\"Looks good\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.payload.productId").value(85));
    }

    @Test
    void rejectsInvalidContentAndProductIdsWithoutWriting() throws Exception {
        for (String body : new String[]{
                "{\"productId\":85,\"content\":\"   \"}",
                "{\"productId\":85}",
                "{\"content\":\"Hello\"}",
                "{\"productId\":0,\"content\":\"Hello\"}",
                "{\"productId\":85,\"content\":\"" + "a".repeat(2001) + "\"}"
        }) {
            mvc.perform(post("/api/v1/comments").with(user("Customer@example.com"))
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(comments, notifications);
    }

    @Test
    void rejectsMissingOrDeletedProductWithoutWriting() throws Exception {
        mvc.perform(post("/api/v1/comments").with(user("Customer@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":999,\"content\":\"Hello\"}"))
                .andExpect(status().isNotFound());
        verifyNoInteractions(comments, notifications);
    }

    @Test
    void requiresAuthentication() throws Exception {
        mvc.perform(post("/api/v1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":85,\"content\":\"Hello\"}"))
                .andExpect(status().is4xxClientError());
        verifyNoInteractions(comments, notifications, users, products);
    }
}
