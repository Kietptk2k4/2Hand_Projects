import { useMemo } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import {
  SYSTEM_CONFIG_PERMISSIONS,
  hasPermission,
} from "../constants/systemOperationsPermissions.js";

export function useSystemConfigPermissions() {
  const { user } = useAuthSession();
  const permissions = user?.permissions || [];

  return useMemo(
    () => ({
      permissions,
      canViewConfigs: hasPermission(permissions, SYSTEM_CONFIG_PERMISSIONS.VIEW),
      canUpdateConfigs: hasPermission(permissions, SYSTEM_CONFIG_PERMISSIONS.UPDATE),
    }),
    [permissions],
  );
}