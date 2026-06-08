import { NotificationEmptyState } from "./NotificationEmptyState";
import { NotificationListItem } from "./NotificationListItem";
import { NOTIFICATION_LIST_TABS } from "../constants/notificationConstants";

export function NotificationPanel({
  open,
  tab,
  onTabChange,
  items,
  status,
  errorMessage,
  isLoadingMore,
  hasNext,
  onClose,
  onReload,
  onLoadMore,
  onOpenItem,
  onDeleteItem,
  onDismissItem,
  onMarkAllRead,
}) {
  if (!open) return null;

  const hasUnread = items.some((item) => !item.read);

  return (
    <div className="fixed inset-0 z-[70]">
      <button
        type="button"
        className="absolute inset-0 bg-black/30"
        aria-label="Dong bang thong bao"
        onClick={onClose}
      />

      <aside className="absolute right-0 top-0 flex h-full w-full max-w-md flex-col border-l border-outline-variant bg-surface-container-lowest shadow-2xl">
        <div className="flex items-center justify-between border-b border-outline-variant px-4 py-4">
          <div>
            <h2 className="text-lg font-semibold text-on-surface">Thong bao</h2>
            <p className="text-xs text-on-surface-variant">Quan ly tat ca thong bao cua ban</p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-full p-2 text-on-surface-variant hover:bg-surface-container-low hover:text-on-surface"
            aria-label="Dong"
          >
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>

        <div className="flex items-center justify-between gap-3 border-b border-outline-variant px-4 py-3">
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => onTabChange(NOTIFICATION_LIST_TABS.ALL)}
              className={[
                "rounded-full px-3 py-1.5 text-sm font-medium transition",
                tab === NOTIFICATION_LIST_TABS.ALL
                  ? "bg-primary text-on-primary"
                  : "bg-surface-container-low text-on-surface-variant hover:text-on-surface",
              ].join(" ")}
            >
              Tat ca
            </button>
            <button
              type="button"
              onClick={() => onTabChange(NOTIFICATION_LIST_TABS.UNREAD)}
              className={[
                "rounded-full px-3 py-1.5 text-sm font-medium transition",
                tab === NOTIFICATION_LIST_TABS.UNREAD
                  ? "bg-primary text-on-primary"
                  : "bg-surface-container-low text-on-surface-variant hover:text-on-surface",
              ].join(" ")}
            >
              Chua doc
            </button>
          </div>

          {hasUnread ? (
            <button
              type="button"
              onClick={onMarkAllRead}
              className="text-xs font-medium text-primary hover:underline"
            >
              Doc tat ca
            </button>
          ) : null}
        </div>

        <div className="min-h-0 flex-1 overflow-y-auto">
          {status === "loading" && items.length === 0 ? (
            <div className="px-4 py-12 text-center text-sm text-on-surface-variant">Dang tai...</div>
          ) : null}

          {status === "error" ? (
            <div className="px-4 py-8 text-center">
              <p className="text-sm text-error">{errorMessage}</p>
              <button
                type="button"
                onClick={onReload}
                className="mt-3 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white"
              >
                Thu lai
              </button>
            </div>
          ) : null}

          {status !== "loading" && status !== "error" && items.length === 0 ? (
            <NotificationEmptyState unreadOnly={tab === NOTIFICATION_LIST_TABS.UNREAD} />
          ) : null}

          {items.map((item) => (
            <NotificationListItem
              key={item.id}
              notification={item}
              onOpen={onOpenItem}
              onDelete={onDeleteItem}
              onDismiss={onDismissItem}
            />
          ))}

          {hasNext ? (
            <div className="px-4 py-4">
              <button
                type="button"
                onClick={onLoadMore}
                disabled={isLoadingMore}
                className="w-full rounded-lg border border-outline-variant bg-white px-4 py-2 text-sm font-medium text-on-surface hover:bg-surface-container-low disabled:opacity-60"
              >
                {isLoadingMore ? "Dang tai..." : "Tai them"}
              </button>
            </div>
          ) : null}
        </div>
      </aside>
    </div>
  );
}
