import { useMemo } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import {
  SYSTEM_ANNOUNCEMENT_PERMISSIONS,
  hasAnyPermission,
  hasPermission,
} from "../constants/systemOperationsPermissions.js";

const LIST_PERMISSIONS = [
  SYSTEM_ANNOUNCEMENT_PERMISSIONS.CREATE,
  SYSTEM_ANNOUNCEMENT_PERMISSIONS.UPDATE,
  SYSTEM_ANNOUNCEMENT_PERMISSIONS.PUBLISH,
  SYSTEM_ANNOUNCEMENT_PERMISSIONS.CANCEL,
];

export function useSystemAnnouncementPermissions() {
  const { user } = useAuthSession();
  const permissions = user?.permissions || [];

  return useMemo(
    () => ({
      permissions,
      canViewAnnouncements: hasAnyPermission(permissions, LIST_PERMISSIONS),
      canCreateAnnouncements: hasPermission(permissions, SYSTEM_ANNOUNCEMENT_PERMISSIONS.CREATE),
      canUpdateAnnouncements: hasPermission(permissions, SYSTEM_ANNOUNCEMENT_PERMISSIONS.UPDATE),
      canPublishAnnouncements: hasPermission(permissions, SYSTEM_ANNOUNCEMENT_PERMISSIONS.PUBLISH),
      canCancelAnnouncements: hasPermission(permissions, SYSTEM_ANNOUNCEMENT_PERMISSIONS.CANCEL),
    }),
    [permissions],
  );
}