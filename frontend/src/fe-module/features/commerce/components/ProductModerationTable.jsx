import { Link } from "react-router-dom";
import { buildAdminSearchParams } from "../../auth/admin/adminUrlParams.js";
import { AuditCopyableId } from "../../auth/admin/adminAudit/components/AuditCopyableId.jsx";
import { PostAuthorInvestigationLink } from "../../auth/admin/contentModeration/components/PostAuthorInvestigationLink.jsx";
import { formatPostListDateTime } from "../../auth/admin/contentModeration/utils/postDateTimeDisplay.js";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminMobileCard,
  AdminMobileCardList,
} from "../../auth/admin/components/ui";
import { formatVndPrice } from "../../social/utils/formatPrice";
import { formatShortShopId } from "../utils/formatShortShopId";
import { AdminProductStatusBadge } from "./AdminProductStatusBadge";

function ShopModerationLink({ shopId, shopName }) {
  if (!shopId) return <span className="text-admin-text-muted">—</span>;

  const to = `/admin?${buildAdminSearchParams({
    section: "contentModeration",
    tab: "shop-moderation",
    shopId,
  }).toString()}`;

  return (
    <Link
      to={to}
      onClick={(event) => event.stopPropagation()}
      className="text-sm font-medium text-admin-accent hover:underline"
    >
      {shopName || formatShortShopId(shopId)}
    </Link>
  );
}

export function ProductModerationTable({
  items,
  selectedProductId,
  selectedProductIds = [],
  selectionEnabled = false,
  onRowSelect,
  onToggleProduct,
  onToggleAll,
}) {
  if (!items?.length) return null;

  const allSelected = items.every((product) => selectedProductIds.includes(product.productId));

  return (
    <>
      <AdminMobileCardList>
        {items.map((product) => {
          const isDrawerSelected = selectedProductId === product.productId;
          const isBulkSelected = selectedProductIds.includes(product.productId);
          const createdAt = formatPostListDateTime(product.createdAt);

          return (
            <AdminMobileCard
              key={product.productId}
              isSelected={isDrawerSelected}
              onClick={() => onRowSelect?.(product)}
              ariaLabel={`Chọn sản phẩm ${product.title}`}
            >
              {selectionEnabled ? (
                <label
                  className="mb-3 flex min-h-10 items-center gap-2"
                  onClick={(event) => event.stopPropagation()}
                >
                  <input
                    type="checkbox"
                    checked={isBulkSelected}
                    onChange={() => onToggleProduct?.(product.productId)}
                    aria-label={`Chọn sản phẩm ${product.title}`}
                  />
                  <span className="text-xs text-admin-text-secondary">Chọn để thao tác hàng loạt</span>
                </label>
              ) : null}
              <div className="flex items-start gap-3">
                {product.thumbnailUrl ? (
                  <img
                    src={product.thumbnailUrl}
                    alt=""
                    className="h-12 w-12 shrink-0 rounded-lg border border-admin-border object-cover"
                  />
                ) : (
                  <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-lg border border-admin-border bg-admin-surface-muted">
                    <span className="material-symbols-outlined text-admin-text-muted" aria-hidden="true">
                      inventory_2
                    </span>
                  </div>
                )}
                <div className="min-w-0 flex-1">
                  <div className="flex items-start justify-between gap-2">
                    <p className="line-clamp-2 text-sm font-medium text-admin-text">{product.title}</p>
                    <span className="material-symbols-outlined shrink-0 text-admin-text-muted" aria-hidden="true">
                      chevron_right
                    </span>
                  </div>
                  <p className="mt-1 text-xs text-admin-text-secondary">
                    {formatVndPrice(product.effectivePrice ?? product.price)}
                  </p>
                  <div className="mt-2 flex flex-wrap items-center gap-2">
                    <AdminProductStatusBadge status={product.status} />
                  </div>
                  <p className="mt-1 text-xs text-admin-text-muted">
                    {createdAt.date} {createdAt.time}
                  </p>
                </div>
              </div>
              <div className="mt-2 space-y-1" onClick={(event) => event.stopPropagation()}>
                <ShopModerationLink shopId={product.shopId} shopName={product.shopName} />
                <PostAuthorInvestigationLink authorId={product.sellerId} />
              </div>
            </AdminMobileCard>
          );
        })}
      </AdminMobileCardList>

      <AdminDataTable minWidth="1100px" ariaLabel="Danh sách sản phẩm kiểm duyệt">
        <AdminDataTableHead>
          <AdminDataTableRow>
            {selectionEnabled ? (
              <AdminDataTableCell header className="w-12">
                <input
                  type="checkbox"
                  checked={allSelected}
                  onChange={() => onToggleAll?.(items)}
                  aria-label="Chọn tất cả trên trang"
                />
              </AdminDataTableCell>
            ) : null}
            <AdminDataTableCell header className="min-w-[16rem]">
              Sản phẩm
            </AdminDataTableCell>
            <AdminDataTableCell header>Shop</AdminDataTableCell>
            <AdminDataTableCell header>Chủ shop</AdminDataTableCell>
            <AdminDataTableCell header>Danh mục</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header>Lý do gỡ</AdminDataTableCell>
            <AdminDataTableCell header>Ngày tạo</AdminDataTableCell>
            <AdminDataTableCell header>Product ID</AdminDataTableCell>
            <AdminDataTableCell header className="w-10" />
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((product) => {
            const isDrawerSelected = selectedProductId === product.productId;
            const isBulkSelected = selectedProductIds.includes(product.productId);
            const createdAt = formatPostListDateTime(product.createdAt);

            return (
              <AdminDataTableRow
                key={product.productId}
                isSelected={isDrawerSelected}
                onClick={() => onRowSelect?.(product)}
                ariaLabel={`Chọn sản phẩm ${product.title}`}
              >
                {selectionEnabled ? (
                  <AdminDataTableCell onClick={(event) => event.stopPropagation()}>
                    <input
                      type="checkbox"
                      checked={isBulkSelected}
                      onChange={() => onToggleProduct?.(product.productId)}
                      aria-label={`Chọn sản phẩm ${product.title}`}
                    />
                  </AdminDataTableCell>
                ) : null}
                <AdminDataTableCell>
                  <div className="flex items-start gap-3">
                    {product.thumbnailUrl ? (
                      <img
                        src={product.thumbnailUrl}
                        alt=""
                        className="h-10 w-10 shrink-0 rounded-lg border border-admin-border object-cover"
                      />
                    ) : (
                      <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border border-admin-border bg-admin-surface-muted">
                        <span className="material-symbols-outlined text-[18px] text-admin-text-muted" aria-hidden="true">
                          inventory_2
                        </span>
                      </div>
                    )}
                    <div className="min-w-0">
                      <p className="line-clamp-2 text-sm font-medium text-admin-text">{product.title}</p>
                      <p className="mt-0.5 text-xs text-admin-text-secondary">
                        {formatVndPrice(product.effectivePrice ?? product.price)}
                      </p>
                    </div>
                  </div>
                </AdminDataTableCell>
                <AdminDataTableCell onClick={(event) => event.stopPropagation()}>
                  <ShopModerationLink shopId={product.shopId} shopName={product.shopName} />
                </AdminDataTableCell>
                <AdminDataTableCell onClick={(event) => event.stopPropagation()}>
                  <PostAuthorInvestigationLink authorId={product.sellerId} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <span className="text-sm text-admin-text-secondary">{product.categoryName || "—"}</span>
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <AdminProductStatusBadge status={product.status} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  {product.status === "REMOVED" ? (
                    <span className="line-clamp-2 text-sm text-admin-text-secondary">
                      {product.removeReason?.trim() || "—"}
                    </span>
                  ) : (
                    <span className="text-admin-text-muted">—</span>
                  )}
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <div className="text-sm text-admin-text-secondary">
                    <div>{createdAt.date}</div>
                    <div className="text-xs text-admin-text-muted">{createdAt.time}</div>
                  </div>
                </AdminDataTableCell>
                <AdminDataTableCell onClick={(event) => event.stopPropagation()}>
                  <AuditCopyableId value={product.productId} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <span className="material-symbols-outlined text-admin-text-muted" aria-hidden="true">
                    chevron_right
                  </span>
                </AdminDataTableCell>
              </AdminDataTableRow>
            );
          })}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
