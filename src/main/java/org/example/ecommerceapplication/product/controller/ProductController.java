package org.example.ecommerceapplication.product.controller;

import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.product.dto.productRequest.ProductRequest;
import org.example.ecommerceapplication.product.dto.productResponse.ProductResponse;
import org.example.ecommerceapplication.product.service.ProductService;
import org.example.ecommerceapplication.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private  final ProductService productService;
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createNewProducts(
            @RequestBody ProductRequest productRequest
    ){
        ProductResponse response = productService.createProduct(productRequest);
        if(response==null){
            ApiResponse<ProductResponse> apiResponse = new ApiResponse<>(
                    false,
                    "Product cannot create",
                    HttpStatus.NOT_FOUND.value(),
                    null,
                    Instant.now()

            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(apiResponse);
        }
        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>(
                true,
                "Products Create Successfully",
                HttpStatus.OK.value(),
                response,
                Instant.now()

        );
        return  ResponseEntity.status(HttpStatus.OK)
                .body(apiResponse);
    }
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<ProductResponse> products =
                productService.getAllProducts(page, size);

        ApiResponse<Page<ProductResponse>> response =
                ApiResponse.<Page<ProductResponse>>builder()
                        .success(true)
                        .message("Products retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .payload(products)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }
    @PutMapping("/{productId}")
    public  ResponseEntity<ApiResponse<ProductResponse>> updateProductById(
            @PathVariable (name = "productId") Long productId,ProductRequest request){
        ProductResponse response = productService.updateProductById(productId , request);
        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>(
                true,
                "Update Product successfully!",
                HttpStatus.OK.value(),
                response,
                Instant.now()
        );
        return  ResponseEntity.status(HttpStatus.OK)
                .body(apiResponse);
    }
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable Long productId
    ) {

        ProductResponse product =
                productService.getProductById(productId);

        ApiResponse<ProductResponse> response =
                ApiResponse.<ProductResponse>builder()
                        .success(true)
                        .message("Product retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .payload(product)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);

    }
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProductById(
            @PathVariable Long productId
    ) {

        productService.deleteProductById(productId);

        ApiResponse<Void> response =
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Product deleted successfully")
                        .status(HttpStatus.OK.value())
                        .payload(null)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }
}
