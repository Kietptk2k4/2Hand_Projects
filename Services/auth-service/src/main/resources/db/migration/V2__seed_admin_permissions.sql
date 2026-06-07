-- =====================================================================
-- Admin portal permissions (aligned with admin-service AdminPermission)
-- Assign full set to ADMIN; moderation/investigation subset to MODERATOR.
-- =====================================================================

INSERT INTO permissions (code, description, created_at, updated_at)
VALUES
    ('USER_SUSPEND', 'Suspend user account', NOW(), NOW()),
    ('USER_BAN', 'Ban user account', NOW(), NOW()),
    ('USER_RESTRICT', 'Restrict user account', NOW(), NOW()),
    ('USER_ENFORCEMENT_REVOKE', 'Revoke user enforcement', NOW(), NOW()),
    ('USER_ENFORCEMENT_READ', 'Read user enforcement state', NOW(), NOW()),
    ('USER_INVESTIGATION_READ', 'Read user profile for investigation', NOW(), NOW()),
    ('PRODUCT_REMOVE', 'Remove product by admin', NOW(), NOW()),
    ('PRODUCT_RESTORE', 'Restore product by admin', NOW(), NOW()),
    ('PRODUCT_MODERATION_READ', 'Read product moderation history', NOW(), NOW()),
    ('REVIEW_HIDE', 'Hide product review', NOW(), NOW()),
    ('REVIEW_REMOVE', 'Remove product review', NOW(), NOW()),
    ('REVIEW_RESTORE', 'Restore product review', NOW(), NOW()),
    ('SHOP_SUSPEND', 'Suspend shop', NOW(), NOW()),
    ('SHOP_CLOSE', 'Close shop', NOW(), NOW()),
    ('SHOP_RESTORE', 'Reopen shop', NOW(), NOW()),
    ('POST_MODERATE', 'Moderate social post', NOW(), NOW()),
    ('POST_RESTORE', 'Restore social post', NOW(), NOW()),
    ('COMMENT_MODERATE', 'Moderate social comment', NOW(), NOW()),
    ('COMMENT_RESTORE', 'Restore social comment', NOW(), NOW()),
    ('SYSTEM_CONFIG_UPDATE', 'Update system configuration', NOW(), NOW()),
    ('SYSTEM_CONFIG_VIEW', 'View system configuration', NOW(), NOW()),
    ('SYSTEM_ANNOUNCEMENT_CREATE', 'Create system announcement', NOW(), NOW()),
    ('SYSTEM_ANNOUNCEMENT_UPDATE', 'Update system announcement', NOW(), NOW()),
    ('SYSTEM_ANNOUNCEMENT_PUBLISH', 'Publish system announcement', NOW(), NOW()),
    ('SYSTEM_ANNOUNCEMENT_CANCEL', 'Cancel system announcement', NOW(), NOW()),
    ('ADMIN_AUDIT_READ', 'Read admin audit logs', NOW(), NOW()),
    ('ADMIN_AUDIT_VIEW', 'View admin audit logs', NOW(), NOW()),
    ('ADMIN_SESSION_REVOKE', 'Revoke admin session', NOW(), NOW()),
    ('ORDER_SUPPORT_READ', 'Read order support detail', NOW(), NOW()),
    ('PAYMENT_SUPPORT_READ', 'Read payment support detail', NOW(), NOW()),
    ('SHIPMENT_SUPPORT_READ', 'Read shipment support detail', NOW(), NOW()),
    ('WEBHOOK_SUPPORT_READ', 'Read webhook logs for support', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- ADMIN: all admin-portal permissions (including legacy ADMIN_ACCESS from V1)
INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
SELECT r.id, p.id, NOW(), NOW()
FROM roles r
JOIN permissions p ON p.code IN (
    'ADMIN_ACCESS',
    'USER_SUSPEND',
    'USER_BAN',
    'USER_RESTRICT',
    'USER_ENFORCEMENT_REVOKE',
    'USER_ENFORCEMENT_READ',
    'USER_INVESTIGATION_READ',
    'PRODUCT_REMOVE',
    'PRODUCT_RESTORE',
    'PRODUCT_MODERATION_READ',
    'REVIEW_HIDE',
    'REVIEW_REMOVE',
    'REVIEW_RESTORE',
    'SHOP_SUSPEND',
    'SHOP_CLOSE',
    'SHOP_RESTORE',
    'POST_MODERATE',
    'POST_RESTORE',
    'COMMENT_MODERATE',
    'COMMENT_RESTORE',
    'SYSTEM_CONFIG_UPDATE',
    'SYSTEM_CONFIG_VIEW',
    'SYSTEM_ANNOUNCEMENT_CREATE',
    'SYSTEM_ANNOUNCEMENT_UPDATE',
    'SYSTEM_ANNOUNCEMENT_PUBLISH',
    'SYSTEM_ANNOUNCEMENT_CANCEL',
    'ADMIN_AUDIT_READ',
    'ADMIN_AUDIT_VIEW',
    'ADMIN_SESSION_REVOKE',
    'ORDER_SUPPORT_READ',
    'PAYMENT_SUPPORT_READ',
    'SHIPMENT_SUPPORT_READ',
    'WEBHOOK_SUPPORT_READ'
)
WHERE r.code = 'ADMIN'
ON CONFLICT DO NOTHING;

-- MODERATOR: investigation + enforcement read + content moderation (no system config / user ban)
INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
SELECT r.id, p.id, NOW(), NOW()
FROM roles r
JOIN permissions p ON p.code IN (
    'ADMIN_ACCESS',
    'USER_INVESTIGATION_READ',
    'USER_ENFORCEMENT_READ',
    'USER_SUSPEND',
    'USER_RESTRICT',
    'USER_ENFORCEMENT_REVOKE',
    'PRODUCT_REMOVE',
    'PRODUCT_RESTORE',
    'PRODUCT_MODERATION_READ',
    'REVIEW_HIDE',
    'REVIEW_REMOVE',
    'REVIEW_RESTORE',
    'SHOP_SUSPEND',
    'SHOP_CLOSE',
    'SHOP_RESTORE',
    'POST_MODERATE',
    'POST_RESTORE',
    'COMMENT_MODERATE',
    'COMMENT_RESTORE',
    'ADMIN_AUDIT_READ'
)
WHERE r.code = 'MODERATOR'
ON CONFLICT DO NOTHING;
