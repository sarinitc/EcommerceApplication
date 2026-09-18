package org.example.ecommerceapplication.settings.dto.request;

public record LocalSettingsRequest(
        String currency,
        String dateFormat,
        String timezone
) {}
