const SESSION_KIND_KEY = "twohands_session_kind";
export const ADMIN_SESSION_KIND = "admin";
export const USER_SESSION_KIND = "user";

const ADMIN_ROLES = new Set(["ADMIN", "MODERATOR", "SUPPORT", "SUPER_ADMIN"]);

export function readSessionKind() {
  return localStorage.getItem(SESSION_KIND_KEY);
}

export function persistSessionKind(kind) {
  if (kind) {
    localStorage.setItem(SESSION_KIND_KEY, kind);
  } else {
    localStorage.removeItem(SESSION_KIND_KEY);
  }
}

export function hasAdminPortalAccess({ roles = [], permissions = [] } = {}) {
  if (permissions.includes("ADMIN_ACCESS")) {
    return true;
  }
  return roles.some((role) => ADMIN_ROLES.has(role));
}

export function isAdminSession() {
  return readSessionKind() === ADMIN_SESSION_KIND;
}

export function buildAdminUserFromLoginData(loginData) {
  const user = loginData?.user || {};
  return {
    ...user,
    roles: loginData?.roles || [],
    permissions: loginData?.permissions || [],
    sessionKind: ADMIN_SESSION_KIND,
  };
}
