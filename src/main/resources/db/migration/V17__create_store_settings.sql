CREATE TABLE store_settings (
    id BIGSERIAL PRIMARY KEY,

    store_name VARCHAR(150) NOT NULL,
    support_email VARCHAR(150),
    support_phone VARCHAR(30),

    currency VARCHAR(10) NOT NULL,
    date_format VARCHAR(30) NOT NULL,
    timezone VARCHAR(100) NOT NULL,

    enable_product_reviews BOOLEAN NOT NULL DEFAULT TRUE,
    auto_approve_customer_reviews BOOLEAN NOT NULL DEFAULT FALSE,
    enable_coupons BOOLEAN NOT NULL DEFAULT TRUE,
    show_low_stock_badges BOOLEAN NOT NULL DEFAULT TRUE,

    send_order_confirmation_emails BOOLEAN NOT NULL DEFAULT TRUE,
    send_low_stock_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    send_new_customer_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    send_weekly_performance_summary BOOLEAN NOT NULL DEFAULT FALSE,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
