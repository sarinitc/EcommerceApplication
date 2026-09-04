package org.example.ecommerceapplication.promotion.service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.promotion.dto.request.PromotionRequest;
import org.example.ecommerceapplication.promotion.dto.response.PromotionResponse;
import org.example.ecommerceapplication.promotion.entity.DiscountType;
import org.example.ecommerceapplication.promotion.entity.Promotion;
import org.example.ecommerceapplication.promotion.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;


    @Transactional
    public PromotionResponse createPromotion(
            PromotionRequest request
    ) {

        if (promotionRepository
                .existsByCodeIgnoreCase(request.getCode())) {

            throw new RuntimeException(
                    "Promotion code already exists"
            );
        }


        if (!request.getEndDate()
                .isAfter(request.getStartDate())) {

            throw new RuntimeException(
                    "End date must be after start date"
            );
        }


        // Percentage should not be over 100%
        if (request.getDiscountType()
                == DiscountType.PERCENTAGE
                &&
                request.getDiscountValue()
                        .compareTo(
                                new java.math.BigDecimal("100")
                        ) > 0) {

            throw new RuntimeException(
                    "Percentage discount cannot exceed 100%"
            );
        }


        Promotion promotion =
                Promotion.builder()
                        .code(
                                request.getCode()
                                        .trim()
                                        .toUpperCase()
                        )
                        .discountType(
                                request.getDiscountType()
                        )
                        .discountValue(
                                request.getDiscountValue()
                        )
                        .minimumOrderAmount(
                                request.getMinimumOrderAmount()
                        )
                        .startDate(
                                request.getStartDate()
                        )
                        .endDate(
                                request.getEndDate()
                        )
                        .usageLimit(
                                request.getUsageLimit()
                        )
                        .usedCount(0)
                        .active(
                                request.getActive() == null
                                        || request.getActive()
                        )
                        .build();


        Promotion saved =
                promotionRepository.save(promotion);


        return mapToResponse(saved);
    }


    @Transactional(readOnly = true)
    public List<PromotionResponse> getAllPromotions() {

        return promotionRepository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public PromotionResponse getPromotionById(
            Long promotionId
    ) {

        Promotion promotion =
                promotionRepository.findById(promotionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Promotion not found with id: "
                                                + promotionId
                                )
                        );


        return mapToResponse(promotion);
    }


    private PromotionResponse mapToResponse(
            Promotion promotion
    ) {

        return PromotionResponse.builder()
                .promotionId(
                        promotion.getPromotionId()
                )
                .code(
                        promotion.getCode()
                )
                .discountType(
                        promotion.getDiscountType()
                )
                .discountValue(
                        promotion.getDiscountValue()
                )
                .minimumOrderAmount(
                        promotion.getMinimumOrderAmount()
                )
                .startDate(
                        promotion.getStartDate()
                )
                .endDate(
                        promotion.getEndDate()
                )
                .usageLimit(
                        promotion.getUsageLimit()
                )
                .usedCount(
                        promotion.getUsedCount()
                )
                .active(
                        promotion.getActive()
                )
                .createdAt(
                        promotion.getCreatedAt()
                )
                .updatedAt(
                        promotion.getUpdatedAt()
                )
                .build();
    }
}