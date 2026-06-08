import { NotificationEmptyState } from "./NotificationEmptyState";
import { NotificationListItem } from "./NotificationListItem";

export function NotificationDropdown({
  items,
  status,
  onOpenItem,
  onDeleteItem,
  onDismissItem,
  onViewAll,
  onMarkAllRead,
}) {
  const previewItems = items.slice(0, 8);
  const hasUnread = items.some((item) => !item.read);

  return (
    <div className="absolute right-0 top-full z-50 mt-2 w-[min(100vw-2rem,380px)] overflow-hidden rounded-xl border border-header-border bg-white shadow-xl">
      <div className="flex items-center justify-between border-b border-outline-variant px-4 py-3">
        <h2 className="text-sm font-semibold text-on-surface">Thong bao</h2>
        {hasUnread ? (
          <button
            type="button"
            onClick={onMarkAllRead}
            className="text-xs font-medium text-primary hover:underline"
          >
            Danh dau da doc
          </button>
        ) : null}
      </div>

      <div className="max-h-[420px] overflow-y-auto">
        {status === "loading" && previewItems.length === 0 ? (
          <div className="px-4 py-8 text-center text-sm text-on-surface-variant">Dang tai...</div>
        ) : null}

        {status !== "loading" && previewItems.length === 0 ? <NotificationEmptyState /> : null}

        {previewItems.map((item) => (
          <NotificationListItem
            key={item.id}
            notification={item}
            compact
            onOpen={onOpenItem}
            onDelete={onDeleteItem}
            onDismiss={onDismissItem}
          />
        ))}
      </div>

      <div className="border-t border-outline-variant px-4 py-3">
        <button
          type="button"
          onClick={onViewAll}
          className="w-full rounded-lg py-2 text-sm font-medium text-primary hover:bg-primary/5"
        >
          Xem tat ca
        </button>
      </div>
    </div>
  );
}
