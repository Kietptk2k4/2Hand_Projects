import { useCallback, useEffect, useState } from "react";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { fetchNotifications } from "../api/notificationApi";
import { mapNotificationListResponse } from "../utils/notificationMapper";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401");
}

export function usePinnedAnnouncements({ enabled = true } = {}) {
  const { isAuthenticated, showSessionExpired } = useAuthSession();
  const [announcements, setAnnouncements] = useState([]);
  const [status, setStatus] = useState("idle");

  const reload = useCallback(async () => {
    if (!enabled || !isAuthenticated) {
      setAnnouncements([]);
      setStatus("idle");
      return;
    }

    setStatus("loading");

    try {
      const raw = await fetchNotifications({ page: 0, size: 50 });
      const mapped = mapNotificationListResponse(raw);
      const pinned = mapped.items.filter((item) => item.isPinnedAnnouncement);
      setAnnouncements(pinned);
      setStatus("ready");
    } catch (error) {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
      }
      setAnnouncements([]);
      setStatus("error");
    }
  }, [enabled, isAuthenticated, showSessionExpired]);

  useEffect(() => {
    reload();
  }, [reload]);

  const dismissLocal = useCallback((notificationId) => {
    setAnnouncements((current) => current.filter((item) => item.id !== notificationId));
  }, []);

  return {
    announcements,
    status,
    reload,
    dismissLocal,
  };
}
