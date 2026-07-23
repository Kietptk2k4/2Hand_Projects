import { formatDateTime } from "../../auth/security/utils/formatDateTime.js";
import { DetailRow } from "../../auth/admin/adminAudit/components/AdminActionLogDetailDrawerView.jsx";
import { AuditCopyableId } from "../../auth/admin/adminAudit/components/AuditCopyableId.jsx";
import { AdminFilterButton } from "../../auth/admin/components/ui";
import { PostAuthorInvestigationLink } from "../../auth/admin/contentModeration/components/PostAuthorInvestigationLink.jsx";
import { getAllowedActionsForStatus } from "../constants/adminShopModerationConstants.js";
import { formatShortShopId } from "../utils/formatShortShopId";
import { AdminShopStatusBadge } from "./AdminShopStatusBadge";
import { ShopModerationHistoryPanel } from "./ShopModerationHistoryPanel.jsx";

function DrawerDetailSkeleton() {
  return (
    <div className="space-y-4">
      <div className="h-28 animate-pulse rounded-lg bg-admin-surface-muted" />
      <div className="h-20 animate-pulse rounded-lg bg-admin-surface-muted" />
      <div className="h-40 animate-pulse rounded-lg bg-admin-surface-muted" />
    </div>
  );
}

export function ShopModerationDrawerView({
  shopId,
  shop,
  detailStatus,
  detailErrorMessage,
  canSuspendShop,
  canCloseShop,
  canReopenShop,
  historyRefreshToken,
  disabled,
  onClose,
  onModerate,
}) {
  if (!shopId) return null;

  const preview = shop || {};
  const allowedActions = getAllowedActionsForStatus(preview.status);
  const canModerate =
    allowedActions.some((action) => action === "SUSPEND" && canSuspendShop) ||
    allowedActions.some((action) => action === "CLOSE" && canCloseShop) ||
    allowedActions.some((action) => action === "RESTORE" && canReopenShop);

  return (
    <div
      className="fixed inset-0 z-50 flex min-h-dvh sm:justify-end"
      role="dialog"
      aria-modal="true"
      aria-labelledby="shop-mod-drawer-title"
    >
      <button
        type="button"
        aria-label="Đóng chi tiết cửa hàng"
        className="absolute inset-0 bg-admin-text/40 backdrop-blur-sm"
        onClick={onClose}
      />

      <aside className="relative z-10 flex h-full max-h-dvh w-full flex-col bg-admin-surface shadow-[var(--shadow-admin-surface)] sm:max-w-xl sm:border-l sm:border-admin-border">
        <div className="shrink-0 border-b border-admin-border bg-admin-surface-muted px-4 py-4 sm:px-6">
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0 flex-1">
              {preview.status ? (
                <AdminShopStatusBadge status={preview.status} />
              ) : null}
              <h2
                id="shop-mod-drawer-title"
                className="mt-2 text-balance text-lg font-semibold tracking-tight text-admin-text"
              >
                Chi tiết cửa hàng
              </h2>
              <div className="mt-3 flex items-start gap-3">
                <div className="flex h-12 w-12 shrink-0 items-center justify-center overflow-hidden rounded-lg border border-admin-border bg-admin-surface">
                  {preview.logoUrl ? (
                    <img src={preview.logoUrl} alt="" className="h-full w-full object-cover" />
                  ) : (
                    <span className="material-symbols-outlined text-admin-text-muted" aria-hidden="true">
                      storefront
                    </span>
                  )}
                </div>
                <div className="min-w-0">
                  <p className="text-sm font-medium text-admin-text">{preview.shopName || "—"}</p>
                  {preview.description ? (
                    <p className="mt-1 line-clamp-4 text-sm text-admin-text-secondary">{preview.description}</p>
                  ) : detailStatus === "loading" ? (
                    <p className="mt-1 text-sm text-admin-text-secondary">Đang tải mô tả shop…</p>
                  ) : null}
                </div>
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
          {detailStatus === "loading" && !shop?.shopName ? <DrawerDetailSkeleton /> : null}

          {detailStatus === "error" ? (
            <div className="rounded-lg border border-admin-danger/30 bg-admin-danger-soft/20 p-4">
              <p className="text-sm text-admin-danger">{detailErrorMessage}</p>
            </div>
          ) : null}

          <div className="space-y-4">
            <DetailRow label="Chủ shop">
              <PostAuthorInvestigationLink authorId={preview.sellerId} />
            </DetailRow>

            <div className="grid gap-3 sm:grid-cols-3">
              <div className="rounded-lg border border-admin-border-subtle bg-admin-surface-raised p-3">
                <p className="text-xs text-admin-text-muted">SP đang bán</p>
                <p className="mt-1 text-xl font-semibold tabular-nums text-admin-text">
                  {preview.activeProductCount ?? 0}
                </p>
              </div>
              <div className="rounded-lg border border-admin-border-subtle bg-admin-surface-raised p-3">
                <p className="text-xs text-admin-text-muted">Tổng sản phẩm</p>
                <p className="mt-1 text-xl font-semibold tabular-nums text-admin-text">
                  {preview.totalProductCount ?? preview.productCount ?? 0}
                </p>
              </div>
              <div className="rounded-lg border border-admin-border-subtle bg-admin-surface-raised p-3">
                <p className="text-xs text-admin-text-muted">Đơn đang xử lý</p>
                <p className="mt-1 text-xl font-semibold tabular-nums text-admin-text">
                  {preview.openOrderCount ?? 0}
                </p>
              </div>
            </div>

            <DetailRow label="Shop ID">
              <AuditCopyableId value={shopId} displayValue={formatShortShopId(shopId)} />
            </DetailRow>

            {preview.createdAt ? (
              <DetailRow label="Ngày tạo">{formatDateTime(preview.createdAt)}</DetailRow>
            ) : null}
            {preview.updatedAt ? (
              <DetailRow label="Cập nhật">{formatDateTime(preview.updatedAt)}</DetailRow>
            ) : null}
          </div>

          <ShopModerationHistoryPanel shopId={shopId} refreshToken={historyRefreshToken} />
        </div>

        {canModerate ? (
          <div className="shrink-0 border-t border-admin-border-subtle px-4 py-4 sm:px-6">
            <AdminFilterButton
              type="button"
              variant="primary"
              className="w-full"
              disabled={disabled}
              onClick={onModerate}
            >
              <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
                gavel
              </span>
              Kiểm duyệt
            </AdminFilterButton>
          </div>
        ) : null}
      </aside>
    </div>
  );
}
