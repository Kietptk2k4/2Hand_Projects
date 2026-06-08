import { useState } from "react";
import { formatRelativeTime } from "../utils/notificationDateTime";
import { hasNotificationDeepLink } from "../utils/notificationDeepLink";

function NotificationTypeIcon({ type }) {
  const iconMap = {
    POST_LIKED: "favorite",
    USER_FOLLOWED: "person_add",
    COMMENT_CREATED: "chat_bubble",
    COMMENT_REPLIED: "reply",
    COMMENT_LIKED: "thumb_up",
    ORDER_CREATED: "shopping_bag",
    PAYMENT_SUCCESS: "payments",
    PAYMENT_FAILED: "error",
    SHIPMENT_CREATED: "local_shipping",
    SHIPMENT_SHIPPED: "local_shipping",
    SHIPMENT_DELIVERED: "inventory_2",
    ORDER_COMPLETED: "check_circle",
    REVIEW_REMINDER: "rate_review",
    USER_SUSPENDED: "gavel",
    USER_RESTRICTED: "shield",
    PRODUCT_REMOVED: "inventory",
    REVIEW_HIDDEN: "visibility_off",
    SHOP_SUSPENDED: "store",
    SHOP_CLOSED: "storefront",
    SYSTEM_ANNOUNCEMENT_SENT: "campaign",
  };

  const icon = iconMap[type] || "notifications";

  return (
    <span
      className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-surface-container-low text-primary"
      aria-hidden="true"
    >
      <span className="material-symbols-outlined text-xl">{icon}</span>
    </span>
  );
}

export function NotificationListItem({
  notification,
  compact = false,
  onOpen,
  onDelete,
  onDismiss,
}) {
  const [menuOpen, setMenuOpen] = useState(false);
  const clickable = hasNotificationDeepLink(notification);

  const handleOpen = () => {
    onOpen?.(notification);
  };

  const handleDelete = async (event) => {
    event.stopPropagation();
    setMenuOpen(false);
    await onDelete?.(notification);
  };

  const handleDismiss = async (event) => {
    event.stopPropagation();
    setMenuOpen(false);
    await onDismiss?.(notification);
  };

  return (
    <div
      className={[
        "group relative flex w-full gap-3 border-b border-outline-variant/60 text-left transition-colors",
        clickable ? "hover:bg-surface-container-low" : "hover:bg-surface-container-low/60",
        compact ? "px-3 py-3" : "px-4 py-4",
        notification.read ? "bg-transparent" : "bg-primary/5",
      ].join(" ")}
    >
      <button
        type="button"
        onClick={handleOpen}
        className={[
          "flex min-w-0 flex-1 items-start gap-3 text-left transition-colors",
          clickable ? "cursor-pointer hover:text-primary" : "cursor-default",
        ].join(" ")}
        aria-label={clickable ? "Mo thong bao lien quan" : "Thong bao khong co lien ket"}
      >
        <NotificationTypeIcon type={notification.type} />
        <span className="min-w-0 flex-1">
          <span className="flex items-start justify-between gap-2">
            <span
              className={[
                "line-clamp-1 text-sm font-semibold",
                clickable ? "text-on-surface group-hover:text-primary" : "text-on-surface",
              ].join(" ")}
            >
              {notification.title}
            </span>
            {!notification.read ? (
              <span className="mt-1 h-2 w-2 shrink-0 rounded-full bg-primary" aria-label="Chua doc" />
            ) : null}
          </span>
          <span className={`mt-0.5 block text-on-surface-variant ${compact ? "line-clamp-2 text-xs" : "line-clamp-3 text-sm"}`}>
            {notification.content}
          </span>
          <span className="mt-1 block text-xs text-on-surface-variant">
            {formatRelativeTime(notification.createdAt)}
          </span>
        </span>
      </button>

      <div className="relative shrink-0">
        <button
          type="button"
          onClick={(event) => {
            event.stopPropagation();
            setMenuOpen((open) => !open);
          }}
          className="rounded-full p-1.5 text-on-surface-variant opacity-0 transition hover:bg-surface-container-low hover:text-on-surface group-hover:opacity-100"
          aria-label="Tuy chon thong bao"
        >
          <span className="material-symbols-outlined text-lg">more_vert</span>
        </button>

        {menuOpen ? (
          <div className="absolute right-0 top-full z-10 mt-1 min-w-[140px] overflow-hidden rounded-lg border border-outline-variant bg-white shadow-lg">
            {notification.isDismissibleAnnouncement ? (
              <button
                type="button"
                onClick={handleDismiss}
                className="block w-full px-3 py-2 text-left text-sm text-on-surface hover:bg-surface-container-low"
              >
                An thong bao
              </button>
            ) : null}
            <button
              type="button"
              onClick={handleDelete}
              className="block w-full px-3 py-2 text-left text-sm text-error hover:bg-red-50"
            >
              Xoa
            </button>
          </div>
        ) : null}
      </div>
    </div>
  );
}
