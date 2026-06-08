import { formatVndPrice } from "../../social/utils/formatPrice";
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
      className="fixed inset-0 z-50 flex items-center justify-center bg-inverse-surface/40 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="removed-case-title"
    >
      <div className="flex max-h-[90vh] w-full max-w-lg flex-col overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-lg">
        <div className="border-b border-outline-variant px-6 py-4">
          <h2 id="removed-case-title" className="text-headline-sm font-semibold text-on-surface">
            Hồ sơ gỡ sản phẩm
          </h2>
          <p className="mt-1 text-body-sm text-on-surface-variant">{product.title}</p>
          <div className="mt-2">
            <AdminProductStatusBadge status="REMOVED" />
          </div>
        </div>

        <div className="flex-1 space-y-4 overflow-y-auto px-6 py-4">
          <div className="flex gap-3">
            {product.thumbnailUrl ? (
              <img
                src={product.thumbnailUrl}
                alt=""
                className="h-20 w-20 shrink-0 rounded-lg border border-outline-variant object-cover"
              />
            ) : null}
            <div>
              <p className="text-body-sm text-on-surface-variant">
                Giá: {formatVndPrice(product.effectivePrice ?? product.price)}
              </p>
              <p className="mt-1 font-mono text-label-sm text-on-surface-variant">
                Shop: {formatShortShopId(product.shopId)} · Seller:{" "}
                {formatShortSellerId(product.sellerId)}
              </p>
            </div>
          </div>

          <div className="rounded-lg border border-outline-variant bg-surface-container-low/50 p-3">
            <p className="text-label-sm font-medium text-on-surface">Lý do gỡ</p>
            <p className="mt-2 text-body-sm text-on-surface">
              {product.removeReason?.trim() || "—"}
            </p>
          </div>

          <dl className="grid gap-2 text-body-sm text-on-surface-variant">
            <div>
              <dt className="font-medium text-on-surface">Thời điểm gỡ</dt>
              <dd>{product.removedAt ? formatOrderDate(product.removedAt) : "—"}</dd>
            </div>
            <div>
              <dt className="font-medium text-on-surface">product_id</dt>
              <dd className="font-mono break-all">{product.productId}</dd>
            </div>
          </dl>
        </div>

        <div className="flex flex-wrap justify-end gap-3 border-t border-outline-variant px-6 py-4">
          <button
            type="button"
            onClick={onClose}
            disabled={isRestoring}
            className="rounded-lg px-4 py-2 text-label-md text-on-surface-variant hover:bg-surface-container-low disabled:opacity-50"
          >
            Đóng
          </button>
          <button
            type="button"
            onClick={onViewHistory}
            disabled={isRestoring}
            className="rounded-lg border border-outline-variant px-4 py-2 text-label-md text-on-surface-variant hover:bg-surface-container-low disabled:opacity-50"
          >
            Xem lịch sử
          </button>
          <button
            type="button"
            onClick={onRestore}
            disabled={isRestoring}
            className="rounded-lg bg-primary px-4 py-2 text-label-md font-medium text-on-primary disabled:opacity-50"
          >
            Khôi phục
          </button>
        </div>
      </div>
    </div>
  );
}
