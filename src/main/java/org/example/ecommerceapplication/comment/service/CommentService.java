package org.example.ecommerceapplication.comment.service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.comment.dto.request.CommentRequest;
import org.example.ecommerceapplication.comment.dto.response.CommentResponse;
import org.example.ecommerceapplication.comment.entity.Comment;
import org.example.ecommerceapplication.comment.repository.CommentRepository;
import org.example.ecommerceapplication.notification.entity.NotificationType;
import org.example.ecommerceapplication.notification.service.NotificationService;
import org.example.ecommerceapplication.product.entity.Product;
import org.example.ecommerceapplication.product.repository.ProductRepository;
import org.example.ecommerceapplication.user.entity.User;
import org.example.ecommerceapplication.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public CommentResponse createComment(CommentRequest request, String authorEmail) {
        Product product = productRepository.findByProductIdAndDeletedFalse(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        User author = userRepository.findByEmailIgnoreCase(authorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setProduct(product);
        comment.setAuthor(author);
        Comment savedComment = commentRepository.save(comment);

        User receiver = product.getSeller();
        notificationService.sendNotification(
                receiver.getId(),
                receiver.getEmail(),
                NotificationType.COMMENT_ADDED,
                "New Comment",
                "Someone commented on your product",
                savedComment.getId(),
                "COMMENT"
        );

        return new CommentResponse(savedComment.getId(), product.getProductId(), author.getId(),
                savedComment.getContent(), savedComment.getCreatedAt());
    }
}
