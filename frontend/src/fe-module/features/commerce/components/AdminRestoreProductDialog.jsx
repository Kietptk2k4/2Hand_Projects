import { useEffect, useState } from "react";
import { formatVndPrice } from "../../social/utils/formatPrice";
import { REASON_MAX_LENGTH } from "../constants/adminProductRemovalConstants";
import { formatShortSellerId, formatShortShopId } from "../utils/formatShortShopId";
import { AdminProductStatusBadge } from "./AdminProductStatusBadge";

export function AdminRestoreProductDialog({
  open,
  product,
  isSubmitting,
  submitError,
  onClose,
  onSubmit,
}) {
  const [reason, setReason] = useState("");

  useEffect(() => {
    if (!open || !product) return;
    setReason("");
  }, [open, product?.productId]);

  if (!open || !product) return null;

  const trimmedReason = reason.trim();

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-inverse-surface/40 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="restore-product-title"
    >
      <div className="flex max-h-[90vh] w-full max-w-lg flex-col overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-lg">
        <div className="border-b border-outline-variant px-6 py-4">
          <h2 id="restore-product-title" className="text-headline-sm font-semibold text-on-surface">
            Khôi phục sản phẩm
          </h2>
          <p className="mt-1 text-body-sm text-on-surface-variant">{product.title}</p>
          <div className="mt-2 flex flex-wrap items-center gap-2">
            <AdminProductStatusBadge status={product.status} />
            <span className="text-label-sm text-on-surface-variant">
              {formatVndPrice(product.effectivePrice ?? product.price)}
            </span>
          </div>
        </div>

        <div className="flex-1 space-y-4 overflow-y-auto px-6 py-4">
          <p className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-body-sm text-amber-950">
            Khôi phục sản phẩm qua admin-service. Trạng thái hiển thị cuối cùng do Commerce Service quyết định.
          </p>

          <label className="block">
            <span className="text-label-sm font-medium text-on-surface">
              Lý do khôi phục <span className="text-error">*</span>
            </span>
            <textarea
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              rows={4}
              maxLength={REASON_MAX_LENGTH}
              disabled={isSubmitting}
              placeholder="Mô tả lý do khôi phục..."
              className="mt-1 w-full resize-y rounded-lg border border-outline-variant px-3 py-2 text-body-sm disabled:opacity-50"
            />
          </label>
          <p className="text-right text-label-sm text-on-surface-variant">
            {trimmedReason.length}/{REASON_MAX_LENGTH}
          </p>

          <dl className="grid gap-1 text-label-sm text-on-surface-variant">
            <div>
              <dt className="inline font-medium">product_id: </dt>
              <dd className="inline font-mono break-all">{product.productId}</dd>
            </div>
            <div>
              <dt className="inline font-medium">shop_id: </dt>
              <dd className="inline font-mono">{formatShortShopId(product.shopId)}</dd>
            </div>
            <div>
              <dt className="inline font-medium">seller_id: </dt>
              <dd className="inline font-mono">{formatShortSellerId(product.sellerId)}</dd>
            </div>
          </dl>

          {submitError ? <p className="text-sm text-error">{submitError}</p> : null}
        </div>

        <div className="flex justify-end gap-3 border-t border-outline-variant px-6 py-4">
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
            disabled={isSubmitting || !trimmedReason}
            onClick={() => onSubmit?.({ reason: trimmedReason })}
            className="rounded-lg bg-primary px-4 py-2 text-label-md font-medium text-on-primary disabled:opacity-50"
          >
            {isSubmitting ? "Đang xử lý..." : "Khôi phục"}
          </button>
        </div>
      </div>
    </div>
  );
}
