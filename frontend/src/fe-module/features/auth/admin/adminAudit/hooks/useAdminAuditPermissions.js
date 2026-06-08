import { useMemo } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import {
  ADMIN_AUDIT_PERMISSIONS,
  hasAdminAuditPermission,
} from "../constants/adminAuditPermissions.js";

export function useAdminAuditPermissions() {
  const { user } = useAuthSession();
  const permissions = user?.permissions || [];

  return useMemo(
    () => ({
      permissions,
      canViewAudit: hasAdminAuditPermission(permissions, ADMIN_AUDIT_PERMISSIONS.VIEW),
      canReadAudit: hasAdminAuditPermission(permissions, ADMIN_AUDIT_PERMISSIONS.READ),
    }),
    [permissions],
  );
}