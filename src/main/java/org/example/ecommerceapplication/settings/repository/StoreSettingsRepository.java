package org.example.ecommerceapplication.settings.repository;

import org.example.ecommerceapplication.settings.entity.StoreSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreSettingsRepository
        extends JpaRepository<StoreSettings, Long> {
}