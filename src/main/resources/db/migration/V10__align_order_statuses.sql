UPDATE orders
SET order_status = 'DELIVERED'
WHERE order_status = 'COMPLETED';
