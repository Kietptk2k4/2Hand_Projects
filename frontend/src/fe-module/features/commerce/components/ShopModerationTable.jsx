import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminMobileCard,
  AdminMobileCardList,
} from "../../auth/admin/components/ui";
import { PostAuthorInvestigationLink } from "../../auth/admin/contentModeration/components/PostAuthorInvestigationLink.jsx";
import { formatPostListDateTime } from "../../auth/admin/contentModeration/utils/postDateTimeDisplay.js";
import { AuditCopyableId } from "../../auth/admin/adminAudit/components/AuditCopyableId.jsx";
import { formatShortShopId } from "../utils/formatShortShopId";
import { AdminShopStatusBadge } from "./AdminShopStatusBadge";

export function ShopModerationTable({
  items,
  selectedShopId,
  selectedShopIds = [],
  selectionEnabled = false,
  onRowSelect,
  onToggleShop,
  onToggleAll,
}) {
  if (!items?.length) return null;

  const allSelected = items.every((shop) => selectedShopIds.includes(shop.shopId));

  return (
    <>
      <AdminMobileCardList>
        {items.map((shop) => {
          const isDrawerSelected = selectedShopId === shop.shopId;
          const isBulkSelected = selectedShopIds.includes(shop.shopId);
          const createdAt = formatPostListDateTime(shop.createdAt);

          return (
            <AdminMobileCard
              key={shop.shopId}
              isSelected={isDrawerSelected}
              onClick={() => onRowSelect?.(shop)}
              ariaLabel={`Chọn cửa hàng ${shop.shopName}`}
            >
              {selectionEnabled ? (
                <label
                  className="mb-3 flex min-h-10 items-center gap-2"
                  onClick={(event) => event.stopPropagation()}
                >
                  <input
                    type="checkbox"
                    checked={isBulkSelected}
                    onChange={() => onToggleShop?.(shop.shopId)}
                    aria-label={`Chọn cửa hàng ${shop.shopName}`}
                  />
                  <span className="text-xs text-admin-text-secondary">Chọn để thao tác hàng loạt</span>
                </label>
              ) : null}
              <div className="flex items-start gap-3">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center overflow-hidden rounded-lg border border-admin-border bg-admin-surface-muted">
                  {shop.logoUrl ? (
                    <img src={shop.logoUrl} alt="" className="h-full w-full object-cover" />
                  ) : (
                    <span className="material-symbols-outlined text-admin-text-muted" aria-hidden="true">
                      storefront
                    </span>
                  )}
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-start justify-between gap-2">
                    <p className="line-clamp-2 text-sm font-medium text-admin-text">{shop.shopName}</p>
                    <span className="material-symbols-outlined shrink-0 text-admin-text-muted" aria-hidden="true">
                      chevron_right
                    </span>
                  </div>
                  <div className="mt-2 flex flex-wrap items-center gap-2">
                    <AdminShopStatusBadge status={shop.status} />
                    <span className="text-xs text-admin-text-secondary">
                      {shop.activeProductCount ?? 0} SP đang bán
                    </span>
                  </div>
                  <p className="mt-1 text-xs text-admin-text-muted">
                    {createdAt.date} {createdAt.time}
                  </p>
                </div>
              </div>
              <div className="mt-2" onClick={(event) => event.stopPropagation()}>
                <PostAuthorInvestigationLink authorId={shop.sellerId} />
              </div>
            </AdminMobileCard>
          );
        })}
      </AdminMobileCardList>

      <AdminDataTable minWidth="900px" ariaLabel="Danh sách shop kiểm duyệt">
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
            <AdminDataTableCell header className="min-w-[14rem]">
              Shop
            </AdminDataTableCell>
            <AdminDataTableCell header>Chủ shop</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header>Sản phẩm</AdminDataTableCell>
            <AdminDataTableCell header>Ngày tạo</AdminDataTableCell>
            <AdminDataTableCell header>Shop ID</AdminDataTableCell>
            <AdminDataTableCell header className="w-10" />
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((shop) => {
            const isDrawerSelected = selectedShopId === shop.shopId;
            const isBulkSelected = selectedShopIds.includes(shop.shopId);
            const createdAt = formatPostListDateTime(shop.createdAt);

            return (
              <AdminDataTableRow
                key={shop.shopId}
                isSelected={isDrawerSelected}
                onClick={() => onRowSelect?.(shop)}
                className="cursor-pointer"
              >
                {selectionEnabled ? (
                  <AdminDataTableCell onClick={(event) => event.stopPropagation()}>
                    <input
                      type="checkbox"
                      checked={isBulkSelected}
                      onChange={() => onToggleShop?.(shop.shopId)}
                      aria-label={`Chọn cửa hàng ${shop.shopName}`}
                    />
                  </AdminDataTableCell>
                ) : null}
                <AdminDataTableCell>
                  <div className="flex items-center gap-3">
                    <div className="flex h-10 w-10 shrink-0 items-center justify-center overflow-hidden rounded-lg border border-admin-border bg-admin-surface-muted">
                      {shop.logoUrl ? (
                        <img src={shop.logoUrl} alt="" className="h-full w-full object-cover" />
                      ) : (
                        <span className="material-symbols-outlined text-admin-text-muted" aria-hidden="true">
                          storefront
                        </span>
                      )}
                    </div>
                    <p className="line-clamp-2 text-sm font-medium text-admin-text">{shop.shopName}</p>
                  </div>
                </AdminDataTableCell>
                <AdminDataTableCell onClick={(event) => event.stopPropagation()}>
                  <PostAuthorInvestigationLink authorId={shop.sellerId} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <AdminShopStatusBadge status={shop.status} />
                </AdminDataTableCell>
                <AdminDataTableCell className="text-sm tabular-nums text-admin-text-secondary">
                  {shop.activeProductCount ?? 0} / {shop.productCount ?? 0}
                </AdminDataTableCell>
                <AdminDataTableCell className="text-sm text-admin-text-secondary">
                  <div>{createdAt.date}</div>
                  <div className="text-xs text-admin-text-muted">{createdAt.time}</div>
                </AdminDataTableCell>
                <AdminDataTableCell onClick={(event) => event.stopPropagation()}>
                  <AuditCopyableId value={shop.shopId} displayValue={formatShortShopId(shop.shopId)} />
                </AdminDataTableCell>
                <AdminDataTableCell className="text-admin-text-muted">
                  <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
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
