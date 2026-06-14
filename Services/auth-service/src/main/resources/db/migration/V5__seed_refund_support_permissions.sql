-- Refund approval support permissions (aligned with admin-service AdminPermission)

INSERT INTO permissions (code, description, created_at, updated_at)
VALUES
    ('REFUND_SUPPORT_READ', 'Read refund approval queue', NOW(), NOW()),
    ('REFUND_SUPPORT_APPROVE', 'Confirm or reject refund requests', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
SELECT r.id, p.id, NOW(), NOW()
FROM roles r
JOIN permissions p ON p.code IN (
    'REFUND_SUPPORT_READ',
    'REFUND_SUPPORT_APPROVE'
)
WHERE r.code = 'ADMIN'
ON CONFLICT DO NOTHING;
