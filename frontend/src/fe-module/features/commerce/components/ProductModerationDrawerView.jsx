import { Link } from "react-router-dom";
import { formatDateTime } from "../../auth/security/utils/formatDateTime.js";
import { buildAdminSearchParams } from "../../auth/admin/adminUrlParams.js";
import { DetailRow } from "../../auth/admin/adminAudit/components/AdminActionLogDetailDrawerView.jsx";
import { AuditCopyableId } from "../../auth/admin/adminAudit/components/AuditCopyableId.jsx";
import { AdminFilterButton } from "../../auth/admin/components/ui";
import { PostAuthorInvestigationLink } from "../../auth/admin/contentModeration/components/PostAuthorInvestigationLink.jsx";
import { formatVndPrice } from "../../social/utils/formatPrice";
import { formatShortShopId } from "../utils/formatShortShopId";
import { AdminProductStatusBadge } from "./AdminProductStatusBadge";
import { ProductModerationHistoryPanel } from "./ProductModerationHistoryPanel.jsx";

function DrawerDetailSkeleton() {
  return (
    <div className="space-y-4">
      <div className="h-28 animate-pulse rounded-lg bg-admin-surface-muted" />
      <div className="h-20 animate-pulse rounded-lg bg-admin-surface-muted" />
      <div className="h-40 animate-pulse rounded-lg bg-admin-surface-muted" />
    </div>
  );
}

function ShopModerationLink({ shopId, shopName }) {
  if (!shopId) return null;

  const to = `/admin?${buildAdminSearchParams({
    section: "contentModeration",
    tab: "shop-moderation",
    shopId,
  }).toString()}`;

  return (
    <Link to={to} className="text-sm font-medium text-admin-accent hover:underline">
      {shopName || formatShortShopId(shopId)}
    </Link>
  );
}

export function ProductModerationDrawerView({
  productId,
  product,
  detailStatus,
  detailErrorMessage,
  canRemoveProduct,
  canRestoreProduct,
  historyRefreshToken,
  disabled,
  onClose,
  onRemove,
  onRestore,
}) {
  if (!productId) return null;

  const preview = product || {};
  const canRemove = canRemoveProduct && preview.status && preview.status !== "REMOVED";
  const canRestore = canRestoreProduct && preview.status === "REMOVED";
  const gallery = preview.media?.length
    ? preview.media
    : preview.thumbnailUrl
      ? [{ mediaUrl: preview.thumbnailUrl, mediaType: "image" }]
      : [];

  return (
    <div
      className="fixed inset-0 z-50 flex min-h-dvh sm:justify-end"
      role="dialog"
      aria-modal="true"
      aria-labelledby="product-mod-drawer-title"
    >
      <button
        type="button"
        aria-label="Đóng chi tiết sản phẩm"
        className="absolute inset-0 bg-admin-text/40 backdrop-blur-sm"
        onClick={onClose}
      />

      <aside className="relative z-10 flex h-full max-h-dvh w-full flex-col bg-admin-surface shadow-[var(--shadow-admin-surface)] sm:max-w-xl sm:border-l sm:border-admin-border">
        <div className="shrink-0 border-b border-admin-border bg-admin-surface-muted px-4 py-4 sm:px-6">
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0 flex-1">
              {preview.status ? <AdminProductStatusBadge status={preview.status} /> : null}
              <h2
                id="product-mod-drawer-title"
                className="mt-2 text-balance text-lg font-semibold tracking-tight text-admin-text"
              >
                Chi tiết sản phẩm
              </h2>
              <p className="mt-2 text-sm font-medium text-admin-text">{preview.title || "—"}</p>
              <p className="mt-1 text-sm text-admin-text-secondary">
                {formatVndPrice(preview.effectivePrice ?? preview.price)}
              </p>
            </div>
            <button
              type="button"
              onClick={onClose}
              className="inline-flex min-h-10 min-w-10 shrink-0 items-center justify-center rounded-lg border border-admin-border text-admin-text-secondary transition-colors hover:bg-admin-surface-muted hover:text-admin-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
              aria-label="Đóng drawer"
            >
              <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
                close
              </span>
            </button>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto px-4 py-5 sm:px-6">
          {detailStatus === "loading" && !preview.title ? <DrawerDetailSkeleton /> : null}

          {detailStatus === "error" ? (
            <div className="rounded-lg border border-admin-danger/30 bg-admin-danger-soft/20 p-4">
              <p className="text-sm text-admin-danger">{detailErrorMessage}</p>
            </div>
          ) : null}

          {gallery.length ? (
            <div className="mb-4 flex gap-2 overflow-x-auto pb-1">
              {gallery.map((item, index) => (
                <img
                  key={`${item.mediaUrl}-${index}`}
                  src={item.mediaUrl}
                  alt=""
                  className="h-24 w-24 shrink-0 rounded-lg border border-admin-border object-cover"
                />
              ))}
            </div>
          ) : null}

          {preview.description ? (
            <p className="mb-4 line-clamp-6 text-sm text-admin-text-secondary">{preview.description}</p>
          ) : detailStatus === "loading" ? (
            <p className="mb-4 text-sm text-admin-text-secondary">Đang tải mô tả…</p>
          ) : null}

          <div className="space-y-4">
            <DetailRow label="Shop">
              <ShopModerationLink shopId={preview.shopId} shopName={preview.shopName} />
            </DetailRow>

            <DetailRow label="Chủ shop">
              <PostAuthorInvestigationLink authorId={preview.sellerId} />
            </DetailRow>

            {preview.categoryName ? (
              <DetailRow label="Danh mục">{preview.categoryName}</DetailRow>
            ) : null}

            <div className="grid gap-3 sm:grid-cols-2">
              <div className="rounded-lg border border-admin-border-subtle bg-admin-surface-raised p-3">
                <p className="text-xs text-admin-text-muted">Tồn kho</p>
                <p className="mt-1 text-xl font-semibold tabular-nums text-admin-text">
                  {preview.stockQuantity ?? "—"}
                </p>
              </div>
              <div className="rounded-lg border border-admin-border-subtle bg-admin-surface-raised p-3">
                <p className="text-xs text-admin-text-muted">Đơn đang xử lý</p>
                <p className="mt-1 text-xl font-semibold tabular-nums text-admin-text">
                  {preview.openOrderCount ?? 0}
                </p>
              </div>
            </div>

            {preview.status === "REMOVED" ? (
              <div className="rounded-lg border border-admin-danger/30 bg-admin-danger-soft/20 p-3">
                <p className="text-xs font-medium text-admin-danger">Hồ sơ gỡ</p>
                <p className="mt-2 text-sm text-admin-text">
                  {preview.removeReason?.trim() || "Chưa có lý do gỡ trong commerce."}
                </p>
                {preview.removedAt ? (
                  <p className="mt-2 text-xs text-admin-text-secondary">
                    Gỡ lúc: {formatDateTime(preview.removedAt)}
                  </p>
                ) : null}
              </div>
            ) : null}

            {preview.attributes?.length ? (
              <div className="rounded-lg border border-admin-border-subtle bg-admin-surface-raised p-3">
                <p className="text-xs font-medium text-admin-text-muted">Thuộc tính</p>
                <dl className="mt-2 space-y-1 text-sm">
                  {preview.attributes.map((attr) => (
                    <div key={`${attr.name}-${attr.value}`} className="flex justify-between gap-3">
                      <dt className="text-admin-text-secondary">{attr.name}</dt>
                      <dd className="text-right text-admin-text">{attr.value}</dd>
                    </div>
                  ))}
                </dl>
              </div>
            ) : null}

            <DetailRow label="Product ID">
              <AuditCopyableId value={productId} />
            </DetailRow>

            {preview.createdAt ? (
              <DetailRow label="Ngày tạo">{formatDateTime(preview.createdAt)}</DetailRow>
            ) : null}
            {preview.updatedAt ? (
              <DetailRow label="Cập nhật">{formatDateTime(preview.updatedAt)}</DetailRow>
            ) : null}
          </div>

          <ProductModerationHistoryPanel productId={productId} refreshToken={historyRefreshToken} />
        </div>

        <div className="shrink-0 border-t border-admin-border-subtle px-4 py-4 sm:px-6">
          <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
            {canRestore ? (
              <AdminFilterButton type="button" variant="secondary" disabled={disabled} onClick={onRestore}>
                Khôi phục
              </AdminFilterButton>
            ) : null}
            {canRemove ? (
              <AdminFilterButton
                type="button"
                variant="primary"
                className="!bg-admin-danger !text-white hover:!brightness-95"
                disabled={disabled}
                onClick={onRemove}
              >
                Gỡ listing
              </AdminFilterButton>
            ) : null}
          </div>
        </div>
      </aside>
    </div>
  );
}
