import { useEffect, useState } from "react";

export function CancelOrderConfirmDialog({
  open,
  orderLabel,
  isSubmitting,
  submitError,
  onClose,
  onConfirm,
}) {
  const [reason, setReason] = useState("");

  useEffect(() => {
    if (!open) return;
    setReason("");
  }, [open]);

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-inverse-surface/40 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="cancel-order-title"
    >
      <div className="w-full max-w-md rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-lg">
        <h2 id="cancel-order-title" className="text-headline-sm font-semibold text-on-surface">
          Hủy đơn hàng
        </h2>
        <p className="mt-2 text-body-sm text-on-surface-variant">
          Bạn có chắc muốn hủy đơn{orderLabel ? ` ${orderLabel}` : ""}? Hành động này không thể hoàn
          tác khi đơn đã bắt đầu giao hàng hoặc đã thanh toán.
        </p>

        <label className="mt-4 block">
          <span className="text-label-sm font-medium text-on-surface">
            Lý do hủy <span className="text-on-surface-variant">(tùy chọn)</span>
          </span>
          <textarea
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            rows={3}
            disabled={isSubmitting}
            placeholder="Ví dụ: Không còn nhu cầu mua..."
            className="mt-1 w-full resize-y rounded-lg border border-outline-variant px-3 py-2 text-body-sm disabled:opacity-50"
          />
        </label>

        {submitError ? <p className="mt-3 text-sm text-error">{submitError}</p> : null}

        <div className="mt-6 flex justify-end gap-3">
          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            className="rounded-lg px-4 py-2 text-label-md text-on-surface-variant hover:bg-surface-container-low disabled:opacity-50"
          >
            Đóng
          </button>
          <button
            type="button"
            onClick={() => onConfirm?.({ reason: reason.trim() })}
            disabled={isSubmitting}
            className="rounded-lg bg-error px-4 py-2 text-label-md font-medium text-on-error hover:brightness-95 disabled:opacity-50"
          >
            {isSubmitting ? "Đang xử lý..." : "Hủy đơn"}
          </button>
        </div>
      </div>
    </div>
  );
}
