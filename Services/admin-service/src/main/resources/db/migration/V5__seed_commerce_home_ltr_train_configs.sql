-- Seed Commerce Home LTR train-mode system configs (Admin DB)
-- created_by: system seed actor (not a real admin user)
INSERT INTO system_configs (id, config_key, config_value, value_type, description, is_active, created_by, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'commerce.home.ltr.train_data_mode', 'SEED_ONLY', 'STRING',
     'Home LTR corpus mode: SEED_ONLY | HYBRID | REAL_ONLY', TRUE,
     '00000000-0000-0000-0000-000000000001', NOW(), NOW()),
    (gen_random_uuid(), 'commerce.home.ltr.seed_row_weight', '0.5', 'DECIMAL',
     'Sample weight for SEED rows when train_data_mode=HYBRID', TRUE,
     '00000000-0000-0000-0000-000000000001', NOW(), NOW()),
    (gen_random_uuid(), 'commerce.home.ltr.real_only_min_impressions', '5000', 'INTEGER',
     'Minimum real home impressions required for REAL_ONLY train', TRUE,
     '00000000-0000-0000-0000-000000000001', NOW(), NOW())
ON CONFLICT (config_key) DO NOTHING;
