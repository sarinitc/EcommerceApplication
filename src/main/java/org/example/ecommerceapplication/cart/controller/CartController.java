package org.example.ecommerceapplication.cart.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.example.ecommerceapplication.cart.dto.cartRequest.AddCartItemRequest;
import org.example.ecommerceapplication.cart.dto.cartRequest.CreateCartRequest;
import org.example.ecommerceapplication.cart.dto.cartRequest.UpdateCartItemRequest;
import org.example.ecommerceapplication.cart.dto.cartResponse.CartItemResponse;
import org.example.ecommerceapplication.cart.dto.cartResponse.CartResponse;
import org.example.ecommerceapplication.cart.service.CartService;
import org.example.ecommerceapplication.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CartResponse>> createCart(
            @Valid @RequestBody CreateCartRequest request
    ) {
        CartResponse cartResponse =
                cartService.createCart(request.getUserId());

        ApiResponse<CartResponse> response =
                ApiResponse.<CartResponse>builder()
                        .success(true)
                        .message("Cart created successfully")
                        .status(HttpStatus.CREATED.value())
                        .payload(cartResponse)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CartResponse>>> getAllCarts() {

        List<CartResponse> carts = cartService.getAllCarts();

        ApiResponse<List<CartResponse>> response =
                ApiResponse.<List<CartResponse>>builder()
                        .success(true)
                        .message("Carts retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .payload(carts)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{cartId}")
    @PreAuthorize("hasRole('ADMIN') or @cartSecurity.isOwner(#cartId, authentication)")
    public ResponseEntity<ApiResponse<CartResponse>> getCartById(
            @PathVariable Long cartId
    ) {

        CartResponse cartResponse =
                cartService.getCartById(cartId);

        ApiResponse<CartResponse> response =
                ApiResponse.<CartResponse>builder()
                        .success(true)
                        .message("Cart retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .payload(cartResponse)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{cartId}")
    @PreAuthorize("hasRole('ADMIN') or @cartSecurity.isOwner(#cartId, authentication)")
    public ResponseEntity<ApiResponse<Void>> deleteCartById(
            @PathVariable Long cartId
    ) {

        cartService.deleteCartById(cartId);

        ApiResponse<Void> response =
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Cart deleted successfully")
                        .status(HttpStatus.OK.value())
                        .payload(null)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('ADMIN') or @cartSecurity.isOwner(#request.cartId, authentication)")
    public ResponseEntity<ApiResponse<CartItemResponse>> addCartItem(
            @Valid @RequestBody AddCartItemRequest request
    ) {
        CartItemResponse cartItem = cartService.addCartItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<CartItemResponse>builder()
                        .success(true)
                        .message("Cart item added successfully")
                        .status(HttpStatus.CREATED.value())
                        .payload(cartItem)
                        .timestamp(Instant.now())
                        .build()
        );
    }

    @PutMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('ADMIN') or @cartSecurity.isItemOwner(#cartItemId, authentication)")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        CartItemResponse cartItem = cartService.updateCartItem(cartItemId, request);
        return ResponseEntity.ok(
                ApiResponse.<CartItemResponse>builder()
                        .success(true)
                        .message("Cart item updated successfully")
                        .status(HttpStatus.OK.value())
                        .payload(cartItem)
                        .timestamp(Instant.now())
                        .build()
        );
    }

    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('ADMIN') or @cartSecurity.isItemOwner(#cartItemId, authentication)")
    public ResponseEntity<ApiResponse<Void>> deleteCartItem(
            @PathVariable Long cartItemId
    ) {
        cartService.deleteCartItem(cartItemId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Cart item deleted successfully")
                        .status(HttpStatus.OK.value())
                        .payload(null)
                        .timestamp(Instant.now())
                        .build()
        );
    }
}
