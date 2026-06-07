import { useMemo } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import {
  INVESTIGATION_PERMISSIONS,
  hasInvestigationPermission,
} from "../constants/investigationPermissions.js";

export function useInvestigationPermissions() {
  const { user } = useAuthSession();
  const permissions = user?.permissions || [];

  return useMemo(
    () => ({
      permissions,
      canReadProfile: hasInvestigationPermission(
        permissions,
        INVESTIGATION_PERMISSIONS.READ_PROFILE,
      ),
      canReadEnforcement: hasInvestigationPermission(
        permissions,
        INVESTIGATION_PERMISSIONS.READ_ENFORCEMENT,
      ),
      canSuspend: hasInvestigationPermission(permissions, INVESTIGATION_PERMISSIONS.SUSPEND),
      canBan: hasInvestigationPermission(permissions, INVESTIGATION_PERMISSIONS.BAN),
      canRestrict: hasInvestigationPermission(permissions, INVESTIGATION_PERMISSIONS.RESTRICT),
      canRevoke: hasInvestigationPermission(permissions, INVESTIGATION_PERMISSIONS.REVOKE),
      canRevokeAdminSession: hasInvestigationPermission(
        permissions,
        INVESTIGATION_PERMISSIONS.REVOKE_ADMIN_SESSION,
      ),
    }),
    [permissions],
  );
}
