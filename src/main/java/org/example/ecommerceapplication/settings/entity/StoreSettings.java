package org.example.ecommerceapplication.settings.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(name = "support_email")
    private String supportEmail;

    @Column(name = "support_phone")
    private String supportPhone;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "date_format", nullable = false)
    private String dateFormat;

    @Column(name = "timezone", nullable = false)
    private String timezone;

    @Column(name = "enable_product_reviews", nullable = false)
    private boolean enableProductReviews;

    @Column(name = "auto_approve_customer_reviews", nullable = false)
    private boolean autoApproveCustomerReviews;

    @Column(name = "enable_coupons", nullable = false)
    private boolean enableCoupons;

    @Column(name = "show_low_stock_badges", nullable = false)
    private boolean showLowStockBadges;

    @Column(name = "send_order_confirmation_emails", nullable = false)
    private boolean sendOrderConfirmationEmails;

    @Column(name = "send_low_stock_alerts", nullable = false)
    private boolean sendLowStockAlerts;

    @Column(name = "send_new_customer_alerts", nullable = false)
    private boolean sendNewCustomerAlerts;

    @Column(name = "send_weekly_performance_summary", nullable = false)
    private boolean sendWeeklyPerformanceSummary;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
