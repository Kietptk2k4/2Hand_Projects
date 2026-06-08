export const ADMIN_AUDIT_PERMISSIONS = {
  VIEW: "ADMIN_AUDIT_VIEW",
  READ: "ADMIN_AUDIT_READ",
};

export function hasAdminAuditPermission(permissions, code) {
  return Array.isArray(permissions) && permissions.includes(code);
}