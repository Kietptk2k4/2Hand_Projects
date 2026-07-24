import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { fetchUnreadNotificationCount, fetchUnreadNotifications } from "../api/notificationApi";
import { NOTIFICATION_POLL_INTERVAL_MS } from "../constants/notificationConstants";
import { mapNotificationListResponse } from "../utils/notificationMapper";
import { buildNewNotificationToastPayload } from "../utils/notificationToast";

const NotificationBadgeContext = createContext(null);
const SEEN_UNREAD_SEED_PAGE_SIZE = 50;
const NEW_UNREAD_FETCH_MAX_SIZE = 20;

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401");
}

function readUnreadCount(data) {
  const raw = data?.count ?? data?.unread_count ?? data?.unreadCount;
  const nextCount = Number(raw ?? 0);
  return Number.isFinite(nextCount) ? Math.max(0, nextCount) : 0;
}

function isDocumentVisible() {
  if (typeof document === "undefined") {
    return true;
  }
  return document.visibilityState === "visible";
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
  const [notificationToast, setNotificationToast] = useState(null);

  const previousUnreadCountRef = useRef(null);
  const baselineEstablishedRef = useRef(false);
  const seenNotificationIdsRef = useRef(new Set());
  const refetchRef = useRef(null);

  const resetTrackingState = useCallback(() => {
    previousUnreadCountRef.current = null;
    baselineEstablishedRef.current = false;
    seenNotificationIdsRef.current = new Set();
    setNotificationToast(null);
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

      setNotificationToast(buildNewNotificationToastPayload(delta, newest ?? null));
    } catch {
      setNotificationToast(buildNewNotificationToastPayload(delta, null));
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
      const nextCount = readUnreadCount(data);

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

  refetchRef.current = refetch;

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

  const dismissNotificationToast = useCallback(() => {
    setNotificationToast(null);
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

    let intervalId = null;

    const clearPoll = () => {
      if (intervalId != null) {
        window.clearInterval(intervalId);
        intervalId = null;
      }
    };

    const startPoll = () => {
      clearPoll();
      if (!isDocumentVisible()) {
        return;
      }
      intervalId = window.setInterval(() => {
        void refetchRef.current?.();
      }, NOTIFICATION_POLL_INTERVAL_MS);
    };

    const runVisibleCycle = () => {
      void refetchRef.current?.();
      startPoll();
    };

    const onVisibilityChange = () => {
      if (!isDocumentVisible()) {
        clearPoll();
        return;
      }
      // Resume: refetch immediately, then restart interval. Do not reset baseline/seen ids.
      runVisibleCycle();
    };

    if (isDocumentVisible()) {
      runVisibleCycle();
    }

    document.addEventListener("visibilitychange", onVisibilityChange);
    return () => {
      clearPoll();
      document.removeEventListener("visibilitychange", onVisibilityChange);
    };
  }, [isAuthenticated, user?.id]);

  const value = useMemo(
    () => ({
      unreadCount,
      refetch,
      setCount,
      decrementUnread,
      clearUnread,
      notificationToast,
      dismissNotificationToast,
    }),
    [
      unreadCount,
      refetch,
      setCount,
      decrementUnread,
      clearUnread,
      notificationToast,
      dismissNotificationToast,
    ]
  );

  return (
    <NotificationBadgeContext.Provider value={value}>
      {children}
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
