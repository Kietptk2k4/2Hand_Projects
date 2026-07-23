-- Comment moderation history read permission (aligned with admin-service AdminPermission)

INSERT INTO permissions (code, description, created_at, updated_at)
VALUES
    ('COMMENT_MODERATION_READ', 'Read comment moderation history', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
SELECT r.id, p.id, NOW(), NOW()
FROM roles r
JOIN permissions p ON p.code = 'COMMENT_MODERATION_READ'
WHERE r.code IN ('ADMIN', 'MODERATOR')
ON CONFLICT DO NOTHING;
