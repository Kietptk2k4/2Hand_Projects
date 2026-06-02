export function ConfirmOrderReceivedDialog({
  open,
  isSubmitting,
  submitError,
  onClose,
  onConfirm,
}) {
  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-inverse-surface/40 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="confirm-received-title"
    >
      <div className="w-full max-w-md rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-lg">
        <h2 id="confirm-received-title" className="text-headline-sm font-semibold text-on-surface">
          Xác nhận đã nhận hàng
        </h2>
        <p className="mt-2 text-body-sm text-on-surface-variant">
          Xác nhận bạn đã nhận đủ hàng trong đơn này? Các mục đang ở trạng thái đã giao sẽ được
          hoàn tất và bạn có thể đánh giá sản phẩm sau đó.
        </p>

        {submitError ? <p className="mt-3 text-sm text-error">{submitError}</p> : null}

        <div className="mt-6 flex justify-end gap-3">
          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            className="rounded-lg px-4 py-2 text-label-md text-on-surface-variant hover:bg-surface-container-low disabled:opacity-50"
          >
            Hủy
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={isSubmitting}
            className="rounded-lg bg-primary px-4 py-2 text-label-md font-medium text-on-primary hover:bg-[#0050cb] disabled:opacity-50"
          >
            {isSubmitting ? "Đang xử lý..." : "Xác nhận"}
          </button>
        </div>
      </div>
    </div>
  );
}
