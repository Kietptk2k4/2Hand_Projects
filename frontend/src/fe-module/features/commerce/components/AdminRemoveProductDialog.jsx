import { useEffect, useState } from "react";
import { formatVndPrice } from "../../social/utils/formatPrice";
import { AdminFilterButton } from "../../auth/admin/components/ui";
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
      className="fixed inset-0 z-50 flex items-end justify-center bg-admin-text/40 p-0 sm:items-center sm:p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="remove-product-title"
    >
      <div className="flex max-h-[90dvh] w-full max-w-lg flex-col overflow-hidden rounded-t-2xl border border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)] sm:rounded-xl">
        <div className="border-b border-admin-border-subtle px-4 py-4 sm:px-6">
          <h2 id="remove-product-title" className="text-lg font-semibold text-admin-text">
            Gỡ sản phẩm
          </h2>
          <p className="mt-1 text-sm text-admin-text-secondary">{product.title}</p>
          <div className="mt-2 flex flex-wrap items-center gap-2">
            <AdminProductStatusBadge status={product.status} />
            <span className="text-xs text-admin-text-muted">
              {formatVndPrice(product.effectivePrice ?? product.price)}
            </span>
          </div>
        </div>

        <div className="flex-1 space-y-4 overflow-y-auto px-4 py-4 sm:px-6">
          <div className="flex gap-3 rounded-lg border border-admin-border bg-admin-surface-muted/50 p-3">
            {product.thumbnailUrl ? (
              <img
                src={product.thumbnailUrl}
                alt=""
                className="h-16 w-16 shrink-0 rounded-lg object-cover"
              />
            ) : null}
            <dl className="min-w-0 space-y-1 text-xs text-admin-text-muted">
              <div>
                <dt className="inline font-medium">product_id: </dt>
                <dd className="inline break-all font-mono">{product.productId}</dd>
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

          <div className="rounded-lg border border-admin-danger/30 bg-admin-danger-soft px-3 py-3">
            <p className="text-xs font-semibold text-admin-danger">Lưu ý nghiệp vụ</p>
            <ul className="mt-2 list-disc space-y-1 pl-5 text-sm text-admin-danger">
              {REMOVE_WARNING_POINTS.map((point) => (
                <li key={point}>{point}</li>
              ))}
            </ul>
          </div>

          <label className="block">
            <span className="text-sm font-medium text-admin-text">
              Lý do gỡ <span className="text-admin-danger">*</span>
            </span>
            <textarea
              value={reason}
              onChange={(event) => setReason(event.target.value)}
              rows={4}
              maxLength={REASON_MAX_LENGTH}
              disabled={isSubmitting}
              placeholder="Mô tả lý do gỡ sản phẩm…"
              className="mt-1 w-full resize-y rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-sm text-admin-text focus:border-admin-accent focus:outline-none focus:ring-2 focus:ring-admin-accent-soft disabled:opacity-50"
            />
          </label>
          <p className="text-right text-xs text-admin-text-muted">
            {trimmedReason.length}/{REASON_MAX_LENGTH}
          </p>

          {submitError ? <p className="text-sm text-admin-danger">{submitError}</p> : null}
        </div>

        <div className="flex flex-col-reverse gap-2 border-t border-admin-border-subtle px-4 py-4 sm:flex-row sm:justify-end sm:px-6">
          <AdminFilterButton type="button" variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Hủy
          </AdminFilterButton>
          <AdminFilterButton
            type="button"
            variant="primary"
            className="!bg-admin-danger !text-white hover:!brightness-95"
            disabled={isSubmitting || !trimmedReason}
            onClick={() => onSubmit?.({ reason: trimmedReason })}
          >
            {isSubmitting ? "Đang xử lý…" : "Gỡ sản phẩm"}
          </AdminFilterButton>
        </div>
      </div>
    </div>
  );
}
