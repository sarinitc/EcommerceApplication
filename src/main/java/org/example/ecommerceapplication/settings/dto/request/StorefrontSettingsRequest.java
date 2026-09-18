package org.example.ecommerceapplication.settings.dto.request;

public record StorefrontSettingsRequest(
        Boolean enableProductReviews,
        Boolean autoApproveCustomerReviews,
        Boolean enableCoupons,
        Boolean showLowStockBadges
) {}
