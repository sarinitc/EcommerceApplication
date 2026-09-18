package org.example.ecommerceapplication.settings.dto.request;

public record GeneralSettingsRequest(
        String storeName,
        String supportEmail,
        String supportPhone
) {}