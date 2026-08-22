package org.example.ecommerceapplication.category.service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.category.dto.request.CategoryRequest;
import org.example.ecommerceapplication.category.dto.response.CategoryResponse;
import org.example.ecommerceapplication.category.dto.sellerResponse.SellerResponse;
import org.example.ecommerceapplication.category.exception.CategoryAlreadyExistsException;
import org.example.ecommerceapplication.category.repository.CategoryRepository;
import org.example.ecommerceapplication.product.dto.productResponse.ProductResponse;
import org.example.ecommerceapplication.product.repository.ProductRepository;
import org.springframework.stereotype.Service;


import org.example.ecommerceapplication.category.entity.Category;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private  final ProductRepository productRepository;


    public CategoryResponse createCategory(
            CategoryRequest request
    ) {

        // 1. Check duplicate category name
        if (categoryRepository
                .existsByCategoryNameIgnoreCase(
                        request.getCategoryName()
                )) {

            throw new CategoryAlreadyExistsException();
        }


        // 2. Convert Request -> Entity
        Category category = Category.builder()
                .categoryName(
                        request.getCategoryName()
                )
                .build();


        // 3. Save into database
        Category savedCategory =
                categoryRepository.save(category);


        // 4. Convert Entity -> Response
        return CategoryResponse.builder()
                .categoryId(
                        savedCategory.getCategoryId()
                )
                .categoryName(
                        savedCategory.getCategoryName()
                )
                .build();
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(category -> CategoryResponse.builder()
                        .categoryId(category.getCategoryId())
                        .categoryName(category.getCategoryName())
                        .build())
                .toList();
    }

    public List<ProductResponse> getAllProducts() {

        return productRepository
                .findAllByDeletedFalse()
                .stream()
                .map(product ->
                        ProductResponse.builder()
                                .productId(product.getProductId())
                                .productName(product.getProductName())
                                .description(product.getDescription())
                                .price(product.getPrice())
                                .quantity(product.getQuantity())
                                .discount(product.getDiscount())
                                .specialPrice(product.getSpecialPrice())
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
                )
                .toList();
    }
}
