import { useCallback, useEffect, useRef, useState } from "react";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import {
  deleteNotification,
  dismissAnnouncementNotification,
  fetchNotifications,
  fetchUnreadNotifications,
  markAllNotificationsAsRead,
  markNotificationAsRead,
} from "../api/notificationApi";
import { NOTIFICATION_LIST_TABS } from "../constants/notificationConstants";
import { mapNotificationListResponse } from "../utils/notificationMapper";
import { useNotificationBadge } from "../context/NotificationBadgeContext";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401");
}

export function useNotifications({ enabled = true, tab = NOTIFICATION_LIST_TABS.ALL, pageSize = 20 } = {}) {
  const { isAuthenticated, showSessionExpired } = useAuthSession();
  const { refetch: refetchBadge, decrementUnread, clearUnread } = useNotificationBadge();
  const [items, setItems] = useState([]);
  const [meta, setMeta] = useState({
    page: 0,
    size: pageSize,
    totalElements: 0,
    totalPages: 0,
    hasNext: false,
  });
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const requestIdRef = useRef(0);

  const fetchPage = useCallback(
    async ({ page = 0, append = false } = {}) => {
      if (!enabled || !isAuthenticated) {
        setItems([]);
        setStatus("idle");
        return;
      }

      const requestId = requestIdRef.current + 1;
      requestIdRef.current = requestId;

      if (append) {
        setIsLoadingMore(true);
      } else {
        setStatus("loading");
        setErrorMessage("");
      }

      try {
        const fetcher =
          tab === NOTIFICATION_LIST_TABS.UNREAD ? fetchUnreadNotifications : fetchNotifications;
        const raw = await fetcher({ page, size: pageSize });
        if (requestId !== requestIdRef.current) return;

        const mapped = mapNotificationListResponse(raw);
        setItems((current) => (append ? [...current, ...mapped.items] : mapped.items));
        setMeta(mapped.meta);
        setStatus("ready");
      } catch (error) {
        if (requestId !== requestIdRef.current) return;
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
        } else {
          setErrorMessage(error?.message || "Khong tai duoc thong bao.");
        }
        setStatus("error");
      } finally {
        if (requestId === requestIdRef.current) {
          setIsLoadingMore(false);
        }
      }
    },
    [enabled, isAuthenticated, pageSize, showSessionExpired, tab]
  );

  const reload = useCallback(() => fetchPage({ page: 0, append: false }), [fetchPage]);

  const loadMore = useCallback(() => {
    if (!meta.hasNext || isLoadingMore || status === "loading") return;
    fetchPage({ page: meta.page + 1, append: true });
  }, [fetchPage, isLoadingMore, meta.hasNext, meta.page, status]);

  useEffect(() => {
    reload();
  }, [reload, tab, enabled, isAuthenticated]);

  const markAsRead = useCallback(
    async (notificationId) => {
      const target = items.find((item) => item.id === notificationId);
      if (!target || target.read) return;

      setItems((current) =>
        current.map((item) => (item.id === notificationId ? { ...item, read: true } : item))
      );
      decrementUnread(1);

      try {
        await markNotificationAsRead(notificationId);
        await refetchBadge();
      } catch (error) {
        setItems((current) =>
          current.map((item) => (item.id === notificationId ? { ...item, read: false } : item))
        );
        if (target && !target.read) {
          decrementUnread(-1);
        }
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
        }
        throw error;
      }
    },
    [decrementUnread, items, refetchBadge, showSessionExpired]
  );

  const markAllAsRead = useCallback(async () => {
    const previousItems = items;
    setItems((current) => current.map((item) => ({ ...item, read: true })));
    clearUnread();

    try {
      await markAllNotificationsAsRead();
      await refetchBadge();
      if (tab === NOTIFICATION_LIST_TABS.UNREAD) {
        setItems([]);
      }
    } catch (error) {
      setItems(previousItems);
      await refetchBadge();
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
      }
      throw error;
    }
  }, [clearUnread, items, refetchBadge, showSessionExpired, tab]);

  const removeNotification = useCallback(
    async (notificationId) => {
      const previousItems = items;
      const removed = items.find((item) => item.id === notificationId);
      setItems((current) => current.filter((item) => item.id !== notificationId));

      if (removed && !removed.read) {
        decrementUnread(1);
      }

      try {
        await deleteNotification(notificationId);
        await refetchBadge();
      } catch (error) {
        setItems(previousItems);
        await refetchBadge();
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
        }
        throw error;
      }
    },
    [decrementUnread, items, refetchBadge, showSessionExpired]
  );

  const dismissAnnouncement = useCallback(
    async (notificationId) => {
      const previousItems = items;
      const removed = items.find((item) => item.id === notificationId);
      setItems((current) => current.filter((item) => item.id !== notificationId));

      if (removed && !removed.read) {
        decrementUnread(1);
      }

      try {
        await dismissAnnouncementNotification(notificationId);
        await refetchBadge();
      } catch (error) {
        setItems(previousItems);
        await refetchBadge();
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
        }
        throw error;
      }
    },
    [decrementUnread, items, refetchBadge, showSessionExpired]
  );

  return {
    items,
    meta,
    status,
    errorMessage,
    isLoadingMore,
    reload,
    loadMore,
    markAsRead,
    markAllAsRead,
    removeNotification,
    dismissAnnouncement,
  };
}
