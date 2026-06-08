import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { FeedToast } from "../../social/components/FeedToast";
import { useNotificationBadge } from "../context/NotificationBadgeContext";
import {
  NOTIFICATION_LIST_TABS,
  NOTIFICATION_NO_DEEP_LINK_MESSAGE,
} from "../constants/notificationConstants";
import { useNotifications } from "../hooks/useNotifications";
import { resolveNotificationDeepLink } from "../utils/notificationDeepLink";
import { NotificationBadgePill } from "./NotificationBadgePill";
import { NotificationDropdown } from "./NotificationDropdown";
import { NotificationPanel } from "./NotificationPanel";

function BellIcon() {
  return (
    <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" aria-hidden="true">
      <path d="M18 8a6 6 0 10-12 0c0 7-3 9-3 9h18s-3-2-3-9" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M13.73 21a2 2 0 01-3.46 0" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export function NotificationBell({ buttonClassName = "" }) {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuthSession();
  const { unreadCount, refetch: refetchBadge } = useNotificationBadge();
  const containerRef = useRef(null);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [panelOpen, setPanelOpen] = useState(false);
  const [panelTab, setPanelTab] = useState(NOTIFICATION_LIST_TABS.ALL);
  const [toastMessage, setToastMessage] = useState("");

  const dropdownNotifications = useNotifications({
    enabled: isAuthenticated && dropdownOpen && !panelOpen,
    tab: NOTIFICATION_LIST_TABS.ALL,
    pageSize: 8,
  });

  const panelNotifications = useNotifications({
    enabled: isAuthenticated && panelOpen,
    tab: panelTab,
    pageSize: 20,
  });

  useEffect(() => {
    if (!dropdownOpen) return undefined;

    const onPointerDown = (event) => {
      if (containerRef.current && !containerRef.current.contains(event.target)) {
        setDropdownOpen(false);
      }
    };

    document.addEventListener("pointerdown", onPointerDown);
    return () => document.removeEventListener("pointerdown", onPointerDown);
  }, [dropdownOpen]);

  const showToast = useCallback((message) => {
    setToastMessage(message);
  }, []);

  const handleToggleDropdown = useCallback(() => {
    if (!isAuthenticated) return;
    setDropdownOpen((open) => !open);
    setPanelOpen(false);
  }, [isAuthenticated]);

  const handleOpenItem = useCallback(
    async (notification) => {
      try {
        if (!notification.read) {
          if (panelOpen) {
            await panelNotifications.markAsRead(notification.id);
          } else {
            await dropdownNotifications.markAsRead(notification.id);
          }
        }

        const targetPath = resolveNotificationDeepLink(notification);
        setDropdownOpen(false);
        setPanelOpen(false);

        if (targetPath) {
          navigate(targetPath);
        } else {
          showToast(NOTIFICATION_NO_DEEP_LINK_MESSAGE);
        }
      } catch (error) {
        showToast(error?.message || "Khong the mo thong bao.");
      }
    },
    [dropdownNotifications, navigate, panelNotifications, panelOpen, showToast]
  );

  const handleDeleteItem = useCallback(
    async (notification) => {
      try {
        if (panelOpen) {
          await panelNotifications.removeNotification(notification.id);
        } else {
          await dropdownNotifications.removeNotification(notification.id);
        }
        showToast("Da xoa thong bao.");
      } catch (error) {
        showToast(error?.message || "Khong the xoa thong bao.");
      }
    },
    [dropdownNotifications, panelNotifications, panelOpen, showToast]
  );

  const handleDismissItem = useCallback(
    async (notification) => {
      try {
        if (panelOpen) {
          await panelNotifications.dismissAnnouncement(notification.id);
        } else {
          await dropdownNotifications.dismissAnnouncement(notification.id);
        }
        await refetchBadge();
        showToast("Da an thong bao he thong.");
      } catch (error) {
        showToast(error?.message || "Khong the an thong bao.");
      }
    },
    [dropdownNotifications, panelNotifications, panelOpen, refetchBadge, showToast]
  );

  const handleMarkAllRead = useCallback(async () => {
    try {
      if (panelOpen) {
        await panelNotifications.markAllAsRead();
      } else {
        await dropdownNotifications.markAllAsRead();
      }
      showToast("Da danh dau tat ca la da doc.");
    } catch (error) {
      showToast(error?.message || "Khong the danh dau da doc.");
    }
  }, [dropdownNotifications, panelNotifications, panelOpen, showToast]);

  const handleViewAll = useCallback(() => {
    setDropdownOpen(false);
    setPanelOpen(true);
    setPanelTab(NOTIFICATION_LIST_TABS.ALL);
  }, []);

  if (!isAuthenticated) {
    return null;
  }

  return (
    <>
      <div ref={containerRef} className="relative">
        <button
          type="button"
          onClick={handleToggleDropdown}
          className={[
            "relative flex h-9 w-9 items-center justify-center rounded-full text-header-nav transition-colors hover:bg-header-border/60",
            buttonClassName,
          ]
            .filter(Boolean)
            .join(" ")}
          aria-label="Thong bao"
          aria-expanded={dropdownOpen}
        >
          <BellIcon />
          <NotificationBadgePill count={unreadCount} className="absolute -right-0.5 -top-0.5" />
        </button>

        {dropdownOpen ? (
          <NotificationDropdown
            items={dropdownNotifications.items}
            status={dropdownNotifications.status}
            onOpenItem={handleOpenItem}
            onDeleteItem={handleDeleteItem}
            onDismissItem={handleDismissItem}
            onViewAll={handleViewAll}
            onMarkAllRead={handleMarkAllRead}
          />
        ) : null}
      </div>

      <NotificationPanel
        open={panelOpen}
        tab={panelTab}
        onTabChange={setPanelTab}
        items={panelNotifications.items}
        status={panelNotifications.status}
        errorMessage={panelNotifications.errorMessage}
        isLoadingMore={panelNotifications.isLoadingMore}
        hasNext={panelNotifications.meta.hasNext}
        onClose={() => setPanelOpen(false)}
        onReload={panelNotifications.reload}
        onLoadMore={panelNotifications.loadMore}
        onOpenItem={handleOpenItem}
        onDeleteItem={handleDeleteItem}
        onDismissItem={handleDismissItem}
        onMarkAllRead={handleMarkAllRead}
      />

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </>
  );
}
