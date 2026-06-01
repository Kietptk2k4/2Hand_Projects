export function SellerOrderProcessConfirmDialog({
  open,
  items,
  isProcessing,
  errorMessage,
  onCancel,
  onConfirm,
}) {
  if (!open || !items?.length) return null;

  const preview = items.slice(0, 3);
  const rest = items.length - preview.length;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-inverse-surface/40 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="seller-order-process-title"
    >
      <div className="w-full max-w-md rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-lg">
        <h2 id="seller-order-process-title" className="text-headline-sm font-semibold text-on-surface">
          Xác nhận chuẩn bị hàng
        </h2>
        <p className="mt-2 text-body-sm text-on-surface-variant">
          Bạn sắp đánh dấu {items.length} mục đơn chuyển sang trạng thái đang chuẩn bị. Tiếp tục?
        </p>

        <ul className="mt-4 space-y-1 rounded-lg bg-surface-container-low p-3 text-body-sm text-on-surface">
          {preview.map((item) => (
            <li key={item.orderItemId} className="line-clamp-1">
              · {item.productNameSnapshot}
              {item.quantity > 1 ? ` ×${item.quantity}` : ""}
            </li>
          ))}
          {rest > 0 ? (
            <li className="text-on-surface-variant">và {rest} mục khác</li>
          ) : null}
        </ul>

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
            {isProcessing ? "Đang xử lý..." : "Xác nhận"}
          </button>
        </div>
      </div>
    </div>
  );
}
