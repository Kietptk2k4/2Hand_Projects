import { Link } from "react-router-dom";
import { formatDateTime } from "../../auth/security/utils/formatDateTime.js";
import { buildAdminSearchParams } from "../../auth/admin/adminUrlParams.js";
import { DetailRow } from "../../auth/admin/adminAudit/components/AdminActionLogDetailDrawerView.jsx";
import { AuditCopyableId } from "../../auth/admin/adminAudit/components/AuditCopyableId.jsx";
import { AdminFilterButton } from "../../auth/admin/components/ui";
import { PostAuthorInvestigationLink } from "../../auth/admin/contentModeration/components/PostAuthorInvestigationLink.jsx";
import { formatShortShopId } from "../utils/formatShortShopId";
import { AdminReviewStatusBadge } from "./AdminReviewStatusBadge";
import { ReviewModerationHistoryPanel } from "./ReviewModerationHistoryPanel.jsx";
import { StarRating } from "./StarRating";

function DrawerDetailSkeleton() {
  return (
    <div className="space-y-4">
      <div className="h-28 animate-pulse rounded-lg bg-admin-surface-muted" />
      <div className="h-20 animate-pulse rounded-lg bg-admin-surface-muted" />
    </div>
  );
}

function ProductModerationLink({ productId, productTitle }) {
  if (!productId) return null;

  const to = `/admin?${buildAdminSearchParams({
    section: "contentModeration",
    tab: "product-moderation",
    productId,
  }).toString()}`;

  return (
    <Link to={to} className="text-sm font-medium text-admin-accent hover:underline">
      {productTitle || productId}
    </Link>
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

export function ReviewModerationDrawerView({
  reviewId,
  review,
  detailStatus,
  detailErrorMessage,
  canHideReview,
  canRemoveReview,
  canRestoreReview,
  historyRefreshToken,
  disabled,
  onClose,
  onHide,
  onRemove,
  onRestore,
}) {
  if (!reviewId) return null;

  const preview = review || {};
  const canHide = canHideReview && preview.status === "VISIBLE";
  const canRemove =
    canRemoveReview && (preview.status === "VISIBLE" || preview.status === "HIDDEN");
  const canRestore = canRestoreReview && preview.status === "HIDDEN";

  return (
    <div
      className="fixed inset-0 z-50 flex min-h-dvh sm:justify-end"
      role="dialog"
      aria-modal="true"
      aria-labelledby="review-mod-drawer-title"
    >
      <button
        type="button"
        aria-label="Đóng chi tiết đánh giá"
        className="absolute inset-0 bg-admin-text/40 backdrop-blur-sm"
        onClick={onClose}
      />

      <aside className="relative z-10 flex h-full max-h-dvh w-full flex-col bg-admin-surface shadow-[var(--shadow-admin-surface)] sm:max-w-xl sm:border-l sm:border-admin-border">
        <div className="shrink-0 border-b border-admin-border bg-admin-surface-muted px-4 py-4 sm:px-6">
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0 flex-1">
              {preview.status ? <AdminReviewStatusBadge status={preview.status} /> : null}
              <h2
                id="review-mod-drawer-title"
                className="mt-2 text-balance text-lg font-semibold tracking-tight text-admin-text"
              >
                Chi tiết đánh giá
              </h2>
              <div className="mt-2">
                <StarRating rating={preview.rating ?? 0} />
              </div>
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
          {detailStatus === "loading" && !preview.comment ? <DrawerDetailSkeleton /> : null}

          {detailStatus === "error" ? (
            <div className="rounded-lg border border-admin-danger/30 bg-admin-danger-soft/20 p-4">
              <p className="text-sm text-admin-danger">{detailErrorMessage}</p>
            </div>
          ) : null}

          <p className="mb-4 whitespace-pre-wrap text-sm text-admin-text">
            {preview.comment?.trim() || "—"}
          </p>

          {preview.productThumbnailUrl ? (
            <img
              src={preview.productThumbnailUrl}
              alt=""
              className="mb-4 h-24 w-24 rounded-lg border border-admin-border object-cover"
            />
          ) : null}

          <div className="space-y-4">
            <DetailRow label="Sản phẩm">
              <ProductModerationLink
                productId={preview.productId}
                productTitle={preview.productTitle}
              />
            </DetailRow>

            {preview.shopId ? (
              <DetailRow label="Cửa hàng">
                <ShopModerationLink shopId={preview.shopId} shopName={preview.shopName} />
              </DetailRow>
            ) : null}

            <DetailRow label="Người mua">
              <PostAuthorInvestigationLink authorId={preview.buyerId} />
            </DetailRow>

            <DetailRow label="Chủ shop">
              <PostAuthorInvestigationLink authorId={preview.sellerId} />
            </DetailRow>

            <DetailRow label="Order item ID">
              <AuditCopyableId value={preview.orderItemId} />
            </DetailRow>

            <DetailRow label="Review ID">
              <AuditCopyableId value={reviewId} />
            </DetailRow>

            {preview.createdAt ? (
              <DetailRow label="Ngày tạo">{formatDateTime(preview.createdAt)}</DetailRow>
            ) : null}
          </div>

          <ReviewModerationHistoryPanel reviewId={reviewId} refreshToken={historyRefreshToken} />
        </div>

        <div className="shrink-0 border-t border-admin-border-subtle px-4 py-4 sm:px-6">
          <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
            {canRestore ? (
              <AdminFilterButton type="button" variant="secondary" disabled={disabled} onClick={onRestore}>
                Khôi phục
              </AdminFilterButton>
            ) : null}
            {canHide ? (
              <AdminFilterButton
                type="button"
                variant="primary"
                className="!bg-admin-warning !text-white hover:!brightness-95"
                disabled={disabled}
                onClick={onHide}
              >
                Ẩn
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
                Gỡ
              </AdminFilterButton>
            ) : null}
          </div>
        </div>
      </aside>
    </div>
  );
}
