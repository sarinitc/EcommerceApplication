package org.example.ecommerceapplication.comment.repository;

import org.example.ecommerceapplication.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
