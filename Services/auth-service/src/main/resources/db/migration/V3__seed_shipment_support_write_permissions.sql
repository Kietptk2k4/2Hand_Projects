-- Admin override shipment status (FR_AdminOverrideShipmentStatus) -- Phase 0 permissions

INSERT INTO permissions (code, description, created_at, updated_at)
VALUES
    ('SHIPMENT_SUPPORT_WRITE', 'Override shipment status for support', NOW(), NOW()),
    ('SHIPMENT_SUPPORT_FORCE_WRITE', 'Force override shipment from terminal status', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- ADMIN role: write + force (MVP super-admin capability via ADMIN role)
INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
SELECT r.id, p.id, NOW(), NOW()
FROM roles r
JOIN permissions p ON p.code IN (
    'SHIPMENT_SUPPORT_WRITE',
    'SHIPMENT_SUPPORT_FORCE_WRITE'
)
WHERE r.code = 'ADMIN'
ON CONFLICT DO NOTHING;