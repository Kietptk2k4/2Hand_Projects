import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { FeedToast } from "../../social/components/FeedToast";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { fetchUnreadNotificationCount, fetchUnreadNotifications } from "../api/notificationApi";
import {
  NOTIFICATION_POLL_INTERVAL_MS,
  NOTIFICATION_TOAST_AUTO_DISMISS_MS,
} from "../constants/notificationConstants";
import { mapNotificationListResponse } from "../utils/notificationMapper";
import { buildNewNotificationToastMessage } from "../utils/notificationToast";

const NotificationBadgeContext = createContext(null);
const SEEN_UNREAD_SEED_PAGE_SIZE = 50;
const NEW_UNREAD_FETCH_MAX_SIZE = 20;

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401");
}

async function seedSeenUnreadNotificationIds(seenNotificationIdsRef) {
  try {
    const raw = await fetchUnreadNotifications({ page: 0, size: SEEN_UNREAD_SEED_PAGE_SIZE });
    const mapped = mapNotificationListResponse(raw);
    mapped.items.forEach((item) => {
      if (item.id) {
        seenNotificationIdsRef.current.add(item.id);
      }
    });
  } catch {
    // Baseline seed is best-effort; polling will still work without it.
  }
}

export function NotificationBadgeProvider({ children }) {
  const { isAuthenticated, user, showSessionExpired } = useAuthSession();
  const [unreadCount, setUnreadCount] = useState(0);
  const [newNotificationToast, setNewNotificationToast] = useState("");

  const previousUnreadCountRef = useRef(null);
  const baselineEstablishedRef = useRef(false);
  const seenNotificationIdsRef = useRef(new Set());

  const resetTrackingState = useCallback(() => {
    previousUnreadCountRef.current = null;
    baselineEstablishedRef.current = false;
    seenNotificationIdsRef.current = new Set();
    setNewNotificationToast("");
  }, []);

  const showToastForNewNotifications = useCallback(async (previousCount, nextCount) => {
    const delta = nextCount - previousCount;
    if (delta <= 0) {
      return;
    }

    try {
      const raw = await fetchUnreadNotifications({
        page: 0,
        size: Math.min(delta + 5, NEW_UNREAD_FETCH_MAX_SIZE),
      });
      const mapped = mapNotificationListResponse(raw);
      const unseen = mapped.items.filter(
        (item) => item.id && !seenNotificationIdsRef.current.has(item.id)
      );

      unseen.forEach((item) => {
        seenNotificationIdsRef.current.add(item.id);
      });

      const newest = unseen.sort(
        (left, right) => new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime()
      )[0];

      setNewNotificationToast(buildNewNotificationToastMessage(delta, newest));
    } catch {
      setNewNotificationToast(buildNewNotificationToastMessage(delta, null));
    }
  }, []);

  const refetch = useCallback(async () => {
    if (!isAuthenticated) {
      setUnreadCount(0);
      resetTrackingState();
      return;
    }

    try {
      const data = await fetchUnreadNotificationCount();
      const nextCount = Math.max(0, Number(data?.count ?? 0));

      if (!baselineEstablishedRef.current) {
        await seedSeenUnreadNotificationIds(seenNotificationIdsRef);
        baselineEstablishedRef.current = true;
        previousUnreadCountRef.current = nextCount;
        setUnreadCount(nextCount);
        return;
      }

      const previousCount = previousUnreadCountRef.current ?? nextCount;
      if (nextCount > previousCount) {
        await showToastForNewNotifications(previousCount, nextCount);
      }

      previousUnreadCountRef.current = nextCount;
      setUnreadCount(nextCount);
    } catch (error) {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        setUnreadCount(0);
        resetTrackingState();
      }
    }
  }, [isAuthenticated, resetTrackingState, showSessionExpired, showToastForNewNotifications]);

  const syncTrackedCount = useCallback((nextCount) => {
    const safeCount = Math.max(0, Number(nextCount) || 0);
    previousUnreadCountRef.current = safeCount;
    return safeCount;
  }, []);

  const setCount = useCallback(
    (nextCount) => {
      setUnreadCount(syncTrackedCount(nextCount));
    },
    [syncTrackedCount]
  );

  const decrementUnread = useCallback(
    (amount = 1) => {
      setUnreadCount((current) => syncTrackedCount(current - amount));
    },
    [syncTrackedCount]
  );

  const clearUnread = useCallback(() => {
    setUnreadCount(syncTrackedCount(0));
  }, [syncTrackedCount]);

  const dismissNewNotificationToast = useCallback(() => {
    setNewNotificationToast("");
  }, []);

  useEffect(() => {
    if (!isAuthenticated) {
      setUnreadCount(0);
      resetTrackingState();
      return;
    }

    resetTrackingState();
  }, [isAuthenticated, user?.id, resetTrackingState]);

  useEffect(() => {
    if (!isAuthenticated) {
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
    <NotificationBadgeContext.Provider value={value}>
      {children}
      <FeedToast
        message={newNotificationToast}
        onDismiss={dismissNewNotificationToast}
        autoDismissMs={NOTIFICATION_TOAST_AUTO_DISMISS_MS}
      />
    </NotificationBadgeContext.Provider>
  );
}

export function useNotificationBadge() {
  const context = useContext(NotificationBadgeContext);
  if (!context) {
    throw new Error("useNotificationBadge must be used inside NotificationBadgeProvider");
  }
  return context;
}
