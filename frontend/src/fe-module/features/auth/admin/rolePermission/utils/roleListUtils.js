import { formatAdminDateTime } from "../../utils/formatAdminDateTime.js";

export const SYSTEM_ROLE_CODES = ["ADMIN", "MODERATOR", "USER"];

export function isSystemRole(code) {
  return SYSTEM_ROLE_CODES.includes(code);
}

export function countSystemRoles(roles) {
  const systemCodes = new Set(SYSTEM_ROLE_CODES);
  return (roles || []).filter((role) => systemCodes.has(role.code)).length;
}

export function shouldShowCreatedColumn(roles) {
  return (roles || []).some((role) => {
    if (!role.created_at || !role.updated_at) return true;
    return new Date(role.created_at).getTime() !== new Date(role.updated_at).getTime();
  });
}

export function getLatestRoleUpdatedAt(roles) {
  let latest = null;

  for (const role of roles || []) {
    if (!role.updated_at) continue;
    const timestamp = new Date(role.updated_at).getTime();
    if (Number.isNaN(timestamp)) continue;
    if (latest === null || timestamp > latest) {
      latest = timestamp;
    }
  }

  return latest === null ? null : new Date(latest).toISOString();
}

export function formatLatestRoleUpdateLabel(roles) {
  const latest = getLatestRoleUpdatedAt(roles);
  if (!latest) return null;

  const { time, date } = formatAdminDateTime(latest);
  return `${time} ${date}`;
}
