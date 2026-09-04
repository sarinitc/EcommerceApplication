package org.example.ecommerceapplication.product.controller;

import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.product.dto.productRequest.ProductRequest;
import org.example.ecommerceapplication.product.dto.productResponse.ProductResponse;
import org.example.ecommerceapplication.product.service.ProductService;
import org.example.ecommerceapplication.response.ApiResponse;
import org.example.ecommerceapplication.uploads.dto.response.ImageUploadResponse;
import org.example.ecommerceapplication.uploads.service.ProductImageService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@RestController
@RequestMapping("api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private  final ProductService productService;
    private final ProductImageService productImageService;
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
            @PathVariable (name = "productId") Long productId,
            @RequestBody ProductRequest request){
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
    @PostMapping(
            value = "/uploads",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ImageUploadResponse>>
    uploadProductImage(
            @RequestPart("file") MultipartFile file
    ) {

        ImageUploadResponse image =
                productImageService.uploadImage(file);

        ApiResponse<ImageUploadResponse> response =
                ApiResponse
                        .<ImageUploadResponse>builder()
                        .success(true)
                        .message("Image uploaded successfully")
                        .status(HttpStatus.CREATED.value())
                        .payload(image)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
