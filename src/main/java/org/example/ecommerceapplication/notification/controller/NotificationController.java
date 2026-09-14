package org.example.ecommerceapplication.notification.controller;


import jakarta.validation.Valid;
import org.example.ecommerceapplication.notification.dto.request.NotificationRequest;
import org.example.ecommerceapplication.notification.dto.response.NotificationResponse;
import org.example.ecommerceapplication.notification.service.NotificationService;
import org.example.ecommerceapplication.user.entity.User;
import org.example.ecommerceapplication.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    @GetMapping
    public List<NotificationResponse> getNotifications(
            Authentication authentication
    ) {
        return notificationService.getNotifications(currentUser(authentication).getId());
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationRequest request,
            Authentication authentication
    ) {
        User user = currentUser(authentication);
        NotificationResponse response = notificationService.createNotification(user.getId(), user.getEmail(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public NotificationResponse updateNotification(
            @PathVariable Long id,
            @Valid @RequestBody NotificationRequest request,
            Authentication authentication
    ) {
        return notificationService.updateNotification(id, currentUser(authentication).getId(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotification(@PathVariable Long id, Authentication authentication) {
        notificationService.deleteNotification(id, currentUser(authentication).getId());
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
