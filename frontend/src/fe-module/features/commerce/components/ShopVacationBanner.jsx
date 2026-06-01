export function ShopVacationBanner({ message }) {
  if (!message) return null;

  return (
    <div
      className="mb-8 flex gap-3 rounded-lg border border-primary/20 bg-surface-container-low p-4"
      role="status"
    >
      <span
        className="material-symbols-outlined shrink-0 text-primary"
        aria-hidden="true"
      >
        flight
      </span>
      <div>
        <p className="text-label-md font-semibold text-on-surface">Cửa hàng đang trong kỳ nghỉ</p>
        <p className="mt-1 text-body-sm text-on-surface-variant">{message}</p>
      </div>
    </div>
  );
}
