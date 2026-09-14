package org.example.ecommerceapplication.notification.repository;

import org.example.ecommerceapplication.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(
            Long userId
    );
    long countByUserIdAndIsReadFalse(Long userId);
}
