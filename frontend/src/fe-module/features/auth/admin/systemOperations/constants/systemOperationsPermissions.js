export const SYSTEM_CONFIG_PERMISSIONS = {
  VIEW: "SYSTEM_CONFIG_VIEW",
  UPDATE: "SYSTEM_CONFIG_UPDATE",
};

export const SYSTEM_ANNOUNCEMENT_PERMISSIONS = {
  CREATE: "SYSTEM_ANNOUNCEMENT_CREATE",
  UPDATE: "SYSTEM_ANNOUNCEMENT_UPDATE",
  PUBLISH: "SYSTEM_ANNOUNCEMENT_PUBLISH",
  CANCEL: "SYSTEM_ANNOUNCEMENT_CANCEL",
};

export function hasPermission(permissions, code) {
  return Array.isArray(permissions) && permissions.includes(code);
}

export function hasAnyPermission(permissions, codes) {
  return codes.some((code) => hasPermission(permissions, code));
}