-- Catalog management permissions (aligned with admin-service AdminPermission)

INSERT INTO permissions (code, description, created_at, updated_at)
VALUES
    ('CATALOG_READ', 'Read commerce category and brand catalog', NOW(), NOW()),
    ('CATALOG_WRITE', 'Create and update commerce category and brand catalog', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
SELECT r.id, p.id, NOW(), NOW()
FROM roles r
JOIN permissions p ON p.code IN ('CATALOG_READ', 'CATALOG_WRITE')
WHERE r.code IN ('ADMIN', 'MODERATOR')
ON CONFLICT DO NOTHING;
