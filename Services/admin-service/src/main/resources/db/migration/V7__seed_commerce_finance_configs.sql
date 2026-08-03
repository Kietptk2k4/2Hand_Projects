-- Seed Commerce finance configs (platform fee + min payout) for Admin system-configs tab
INSERT INTO system_configs (id, config_key, config_value, value_type, description, is_active, created_by, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'COMMERCE_PLATFORM_COMMISSION_RATE', '0.10', 'DECIMAL',
     'Platform commission on order_item.final_price (0-1); e.g. 0.10 = 10%', TRUE,
     '00000000-0000-0000-0000-000000000001', NOW(), NOW()),
    (gen_random_uuid(), 'COMMERCE_MIN_PAYOUT_AMOUNT', '100000', 'DECIMAL',
     'Minimum seller payout request amount in VND', TRUE,
     '00000000-0000-0000-0000-000000000001', NOW(), NOW())
ON CONFLICT (config_key) DO NOTHING;
