INSERT INTO subscriptions (
    id, subscription_name, subscription_tier, subscription_plan, price, subscription_status, created_at, updated_at, version
)
VALUES
    (1, 'Free Monthly', 'FREE', 'MONTHLY', 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2, 'Free Quarterly', 'FREE', 'QUATERLY', 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (3, 'Free Yearly', 'FREE', 'YEARLY', 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4, 'Silver Monthly', 'SILVER', 'MONTHLY', 499, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (5, 'Silver Quarterly', 'SILVER', 'QUATERLY', 1299, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (6, 'Silver Yearly', 'SILVER', 'YEARLY', 3999, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (7, 'Gold Monthly', 'GOLD', 'MONTHLY', 999, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (8, 'Gold Quarterly', 'GOLD', 'QUATERLY', 2699, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (9, 'Gold Yearly', 'GOLD', 'YEARLY', 7999, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (10, 'Premium Monthly', 'PREMIUM', 'MONTHLY', 1999, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (11, 'Premium Quarterly', 'PREMIUM', 'QUATERLY', 4999, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (12, 'Premium Yearly', 'PREMIUM', 'YEARLY', 14999, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

SELECT setval(pg_get_serial_sequence('subscriptions', 'id'), (SELECT MAX(id) FROM subscriptions));
