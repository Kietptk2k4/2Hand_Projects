import { useEffect } from "react";
import { NOTIFICATION_EVENT_ICONS } from "../constants/notificationConstants";
import { formatRelativeTime } from "../utils/notificationDateTime";
import { hasNotificationDeepLink } from "../utils/notificationDeepLink";

function ToastTypeIcon({ type }) {
  const icon = NOTIFICATION_EVENT_ICONS[type] || "notifications";

  return (
    <span
      className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-primary/15 to-primary/5 text-primary ring-1 ring-primary/10"
      aria-hidden="true"
    >
      <span className="material-symbols-outlined text-[22px]">{icon}</span>
    </span>
  );
}

/**
 * Instagram-lite notification toast: top card with icon, title, preview, time.
 * Kept separate from FeedToast (action feedback stays bottom/simple).
 */
export function NotificationToast({
  toast,
  onDismiss,
  onOpen,
  autoDismissMs,
}) {
  useEffect(() => {
    if (!toast || !autoDismissMs) {
      return undefined;
    }

    const timeoutId = window.setTimeout(onDismiss, autoDismissMs);
    return () => window.clearTimeout(timeoutId);
  }, [autoDismissMs, onDismiss, toast]);

  if (!toast) {
    return null;
  }

  const notification = toast.notification;
  const clickable = Boolean(notification && hasNotificationDeepLink(notification));
  const relativeTime = formatRelativeTime(notification?.createdAt);
  const typeLabel = notification?.typeLabel || "";

  const handleOpen = () => {
    if (!clickable) {
      return;
    }
    onOpen?.(notification);
  };

  const handleDismiss = (event) => {
    event.stopPropagation();
    onDismiss?.();
  };

  const handleKeyDown = (event) => {
    if (!clickable) {
      return;
    }
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      handleOpen();
    }
  };

  return (
    <div
      className="pointer-events-none fixed bottom-0 right-0 z-[70] flex justify-end p-3 pb-[max(0.75rem,env(safe-area-inset-bottom))] pr-[max(0.75rem,env(safe-area-inset-right))] sm:p-4"
      role="status"
      aria-live="polite"
    >
      <div
        className={[
          "pointer-events-auto w-full max-w-sm origin-bottom-right animate-[notification-toast-in_320ms_cubic-bezier(0.22,1,0.36,1)_both]",
          "rounded-2xl border border-white/60 bg-white/95 shadow-[0_12px_40px_-12px_rgba(15,23,42,0.35)]",
          "ring-1 ring-black/5 backdrop-blur-md",
        ].join(" ")}
      >
        <div
          role={clickable ? "button" : undefined}
          tabIndex={clickable ? 0 : undefined}
          onClick={handleOpen}
          onKeyDown={handleKeyDown}
          className={[
            "flex w-full items-start gap-3 px-3.5 py-3 text-left",
            clickable ? "cursor-pointer" : "cursor-default",
          ].join(" ")}
          aria-label={clickable ? "Mở thông báo" : undefined}
        >
          <ToastTypeIcon type={notification?.type} />

          <div className="min-w-0 flex-1 pt-0.5">
            <div className="flex items-start justify-between gap-2">
              <p className="line-clamp-1 text-sm font-semibold tracking-tight text-on-surface">
                {toast.title}
              </p>
              <button
                type="button"
                onClick={handleDismiss}
                className="shrink-0 rounded-full p-0.5 text-on-surface-variant/70 transition hover:bg-surface-container-low hover:text-on-surface"
                aria-label="Đóng thông báo"
              >
                <span className="material-symbols-outlined text-lg leading-none">close</span>
              </button>
            </div>

            {toast.content ? (
              <p className="mt-0.5 line-clamp-2 text-xs leading-relaxed text-on-surface-variant">
                {toast.content}
              </p>
            ) : null}

            <div className="mt-1.5 flex flex-wrap items-center gap-x-2 gap-y-0.5 text-[11px] text-on-surface-variant/80">
              {relativeTime ? <span>{relativeTime}</span> : <span>Vừa xong</span>}
              {typeLabel ? (
                <>
                  <span aria-hidden="true">·</span>
                  <span className="line-clamp-1">{typeLabel}</span>
                </>
              ) : null}
              {clickable ? (
                <>
                  <span aria-hidden="true">·</span>
                  <span className="font-medium text-primary">Xem</span>
                </>
              ) : null}
            </div>
          </div>
        </div>
      </div>

      <style>{`
        @keyframes notification-toast-in {
          from {
            opacity: 0;
            transform: translateY(12px) scale(0.98);
          }
          to {
            opacity: 1;
            transform: translateY(0) scale(1);
          }
        }
      `}</style>
    </div>
  );
}
