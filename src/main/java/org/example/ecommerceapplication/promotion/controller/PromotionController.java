package org.example.ecommerceapplication.promotion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.common.response.ApiResponse;
import org.example.ecommerceapplication.promotion.dto.request.PromotionRequest;
import org.example.ecommerceapplication.promotion.dto.response.PromotionResponse;
import org.example.ecommerceapplication.promotion.service.PromotionService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromotionResponse>>
    createPromotion(
            @Valid @RequestBody PromotionRequest request
    ) {

        PromotionResponse promotion =
                promotionService.createPromotion(request);


        ApiResponse<PromotionResponse> response =
                ApiResponse
                        .<PromotionResponse>builder()
                        .success(true)
                        .message(
                                "Promotion created successfully"
                        )
                        .status(
                                HttpStatus.CREATED.value()
                        )
                        .payload(promotion)
                        .timestamp(Instant.now())
                        .build();


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<
            ApiResponse<List<PromotionResponse>>
            > getAllPromotions() {

        List<PromotionResponse> promotions =
                promotionService.getAllPromotions();


        ApiResponse<List<PromotionResponse>> response =
                ApiResponse
                        .<List<PromotionResponse>>builder()
                        .success(true)
                        .message(
                                "Promotions retrieved successfully"
                        )
                        .status(HttpStatus.OK.value())
                        .payload(promotions)
                        .timestamp(Instant.now())
                        .build();


        return ResponseEntity.ok(response);
    }


    @GetMapping("/{promotionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromotionResponse>>
    getPromotionById(
            @PathVariable Long promotionId
    ) {

        PromotionResponse promotion =
                promotionService
                        .getPromotionById(promotionId);


        ApiResponse<PromotionResponse> response =
                ApiResponse
                        .<PromotionResponse>builder()
                        .success(true)
                        .message(
                                "Promotion retrieved successfully"
                        )
                        .status(HttpStatus.OK.value())
                        .payload(promotion)
                        .timestamp(Instant.now())
                        .build();


        return ResponseEntity.ok(response);
    }
}