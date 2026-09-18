INSERT INTO store_settings (
    store_name,
    currency,
    date_format,
    timezone
)
SELECT
    'My Store',
    'USD',
    'yyyy-MM-dd',
    'UTC'
WHERE NOT EXISTS (
    SELECT 1
    FROM store_settings
);
