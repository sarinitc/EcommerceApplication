package org.example.ecommerceapplication.category.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.category.dto.request.CategoryRequest;
import org.example.ecommerceapplication.category.dto.response.CategoryResponse;
import org.example.ecommerceapplication.category.service.CategoryService;
import org.example.ecommerceapplication.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a category", description = "Requires the ADMIN role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Category created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "The authenticated user does not have the ADMIN role",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"success\":false,\"message\":\"Access denied: an ADMIN role is required to create a category.\",\"status\":403,\"payload\":null}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "A category with the same name already exists",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"success\":false,\"message\":\"Category already exists\",\"status\":409,\"payload\":null}"
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @RequestBody CategoryRequest request
    ) {

        CategoryResponse categoryResponse =
                categoryService.createCategory(request);

        ApiResponse<CategoryResponse> response =
                ApiResponse.<CategoryResponse>builder()
                        .success(true)
                        .message("Category created successfully")
                        .status(HttpStatus.CREATED.value())
                        .payload(categoryResponse)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllLCategory(){
        List<CategoryResponse> categoryResponses = categoryService.getAllCategories();
        ApiResponse<List<CategoryResponse>> apiResponse =
                ApiResponse.<List<CategoryResponse>>builder()
                        .success(true)
                        .message("Categories retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .payload(categoryResponses)
                        .timestamp((Instant.now()))
                        .build();
        return ResponseEntity.ok(apiResponse);
    }
}
