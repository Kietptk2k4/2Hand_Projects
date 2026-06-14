import { useMemo } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import {
  CATALOG_PERMISSIONS,
  hasCatalogPermission,
} from "../constants/catalogPermissions.js";

export function useCatalogPermissions() {
  const { user } = useAuthSession();
  const permissions = user?.permissions || [];

  return useMemo(
    () => ({
      canRead: hasCatalogPermission(permissions, CATALOG_PERMISSIONS.READ),
      canWrite: hasCatalogPermission(permissions, CATALOG_PERMISSIONS.WRITE),
    }),
    [permissions],
  );
}
