package org.example.ecommerceapplication.settings.service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.settings.dto.request.GeneralSettingsRequest;
import org.example.ecommerceapplication.settings.dto.request.LocalSettingsRequest;
import org.example.ecommerceapplication.settings.dto.request.NotificationSettingsRequest;
import org.example.ecommerceapplication.settings.dto.request.StorefrontSettingsRequest;
import org.example.ecommerceapplication.settings.dto.response.StoreSettingsResponse;
import org.example.ecommerceapplication.settings.entity.StoreSettings;
import org.example.ecommerceapplication.settings.repository.StoreSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StoreSettingService {

    private final StoreSettingsRepository repository;

    @Transactional(readOnly = true)
    public StoreSettingsResponse getSettings() {
        return toResponse(getCurrentSettings());
    }

    @Transactional
    public StoreSettingsResponse updateGeneral(GeneralSettingsRequest request) {
        StoreSettings settings = getCurrentSettings();
        if (request.storeName() != null) settings.setStoreName(request.storeName().trim());
        if (request.supportEmail() != null) settings.setSupportEmail(request.supportEmail().trim().toLowerCase());
        if (request.supportPhone() != null) settings.setSupportPhone(request.supportPhone().trim());
        return toResponse(save(settings));
    }

    @Transactional
    public StoreSettingsResponse updateLocale(LocalSettingsRequest request) {
        StoreSettings settings = getCurrentSettings();
        if (request.currency() != null) settings.setCurrency(request.currency().trim().toUpperCase());
        if (request.dateFormat() != null) settings.setDateFormat(request.dateFormat().trim());
        if (request.timezone() != null) settings.setTimezone(request.timezone().trim());
        return toResponse(save(settings));
    }

    @Transactional
    public StoreSettingsResponse updateStorefront(StorefrontSettingsRequest request) {
        StoreSettings settings = getCurrentSettings();
        if (request.enableProductReviews() != null) settings.setEnableProductReviews(request.enableProductReviews());
        if (request.autoApproveCustomerReviews() != null) settings.setAutoApproveCustomerReviews(request.autoApproveCustomerReviews());
        if (request.enableCoupons() != null) settings.setEnableCoupons(request.enableCoupons());
        if (request.showLowStockBadges() != null) settings.setShowLowStockBadges(request.showLowStockBadges());
        return toResponse(save(settings));
    }

    @Transactional
    public StoreSettingsResponse updateNotifications(NotificationSettingsRequest request) {
        StoreSettings settings = getCurrentSettings();
        if (request.sendOrderConfirmationEmails() != null) settings.setSendOrderConfirmationEmails(request.sendOrderConfirmationEmails());
        if (request.sendLowStockAlerts() != null) settings.setSendLowStockAlerts(request.sendLowStockAlerts());
        if (request.sendNewCustomerAlerts() != null) settings.setSendNewCustomerAlerts(request.sendNewCustomerAlerts());
        if (request.sendWeeklyPerformanceSummary() != null) settings.setSendWeeklyPerformanceSummary(request.sendWeeklyPerformanceSummary());
        return toResponse(save(settings));
    }

    private StoreSettings getCurrentSettings() {
        return repository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Store settings not found"));
    }

    private StoreSettings save(StoreSettings settings) {
        settings.setUpdatedAt(LocalDateTime.now());
        return repository.save(settings);
    }

    private StoreSettingsResponse toResponse(StoreSettings settings) {
        return new StoreSettingsResponse(
                settings.getId(), settings.getStoreName(), settings.getSupportEmail(), settings.getSupportPhone(),
                settings.getCurrency(), settings.getDateFormat(), settings.getTimezone(),
                settings.isEnableProductReviews(), settings.isAutoApproveCustomerReviews(), settings.isEnableCoupons(),
                settings.isShowLowStockBadges(), settings.isSendOrderConfirmationEmails(), settings.isSendLowStockAlerts(),
                settings.isSendNewCustomerAlerts(), settings.isSendWeeklyPerformanceSummary());
    }
}
