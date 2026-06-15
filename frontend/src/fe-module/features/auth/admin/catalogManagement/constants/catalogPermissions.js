export const CATALOG_PERMISSIONS = {
  READ: "CATALOG_READ",
  WRITE: "CATALOG_WRITE",
};

export function hasCatalogPermission(permissions, code) {
  return Array.isArray(permissions) && permissions.includes(code);
}

export function isCatalogForbiddenError(error) {
  const code = String(error?.code ?? "");
  return code === "403" || code.includes("FORBIDDEN");
}
