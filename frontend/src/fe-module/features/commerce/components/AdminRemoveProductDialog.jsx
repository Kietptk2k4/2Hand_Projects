import { useEffect, useState } from "react";
import { formatVndPrice } from "../../social/utils/formatPrice";
import {
  REASON_MAX_LENGTH,
  REMOVE_WARNING_POINTS,
} from "../constants/adminProductRemovalConstants";
import { formatShortSellerId, formatShortShopId } from "../utils/formatShortShopId";
import { AdminProductStatusBadge } from "./AdminProductStatusBadge";

export function AdminRemoveProductDialog({
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
      aria-labelledby="remove-product-title"
    >
      <div className="flex max-h-[90vh] w-full max-w-lg flex-col overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-lg">
        <div className="border-b border-outline-variant px-6 py-4">
          <h2 id="remove-product-title" className="text-headline-sm font-semibold text-on-surface">
            Gỡ sản phẩm
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
          <div className="flex gap-3 rounded-lg border border-outline-variant bg-surface-container-low/50 p-3">
            {product.thumbnailUrl ? (
              <img
                src={product.thumbnailUrl}
                alt=""
                className="h-16 w-16 shrink-0 rounded-lg object-cover"
              />
            ) : null}
            <dl className="min-w-0 space-y-1 text-label-sm text-on-surface-variant">
              <div>
                <dt className="inline font-medium">product_id: </dt>
                <dd className="inline font-mono break-all">{product.productId}</dd>
              </div>
              <div>
                <dt className="inline font-medium">shop_id: </dt>
                <dd className="inline font-mono" title={product.shopId}>
                  {formatShortShopId(product.shopId)}
                </dd>
              </div>
              <div>
                <dt className="inline font-medium">seller_id: </dt>
                <dd className="inline font-mono" title={product.sellerId}>
                  {formatShortSellerId(product.sellerId)}
                </dd>
              </div>
            </dl>
          </div>

          <div className="rounded-lg border border-error/25 bg-error-container/20 px-3 py-3">
            <p className="text-label-sm font-semibold text-on-error-container">Lưu ý nghiệp vụ</p>
            <ul className="mt-2 list-disc space-y-1 pl-5 text-body-sm text-on-error-container">
              {REMOVE_WARNING_POINTS.map((point) => (
                <li key={point}>{point}</li>
              ))}
            </ul>
          </div>

          <label className="block">
            <span className="text-label-sm font-medium text-on-surface">
              Lý do gỡ <span className="text-error">*</span>
            </span>
            <textarea
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              rows={4}
              maxLength={REASON_MAX_LENGTH}
              disabled={isSubmitting}
              placeholder="Mô tả lý do gỡ sản phẩm..."
              className="mt-1 w-full resize-y rounded-lg border border-outline-variant px-3 py-2 text-body-sm disabled:opacity-50"
            />
          </label>
          <p className="text-right text-label-sm text-on-surface-variant">
            {trimmedReason.length}/{REASON_MAX_LENGTH}
          </p>

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
            className="rounded-lg bg-error px-4 py-2 text-label-md font-medium text-on-error hover:brightness-95 disabled:opacity-50"
          >
            {isSubmitting ? "Đang xử lý..." : "Gỡ sản phẩm"}
          </button>
        </div>
      </div>
    </div>
  );
}
