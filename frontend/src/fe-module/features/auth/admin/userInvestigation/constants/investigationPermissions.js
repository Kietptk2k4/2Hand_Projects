export const INVESTIGATION_PERMISSIONS = {
  READ_PROFILE: "USER_INVESTIGATION_READ",
  READ_ENFORCEMENT: "USER_ENFORCEMENT_READ",
  SUSPEND: "USER_SUSPEND",
  BAN: "USER_BAN",
  RESTRICT: "USER_RESTRICT",
  REVOKE: "USER_ENFORCEMENT_REVOKE",
  REVOKE_ADMIN_SESSION: "ADMIN_SESSION_REVOKE",
};

export function hasInvestigationPermission(permissions, code) {
  return Array.isArray(permissions) && permissions.includes(code);
}
