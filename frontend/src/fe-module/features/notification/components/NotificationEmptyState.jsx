export function NotificationEmptyState({ unreadOnly = false }) {
  return (
    <div className="flex flex-col items-center justify-center px-6 py-12 text-center">
      <span className="material-symbols-outlined mb-3 text-4xl text-on-surface-variant" aria-hidden="true">
        notifications_off
      </span>
      <p className="text-sm font-medium text-on-surface">
        {unreadOnly ? "Khong co thong bao chua doc" : "Chua co thong bao"}
      </p>
      <p className="mt-1 text-xs text-on-surface-variant">
        {unreadOnly
          ? "Ban da xem het cac thong bao moi."
          : "Hoat dong moi se hien thi tai day."}
      </p>
    </div>
  );
}
