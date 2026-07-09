import { formatVndPrice } from "../../social/utils/formatPrice";
import { formatShortSellerId, formatShortShopId } from "../utils/formatShortShopId";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminFilterButton,
  AdminMobileCard,
  AdminMobileCardList,
} from "../../auth/admin/components/ui";
import { AdminProductStatusBadge } from "./AdminProductStatusBadge";

export function AdminProductRemovalTable({
  items,
  disabled,
  onRemove,
  onRestore,
  onViewCase,
  onViewHistory,
}) {
  if (!items?.length) return null;

  return (
    <>
      <AdminMobileCardList>
        {items.map((product) => {
          const isRemoved = product.status === "REMOVED";
          const shopRef = formatShortShopId(product.shopId);
          const sellerRef = formatShortSellerId(product.sellerId);

          return (
            <AdminMobileCard
              key={product.productId}
              className={isRemoved ? "border-admin-danger/20 bg-admin-danger-soft/20" : ""}
            >
              <div className="flex items-start gap-3">
                <div className="flex h-12 w-12 shrink-0 items-center justify-center overflow-hidden rounded-lg border border-admin-border bg-admin-surface-muted">
                  {product.thumbnailUrl ? (
                    <img src={product.thumbnailUrl} alt="" className="h-full w-full object-cover" />
                  ) : (
                    <span className="material-symbols-outlined text-admin-text-muted" aria-hidden="true">
                      inventory_2
                    </span>
                  )}
                </div>
                <div className="min-w-0 flex-1">
                  <p className="line-clamp-2 text-sm font-medium text-admin-text">
                    {product.title || "—"}
                  </p>
                  <p className="mt-0.5 text-sm text-admin-text-secondary">
                    {formatVndPrice(product.effectivePrice ?? product.price)}
                  </p>
                  <div className="mt-2 flex flex-wrap items-center gap-2">
                    <AdminProductStatusBadge status={product.status} />
                    <span className="text-xs text-admin-text-muted">{product.categoryName || "—"}</span>
                  </div>
                  <p className="mt-1 font-mono text-xs text-admin-text-muted">
                    Seller {sellerRef} · Shop {shopRef}
                  </p>
                  {product.shopName ? (
                    <p className="mt-0.5 line-clamp-1 text-xs text-admin-text-secondary">
                      {product.shopName}
                    </p>
                  ) : null}
                </div>
              </div>

              <div className="mt-3 flex flex-col gap-2 sm:flex-row sm:flex-wrap">
                {isRemoved ? (
                  <>
                    <AdminFilterButton
                      type="button"
                      variant="secondary"
                      className="w-full sm:w-auto"
                      disabled={disabled}
                      onClick={() => onViewCase?.(product)}
                    >
                      Hồ sơ
                    </AdminFilterButton>
                    <AdminFilterButton
                      type="button"
                      variant="secondary"
                      className="w-full sm:w-auto"
                      disabled={disabled}
                      onClick={() => onViewHistory?.(product)}
                    >
                      Lịch sử
                    </AdminFilterButton>
                    <AdminFilterButton
                      type="button"
                      variant="primary"
                      className="w-full sm:w-auto"
                      disabled={disabled}
                      onClick={() => onRestore?.(product)}
                    >
                      Khôi phục
                    </AdminFilterButton>
                  </>
                ) : (
                  <AdminFilterButton
                    type="button"
                    variant="primary"
                    className="w-full sm:w-auto"
                    disabled={disabled}
                    onClick={() => onRemove?.(product)}
                  >
                    Kiểm duyệt
                  </AdminFilterButton>
                )}
              </div>
            </AdminMobileCard>
          );
        })}
      </AdminMobileCardList>

      <AdminDataTable minWidth="900px" ariaLabel="Danh sách sản phẩm kiểm duyệt">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Sản phẩm</AdminDataTableCell>
            <AdminDataTableCell header>Seller / Shop</AdminDataTableCell>
            <AdminDataTableCell header>Danh mục</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header className="text-right">
              Thao tác
            </AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((product) => {
            const isRemoved = product.status === "REMOVED";
            const shopRef = formatShortShopId(product.shopId);
            const sellerRef = formatShortSellerId(product.sellerId);

            return (
              <AdminDataTableRow
                key={product.productId}
                className={isRemoved ? "bg-admin-danger-soft/15" : ""}
              >
                <AdminDataTableCell>
                  <div className="flex items-center gap-4">
                    <div className="flex h-12 w-12 shrink-0 items-center justify-center overflow-hidden rounded-lg border border-admin-border bg-admin-surface-muted">
                      {product.thumbnailUrl ? (
                        <img
                          src={product.thumbnailUrl}
                          alt=""
                          className="h-full w-full object-cover"
                        />
                      ) : (
                        <span className="material-symbols-outlined text-admin-text-muted" aria-hidden="true">
                          inventory_2
                        </span>
                      )}
                    </div>
                    <div className="min-w-0">
                      <p className="line-clamp-1 text-sm font-medium text-admin-text">
                        {product.title || "—"}
                      </p>
                      <p className="mt-0.5 text-sm text-admin-text-secondary">
                        {formatVndPrice(product.effectivePrice ?? product.price)}
                      </p>
                    </div>
                  </div>
                </AdminDataTableCell>
                <AdminDataTableCell className="text-sm text-admin-text-secondary">
                  <p title={product.sellerId}>
                    <span className="font-mono text-xs">{sellerRef}</span>
                  </p>
                  <p className="mt-0.5 font-mono text-xs" title={product.shopId}>
                    {shopRef}
                  </p>
                  {product.shopName ? (
                    <p className="mt-0.5 line-clamp-1 text-xs">{product.shopName}</p>
                  ) : null}
                </AdminDataTableCell>
                <AdminDataTableCell className="text-sm text-admin-text-secondary">
                  {product.categoryName || "—"}
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <AdminProductStatusBadge status={product.status} />
                </AdminDataTableCell>
                <AdminDataTableCell className="text-right">
                  {isRemoved ? (
                    <div className="flex flex-wrap justify-end gap-2">
                      <AdminFilterButton
                        type="button"
                        variant="secondary"
                        disabled={disabled}
                        onClick={() => onViewCase?.(product)}
                      >
                        Hồ sơ
                      </AdminFilterButton>
                      <AdminFilterButton
                        type="button"
                        variant="secondary"
                        disabled={disabled}
                        onClick={() => onViewHistory?.(product)}
                      >
                        Lịch sử
                      </AdminFilterButton>
                      <AdminFilterButton
                        type="button"
                        variant="primary"
                        disabled={disabled}
                        onClick={() => onRestore?.(product)}
                      >
                        Khôi phục
                      </AdminFilterButton>
                    </div>
                  ) : (
                    <AdminFilterButton
                      type="button"
                      variant="primary"
                      disabled={disabled}
                      onClick={() => onRemove?.(product)}
                    >
                      Kiểm duyệt
                    </AdminFilterButton>
                  )}
                </AdminDataTableCell>
              </AdminDataTableRow>
            );
          })}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
