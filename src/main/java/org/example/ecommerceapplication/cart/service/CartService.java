package org.example.ecommerceapplication.cart.service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.cart.dto.cartRequest.AddCartItemRequest;
import org.example.ecommerceapplication.cart.dto.cartRequest.UpdateCartItemRequest;
import org.example.ecommerceapplication.cart.dto.cartResponse.CartItemResponse;
import org.example.ecommerceapplication.cart.dto.cartResponse.CartResponse;
import org.example.ecommerceapplication.cart.entity.CartItem;
import org.example.ecommerceapplication.cart.entity.Cart;
import org.example.ecommerceapplication.cart.repository.CartRepository;
import org.example.ecommerceapplication.cart.repository.CartItemRepository;
import org.example.ecommerceapplication.category.dto.response.CategoryResponse;
import org.example.ecommerceapplication.category.dto.sellerResponse.SellerResponse;
import org.example.ecommerceapplication.product.dto.productResponse.ProductResponse;
import org.example.ecommerceapplication.product.entity.Product;
import org.example.ecommerceapplication.product.repository.ProductRepository;
import org.example.ecommerceapplication.user.entity.User;
import org.example.ecommerceapplication.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;


    @Transactional
    public CartResponse createCart(Long userId) {

        // 1. Find the user selected by the administrator.
        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );


        // 2. Create a new empty cart. A user may have many carts.
        Cart cart = Cart.builder()
                .user(user)
                .totalPrice(BigDecimal.ZERO)
                .build();


        // 3. Save cart
        Cart savedCart =
                cartRepository.save(cart);



        // 4. Return response
        return toCartResponse(savedCart);
    }

    @Transactional(readOnly = true)
    public List<CartResponse> getAllCarts() {

        List<Cart> carts = cartRepository.findAll();

        return carts.stream()
                .map(this::toCartResponse)
                .toList();
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getCartItems().stream()
                .map(this::toCartItemResponse)
                .toList();

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .totalPrice(cart.getTotalPrice())
                .userId(cart.getUser().getId())
                .items(items)
                .build();
    }

    private CartItemResponse toCartItemResponse(CartItem cartItem) {
        return CartItemResponse.builder()
                .cartItemId(cartItem.getCartItemId())
                .quantity(cartItem.getQuantity())
                .productPrice(cartItem.getProductPrice())
                .discount(cartItem.getDiscount())
                .product(toProductResponse(cartItem.getProduct()))
                .build();
    }

    private ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .price(product.getPrice())
                .discount(product.getDiscount())
                .specialPrice(product.getSpecialPrice())
                .quantity(product.getQuantity())
                .image(product.getImage())
                .category(CategoryResponse.builder()
                        .categoryId(product.getCategory().getCategoryId())
                        .categoryName(product.getCategory().getCategoryName())
                        .build())
                .seller(SellerResponse.builder()
                        .sellerId(product.getSeller().getId())
                        .username(product.getSeller().getUsername())
                        .build())
                .build();
    }
    @Transactional(readOnly = true)
    public CartResponse getCartById(Long cartId) {

        Cart cart = cartRepository.findByCartId(cartId)
                .orElseThrow(() ->
                        new RuntimeException("Cart ID not found")
                );

        return toCartResponse(cart);
    }
    @Transactional
    public void deleteCartById(Long cartId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() ->
                        new RuntimeException("Cart ID not found")
                );

        cartRepository.delete(cart);
    }

    @Transactional
    public CartItemResponse addCartItem(AddCartItemRequest request) {
        Cart cart = cartRepository.findByCartId(request.getCartId())
                .orElseThrow(() -> new RuntimeException("Cart ID not found"));
        Product product = productRepository.findByProductIdAndDeletedFalse(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product ID not found"));

        CartItem cartItem = cartItemRepository
                .findByCartCartIdAndProductProductId(cart.getCartId(), product.getProductId())
                .orElseGet(() -> CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .productPrice(product.getSpecialPrice() != null
                                ? product.getSpecialPrice()
                                : product.getPrice())
                        .discount(product.getDiscount())
                        .quantity(0)
                        .build());

        cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        CartItem savedCartItem = cartItemRepository.save(cartItem);
        updateCartTotal(cart);
        return toCartItemResponse(savedCartItem);
    }

    @Transactional
    public CartItemResponse updateCartItem(Long cartItemId, UpdateCartItemRequest request) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item ID not found"));

        cartItem.setQuantity(request.getQuantity());
        CartItem savedCartItem = cartItemRepository.save(cartItem);
        updateCartTotal(savedCartItem.getCart());
        return toCartItemResponse(savedCartItem);
    }

    @Transactional
    public void deleteCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item ID not found"));
        Cart cart = cartItem.getCart();
        cartItemRepository.delete(cartItem);
        cart.getCartItems().remove(cartItem);
        updateCartTotal(cart);
    }

    private void updateCartTotal(Cart cart) {
        BigDecimal total = cartItemRepository.findAllByCartCartId(cart.getCartId()).stream()
                .map(item -> item.getProductPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalPrice(total);
    }
}
