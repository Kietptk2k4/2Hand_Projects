import { useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { useNotificationBadge } from "../context/NotificationBadgeContext";
import { NOTIFICATION_TOAST_AUTO_DISMISS_MS } from "../constants/notificationConstants";
import { resolveNotificationDeepLink } from "../utils/notificationDeepLink";
import { NotificationToast } from "./NotificationToast";

/**
 * Renders the rich notification toast inside the router tree (needs useNavigate).
 */
export function NotificationToastHost() {
  const navigate = useNavigate();
  const { notificationToast, dismissNotificationToast } = useNotificationBadge();

  const handleOpen = useCallback(
    (notification) => {
      const targetPath = resolveNotificationDeepLink(notification);
      dismissNotificationToast();
      if (targetPath) {
        navigate(targetPath);
      }
    },
    [dismissNotificationToast, navigate]
  );

  return (
    <NotificationToast
      toast={notificationToast}
      onDismiss={dismissNotificationToast}
      onOpen={handleOpen}
      autoDismissMs={NOTIFICATION_TOAST_AUTO_DISMISS_MS}
    />
  );
}
