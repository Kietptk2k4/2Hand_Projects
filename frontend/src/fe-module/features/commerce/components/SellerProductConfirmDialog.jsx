import { CONFIRM_ACTIONS } from "../constants/sellerProductConstants";

export function SellerProductConfirmDialog({
  pending,
  isActing,
  errorMessage,
  onCancel,
  onConfirm,
}) {
  if (!pending) return null;

  const config = CONFIRM_ACTIONS[pending.action];
  if (!config) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-inverse-surface/40 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="seller-product-confirm-title"
    >
      <div className="w-full max-w-md rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-lg">
        <h2 id="seller-product-confirm-title" className="text-headline-sm font-semibold text-on-surface">
          {config.title}
        </h2>
        <p className="mt-2 text-body-sm text-on-surface-variant">{config.message}</p>
        <p className="mt-2 text-body-sm font-medium text-on-surface line-clamp-2">
          {pending.product?.title}
        </p>

        {errorMessage ? (
          <p className="mt-3 text-sm text-error">{errorMessage}</p>
        ) : null}

        <div className="mt-6 flex justify-end gap-3">
          <button
            type="button"
            onClick={onCancel}
            disabled={isActing}
            className="rounded-lg px-4 py-2 text-label-md text-on-surface-variant hover:bg-surface-container-low disabled:opacity-50"
          >
            Hủy
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={isActing}
            className="rounded-lg bg-primary px-4 py-2 text-label-md font-medium text-on-primary hover:bg-[#0050cb] disabled:opacity-50"
          >
            {isActing ? "Đang xử lý..." : config.confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
