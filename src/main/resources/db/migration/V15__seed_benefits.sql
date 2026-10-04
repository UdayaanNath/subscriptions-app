INSERT INTO benefits (
    id, subscription_id, benefit_type, name, description, discount_percent, created_at, updated_at, version
)
VALUES
    (1, 1, 'EXCLUSIVE_DEALS', 'Member deals', 'Occasional deals for Free Monthly', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 2, 'EXCLUSIVE_DEALS', 'Member deals', 'Occasional deals for Free Quarterly', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 3, 'EXCLUSIVE_DEALS', 'Member deals', 'Occasional deals for Free Yearly', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    (4, 4, 'FREE_DELIVERY', 'Free delivery', 'Free delivery on eligible orders', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (5, 4, 'DISCOUNT', 'Silver discount', '5% off eligible orders', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (6, 5, 'FREE_DELIVERY', 'Free delivery', 'Free delivery on eligible orders', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (7, 5, 'DISCOUNT', 'Silver discount', '5% off eligible orders', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (8, 6, 'FREE_DELIVERY', 'Free delivery', 'Free delivery on eligible orders', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (9, 6, 'DISCOUNT', 'Silver discount', '5% off eligible orders', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    (10, 7, 'FREE_DELIVERY', 'Free delivery', 'Free delivery on eligible orders', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (11, 7, 'DISCOUNT', 'Gold discount', '10% off eligible orders', 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (12, 7, 'EARLY_ACCESS', 'Early access', 'Early access to new offers', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (13, 8, 'FREE_DELIVERY', 'Free delivery', 'Free delivery on eligible orders', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (14, 8, 'DISCOUNT', 'Gold discount', '10% off eligible orders', 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (15, 8, 'EARLY_ACCESS', 'Early access', 'Early access to new offers', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (16, 9, 'FREE_DELIVERY', 'Free delivery', 'Free delivery on eligible orders', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (17, 9, 'DISCOUNT', 'Gold discount', '10% off eligible orders', 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (18, 9, 'EARLY_ACCESS', 'Early access', 'Early access to new offers', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    (19, 10, 'FREE_DELIVERY', 'Free delivery', 'Free delivery on eligible orders', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (20, 10, 'DISCOUNT', 'Premium discount', '15% off eligible orders', 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (21, 10, 'PRIORITY_SUPPORT', 'Priority support', 'Priority support for Premium Monthly', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (22, 10, 'EXCLUSIVE_COUPON', 'Premium coupon', 'Exclusive coupon for Premium Monthly', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (23, 11, 'FREE_DELIVERY', 'Free delivery', 'Free delivery on eligible orders', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (24, 11, 'DISCOUNT', 'Premium discount', '15% off eligible orders', 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (25, 11, 'PRIORITY_SUPPORT', 'Priority support', 'Priority support for Premium Quarterly', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (26, 11, 'EXCLUSIVE_COUPON', 'Premium coupon', 'Exclusive coupon for Premium Quarterly', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (27, 12, 'FREE_DELIVERY', 'Free delivery', 'Free delivery on eligible orders', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (28, 12, 'DISCOUNT', 'Premium discount', '15% off eligible orders', 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (29, 12, 'PRIORITY_SUPPORT', 'Priority support', 'Priority support for Premium Yearly', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (30, 12, 'EXCLUSIVE_COUPON', 'Premium coupon', 'Exclusive coupon for Premium Yearly', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

SELECT setval(pg_get_serial_sequence('benefits', 'id'), (SELECT MAX(id) FROM benefits));
