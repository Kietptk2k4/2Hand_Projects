-- Seed Social feed LTR train-mode + seen-posts retention (Admin DB)
INSERT INTO system_configs (id, config_key, config_value, value_type, description, is_active, created_by, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'social.feed.ltr.train_data_mode', 'SEED_ONLY', 'STRING',
     'Feed LTR corpus mode: SEED_ONLY | HYBRID | REAL_ONLY', TRUE,
     '00000000-0000-0000-0000-000000000001', NOW(), NOW()),
    (gen_random_uuid(), 'social.feed.ltr.seed_row_weight', '0.5', 'DECIMAL',
     'Sample weight for SEED rows when train_data_mode=HYBRID', TRUE,
     '00000000-0000-0000-0000-000000000001', NOW(), NOW()),
    (gen_random_uuid(), 'social.feed.ltr.real_only_min_impressions', '5000', 'INTEGER',
     'Minimum real post impressions required for REAL_ONLY train', TRUE,
     '00000000-0000-0000-0000-000000000001', NOW(), NOW()),
    (gen_random_uuid(), 'social.feed.seen_posts_retention_days', '7', 'INTEGER',
     'Delete user_seen_posts rows older than N days (TTL for For You recall)', TRUE,
     '00000000-0000-0000-0000-000000000001', NOW(), NOW())
ON CONFLICT (config_key) DO NOTHING;
