package org.example.ecommerceapplication.cart.repository;

import org.example.ecommerceapplication.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartCartIdAndProductProductId(Long cartId, Long productId);

    List<CartItem> findAllByCartCartId(Long cartId);

    boolean existsByCartItemIdAndCartUserEmail(Long cartItemId, String email);
}
