import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { fetchUnreadNotificationCount } from "../api/notificationApi";
import { NOTIFICATION_POLL_INTERVAL_MS } from "../constants/notificationConstants";

const NotificationBadgeContext = createContext(null);

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401");
}

export function NotificationBadgeProvider({ children }) {
  const { isAuthenticated, user, showSessionExpired } = useAuthSession();
  const [unreadCount, setUnreadCount] = useState(0);

  const refetch = useCallback(async () => {
    if (!isAuthenticated) {
      setUnreadCount(0);
      return;
    }

    try {
      const data = await fetchUnreadNotificationCount();
      setUnreadCount(Number(data?.count ?? 0));
    } catch (error) {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        setUnreadCount(0);
      }
    }
  }, [isAuthenticated, showSessionExpired]);

  const setCount = useCallback((nextCount) => {
    setUnreadCount(Math.max(0, Number(nextCount) || 0));
  }, []);

  const decrementUnread = useCallback((amount = 1) => {
    setUnreadCount((current) => Math.max(0, current - amount));
  }, []);

  const clearUnread = useCallback(() => {
    setUnreadCount(0);
  }, []);

  useEffect(() => {
    if (!isAuthenticated) {
      setUnreadCount(0);
      return undefined;
    }

    refetch();
    const intervalId = window.setInterval(refetch, NOTIFICATION_POLL_INTERVAL_MS);
    return () => window.clearInterval(intervalId);
  }, [isAuthenticated, user?.id, refetch]);

  const value = useMemo(
    () => ({
      unreadCount,
      refetch,
      setCount,
      decrementUnread,
      clearUnread,
    }),
    [unreadCount, refetch, setCount, decrementUnread, clearUnread]
  );

  return (
    <NotificationBadgeContext.Provider value={value}>{children}</NotificationBadgeContext.Provider>
  );
}

export function useNotificationBadge() {
  const context = useContext(NotificationBadgeContext);
  if (!context) {
    throw new Error("useNotificationBadge must be used inside NotificationBadgeProvider");
  }
  return context;
}
