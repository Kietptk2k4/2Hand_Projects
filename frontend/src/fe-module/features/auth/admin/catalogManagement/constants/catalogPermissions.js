export const CATALOG_PERMISSIONS = {
  READ: "CATALOG_READ",
  WRITE: "CATALOG_WRITE",
};

export function hasCatalogPermission(permissions, code) {
  return Array.isArray(permissions) && permissions.includes(code);
}
