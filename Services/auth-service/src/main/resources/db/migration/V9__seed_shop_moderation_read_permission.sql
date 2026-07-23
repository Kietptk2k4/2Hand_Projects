-- Shop moderation history read permission (aligned with admin-service AdminPermission)

INSERT INTO permissions (code, description, created_at, updated_at)
VALUES
    ('SHOP_MODERATION_READ', 'Read shop moderation history', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
SELECT r.id, p.id, NOW(), NOW()
FROM roles r
JOIN permissions p ON p.code = 'SHOP_MODERATION_READ'
WHERE r.code IN ('ADMIN', 'MODERATOR')
ON CONFLICT DO NOTHING;
