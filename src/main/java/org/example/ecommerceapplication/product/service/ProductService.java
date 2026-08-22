package org.example.ecommerceapplication.product.service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.category.dto.response.CategoryResponse;
import org.example.ecommerceapplication.category.dto.sellerResponse.SellerResponse;
import org.example.ecommerceapplication.category.entity.Category;
import org.example.ecommerceapplication.category.repository.CategoryRepository;
import org.example.ecommerceapplication.product.dto.productRequest.ProductRequest;
import org.example.ecommerceapplication.product.dto.productResponse.ProductResponse;
import org.example.ecommerceapplication.product.entity.Product;
import org.example.ecommerceapplication.product.exception.ProductAlreadyExistsException;
import org.example.ecommerceapplication.product.repository.ProductRepository;
import org.example.ecommerceapplication.user.entity.User;
import org.example.ecommerceapplication.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;


    public ProductResponse createProduct(ProductRequest request) {

        if (productRepository.existsByProductNameIgnoreCaseAndDeletedFalse(request.getProductName())) {
            throw new ProductAlreadyExistsException(request.getProductName());
        }

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() ->
                        new RuntimeException("Category not found")
                );
        // find user by id if cannot find that user throw an error seller not found.

        User seller = userRepository
                .findById(request.getSellerId())
                .orElseThrow(() ->
                        new RuntimeException("Seller not found")
                );

        Product product = Product.builder()
                .productName(request.getProductName())
                .description(request.getDescription())

                .price(request.getPrice())
                .quantity(request.getQuantity())
                .discount(request.getDiscount())
                .specialPrice(request.getSpecialPrice())
                .image(request.getImage())
                .category(category)
                .seller(seller)
                .build();
        Product savedProduct = productRepository.save(product);
        return ProductResponse.builder()
                .productId(savedProduct.getProductId())
                .productName(savedProduct.getProductName())
                .description(savedProduct.getDescription())
                .price(savedProduct.getPrice())
                .quantity(savedProduct.getQuantity())
                .discount(savedProduct.getDiscount())
                .specialPrice(savedProduct.getSpecialPrice())
                .image(savedProduct.getImage())
                .category(CategoryResponse.builder()
                        .categoryId(savedProduct.getCategory().getCategoryId())
                        .categoryName(savedProduct.getCategory().getCategoryName())
                        .build())
                .seller(SellerResponse.builder()
                        .sellerId(savedProduct.getSeller().getId())
                        .username(savedProduct.getSeller().getUsername())
                        .build())
                .build();
    }

    public Page<ProductResponse> getAllProducts(
            int page,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        Page<Product> products =
                productRepository.findAllByDeletedFalse(pageable);

        return products.map(product ->
                ProductResponse.builder()
                        .productId(product.getProductId())
                        .productName(product.getProductName())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .discount(product.getDiscount())
                        .specialPrice(product.getSpecialPrice())
                        .quantity(product.getQuantity())
                        .image(product.getImage())

                        .category(
                                CategoryResponse.builder()
                                        .categoryId(
                                                product.getCategory()
                                                        .getCategoryId()
                                        )
                                        .categoryName(
                                                product.getCategory()
                                                        .getCategoryName()
                                        )
                                        .build()
                        )

                        .seller(
                                SellerResponse.builder()
                                        .sellerId(
                                                product.getSeller()
                                                        .getId()
                                        )
                                        .username(
                                                product.getSeller()
                                                        .getUsername()
                                        )
                                        .build()
                        )

                        .build()
        );
    }

    public ProductResponse updateProductById(
            Long productId,
            ProductRequest request
    ) {
        // find that existing product
        Product product = productRepository
                .findByProductIdAndDeletedFalse(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found")
                );
        // find that category
        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() ->
                        new RuntimeException("Category not found"));
        // 3. Find seller from request
        User seller = userRepository
                .findById(request.getSellerId())
                .orElseThrow(() ->
                        new RuntimeException("Seller not found")
                );
        // 4. Update existing product fields
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setDiscount(request.getDiscount());
        product.setSpecialPrice(request.getSpecialPrice());
        product.setImage(request.getImage());
        product.setCategory(category);
        product.setSeller(seller);
        // 5. Save updated product
        Product updatedProduct= productRepository.save(product);

        // 6. Return response
        return ProductResponse.builder()
                .productId(updatedProduct.getProductId())
                .productName(updatedProduct.getProductName())
                .description(updatedProduct.getDescription())
                .price(updatedProduct.getPrice())
                .discount(updatedProduct.getDiscount())
                .specialPrice(updatedProduct.getSpecialPrice())
                .quantity(updatedProduct.getQuantity())
                .image(updatedProduct.getImage())

                .category(
                        CategoryResponse.builder()
                                .categoryId(
                                        updatedProduct
                                                .getCategory()
                                                .getCategoryId()
                                )
                                .categoryName(
                                        updatedProduct
                                                .getCategory()
                                                .getCategoryName()
                                )
                                .build()
                )

                .seller(
                        SellerResponse.builder()
                                .sellerId(
                                        updatedProduct
                                                .getSeller()
                                                .getId()
                                )
                                .username(
                                        updatedProduct
                                                .getSeller()
                                                .getUsername()
                                )
                                .build()
                )

                .build();
    }
    public ProductResponse getProductById(Long productId) {

        Product product = productRepository
                .findByProductIdAndDeletedFalse(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found")
                );

        return ProductResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .price(product.getPrice())
                .discount(product.getDiscount())
                .specialPrice(product.getSpecialPrice())
                .quantity(product.getQuantity())
                .image(product.getImage())

                .category(
                        CategoryResponse.builder()
                                .categoryId(
                                        product.getCategory()
                                                .getCategoryId()
                                )
                                .categoryName(
                                        product.getCategory()
                                                .getCategoryName()
                                )
                                .build()
                )

                .seller(
                        SellerResponse.builder()
                                .sellerId(
                                        product.getSeller()
                                                .getId()
                                )
                                .username(
                                        product.getSeller()
                                                .getUsername()
                                )
                                .build()
                )

                .build();
    }
    public ProductResponse deleteProductById(Long productId) {

        // 1. Find existing product
        Product product = productRepository
                .findByProductIdAndDeletedFalse(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found")
                );

        // 2. Keep the product for existing orders, but hide it from active listings.
        product.setDeleted(true);
        Product deletedProduct = productRepository.save(product);

        // 3. Return deleted product information
        return ProductResponse.builder()
                .productId(deletedProduct.getProductId())
                .productName(deletedProduct.getProductName())
                .description(deletedProduct.getDescription())
                .price(deletedProduct.getPrice())
                .quantity(deletedProduct.getQuantity())
                .discount(deletedProduct.getDiscount())
                .specialPrice(deletedProduct.getSpecialPrice())
                .image(deletedProduct.getImage())
                .build();
    }
}

