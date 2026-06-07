const ADMIN_ROLE_CODES = new Set(["ADMIN", "MODERATOR"]);

export function isAdminInvestigationTarget(targetUser) {
  const roleCodes = targetUser?.role_codes;
  if (!Array.isArray(roleCodes)) {
    return false;
  }
  return roleCodes.some((code) => ADMIN_ROLE_CODES.has(code));
}
