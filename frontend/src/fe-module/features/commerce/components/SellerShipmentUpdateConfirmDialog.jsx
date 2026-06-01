export function SellerShipmentUpdateConfirmDialog({
  open,
  title,
  description,
  isProcessing,
  errorMessage,
  confirmLabel,
  onCancel,
  onConfirm,
}) {
  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-inverse-surface/40 p-4"
      role="dialog"
      aria-modal="true"
    >
      <div className="w-full max-w-md rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-lg">
        <h2 className="text-headline-sm font-semibold text-on-surface">{title}</h2>
        <p className="mt-2 text-body-sm text-on-surface-variant">{description}</p>
        {errorMessage ? <p className="mt-3 text-sm text-error">{errorMessage}</p> : null}
        <div className="mt-6 flex justify-end gap-3">
          <button
            type="button"
            onClick={onCancel}
            disabled={isProcessing}
            className="rounded-lg px-4 py-2 text-label-md text-on-surface-variant hover:bg-surface-container-low disabled:opacity-50"
          >
            Hủy
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={isProcessing}
            className="rounded-lg bg-primary px-4 py-2 text-label-md font-medium text-on-primary hover:bg-[#0050cb] disabled:opacity-50"
          >
            {isProcessing ? "Đang xử lý..." : confirmLabel || "Xác nhận"}
          </button>
        </div>
      </div>
    </div>
  );
}
