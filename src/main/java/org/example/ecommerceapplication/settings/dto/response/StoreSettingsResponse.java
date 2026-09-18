package org.example.ecommerceapplication.settings.dto.response;

public record StoreSettingsResponse(
        Long id,
        String storeName,
        String supportEmail,
        String supportPhone,
        String currency,
        String dateFormat,
        String timezone,
        boolean enableProductReviews,
        boolean autoApproveCustomerReviews,
        boolean enableCoupons,
        boolean showLowStockBadges,
        boolean sendOrderConfirmationEmails,
        boolean sendLowStockAlerts,
        boolean sendNewCustomerAlerts,
        boolean sendWeeklyPerformanceSummary
) {}
