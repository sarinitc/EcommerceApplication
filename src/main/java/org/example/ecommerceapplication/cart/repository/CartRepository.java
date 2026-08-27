package org.example.ecommerceapplication.cart.repository;

import org.example.ecommerceapplication.cart.entity.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
@Repository
public interface CartRepository
        extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = {
            "cartItems",
            "cartItems.product",
            "cartItems.product.category",
            "cartItems.product.seller"
    })
    Optional<Cart> findByCartId(Long cartId);

    boolean existsByCartIdAndUserEmail(Long cartId, String email);

    @Override
    @EntityGraph(attributePaths = {
            "cartItems",
            "cartItems.product",
            "cartItems.product.category",
            "cartItems.product.seller"
    })
    List<Cart> findAll();

}
