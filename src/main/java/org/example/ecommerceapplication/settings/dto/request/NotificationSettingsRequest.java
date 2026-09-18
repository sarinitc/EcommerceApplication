package org.example.ecommerceapplication.settings.dto.request;

public record NotificationSettingsRequest(
        Boolean sendOrderConfirmationEmails,
        Boolean sendLowStockAlerts,
        Boolean sendNewCustomerAlerts,
        Boolean sendWeeklyPerformanceSummary
) {}
