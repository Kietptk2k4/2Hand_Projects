import { formatVndPrice } from "../../social/utils/formatPrice";
import { AdminFilterButton } from "../../auth/admin/components/ui";
import { formatOrderDate } from "../utils/formatOrderDate";
import { formatShortSellerId, formatShortShopId } from "../utils/formatShortShopId";
import { AdminProductStatusBadge } from "./AdminProductStatusBadge";

export function AdminRemovedProductCaseDialog({
  open,
  product,
  isRestoring = false,
  onClose,
  onRestore,
  onViewHistory,
}) {
  if (!open || !product) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-end justify-center bg-admin-text/40 p-0 sm:items-center sm:p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="removed-case-title"
    >
      <div className="flex max-h-[90dvh] w-full max-w-lg flex-col overflow-hidden rounded-t-2xl border border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)] sm:rounded-xl">
        <div className="border-b border-admin-border-subtle px-4 py-4 sm:px-6">
          <h2 id="removed-case-title" className="text-lg font-semibold text-admin-text">
            Hồ sơ gỡ sản phẩm
          </h2>
          <p className="mt-1 text-sm text-admin-text-secondary">{product.title}</p>
          <div className="mt-2">
            <AdminProductStatusBadge status="REMOVED" />
          </div>
        </div>

        <div className="flex-1 space-y-4 overflow-y-auto px-4 py-4 sm:px-6">
          <div className="flex gap-3">
            {product.thumbnailUrl ? (
              <img
                src={product.thumbnailUrl}
                alt=""
                className="h-20 w-20 shrink-0 rounded-lg border border-admin-border object-cover"
              />
            ) : null}
            <div>
              <p className="text-sm text-admin-text-secondary">
                Giá: {formatVndPrice(product.effectivePrice ?? product.price)}
              </p>
              <p className="mt-1 font-mono text-xs text-admin-text-muted">
                Shop: {formatShortShopId(product.shopId)} · Seller:{" "}
                {formatShortSellerId(product.sellerId)}
              </p>
            </div>
          </div>

          <div className="rounded-lg border border-admin-border bg-admin-surface-muted/50 p-3">
            <p className="text-xs font-medium text-admin-text">Lý do gỡ</p>
            <p className="mt-2 text-sm text-admin-text">{product.removeReason?.trim() || "—"}</p>
          </div>

          <dl className="grid gap-2 text-sm text-admin-text-secondary">
            <div>
              <dt className="font-medium text-admin-text">Thời điểm gỡ</dt>
              <dd>{product.removedAt ? formatOrderDate(product.removedAt) : "—"}</dd>
            </div>
            <div>
              <dt className="font-medium text-admin-text">product_id</dt>
              <dd className="break-all font-mono text-xs">{product.productId}</dd>
            </div>
          </dl>
        </div>

        <div className="flex flex-col gap-2 border-t border-admin-border-subtle px-4 py-4 sm:flex-row sm:flex-wrap sm:justify-end sm:px-6">
          <AdminFilterButton type="button" variant="secondary" disabled={isRestoring} onClick={onClose}>
            Đóng
          </AdminFilterButton>
          <AdminFilterButton
            type="button"
            variant="secondary"
            disabled={isRestoring}
            onClick={onViewHistory}
          >
            Xem lịch sử
          </AdminFilterButton>
          <AdminFilterButton type="button" variant="primary" disabled={isRestoring} onClick={onRestore}>
            Khôi phục
          </AdminFilterButton>
        </div>
      </div>
    </div>
  );
}
