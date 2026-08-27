package org.example.ecommerceapplication.security;

import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.cart.repository.CartItemRepository;
import org.example.ecommerceapplication.cart.repository.CartRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("cartSecurity")
@RequiredArgsConstructor
public class CartSecurity {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public boolean isOwner(
            Long cartId,
            Authentication authentication
    ) {

        if (authentication == null) {
            return false;
        }

        if (!authentication.isAuthenticated()) {
            return false;
        }

        if ("anonymousUser"
                .equals(authentication.getName())) {
            return false;
        }

        return cartRepository
                .existsByCartIdAndUserEmail(
                        cartId,
                        authentication.getName()
                );
    }

    public boolean isItemOwner(
            Long cartItemId,
            Authentication authentication
    ) {

        if (authentication == null) {
            return false;
        }

        if (!authentication.isAuthenticated()) {
            return false;
        }

        if ("anonymousUser"
                .equals(authentication.getName())) {
            return false;
        }

        return cartItemRepository
                .existsByCartItemIdAndCartUserEmail(
                        cartItemId,
                        authentication.getName()
                );
    }
}