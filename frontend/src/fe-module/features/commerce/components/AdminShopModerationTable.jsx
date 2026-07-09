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
import { AdminShopStatusBadge } from "./AdminShopStatusBadge";

function formatCreatedDate(iso) {
  if (!iso) return "—";
  try {
    return new Date(iso).toLocaleDateString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  } catch {
    return iso;
  }
}

export function AdminShopModerationTable({ items, disabled, onModerate }) {
  if (!items?.length) return null;

  return (
    <>
      <AdminMobileCardList>
        {items.map((shop) => (
          <AdminMobileCard key={shop.shopId}>
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
                <p className="line-clamp-2 text-sm font-medium text-admin-text">{shop.shopName}</p>
                <p className="mt-0.5 font-mono text-xs text-admin-text-muted">
                  {formatShortShopId(shop.shopId)}
                </p>
                <div className="mt-2 flex flex-wrap items-center gap-2">
                  <AdminShopStatusBadge status={shop.status} />
                  <span className="text-xs text-admin-text-secondary">
                    Seller {formatShortSellerId(shop.sellerId)}
                  </span>
                </div>
                <p className="mt-1 text-xs text-admin-text-muted">
                  Tạo {formatCreatedDate(shop.createdAt)}
                </p>
              </div>
            </div>
            <AdminFilterButton
              type="button"
              variant="secondary"
              className="mt-3 w-full"
              disabled={disabled}
              onClick={() => onModerate?.(shop)}
            >
              <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
                gavel
              </span>
              Kiểm duyệt
            </AdminFilterButton>
          </AdminMobileCard>
        ))}
      </AdminMobileCardList>

      <AdminDataTable minWidth="760px" ariaLabel="Danh sách shop kiểm duyệt">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Thông tin shop</AdminDataTableCell>
            <AdminDataTableCell header>Seller ID</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header>Ngày tạo</AdminDataTableCell>
            <AdminDataTableCell header className="text-right">
              Thao tác
            </AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((shop) => (
            <AdminDataTableRow key={shop.shopId}>
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
                  <div className="min-w-0">
                    <p className="line-clamp-2 text-sm font-medium text-admin-text">{shop.shopName}</p>
                    <p className="font-mono text-xs text-admin-text-muted">
                      {formatShortShopId(shop.shopId)}
                    </p>
                  </div>
                </div>
              </AdminDataTableCell>
              <AdminDataTableCell className="font-mono text-xs text-admin-text-secondary">
                {formatShortSellerId(shop.sellerId)}
              </AdminDataTableCell>
              <AdminDataTableCell>
                <AdminShopStatusBadge status={shop.status} />
              </AdminDataTableCell>
              <AdminDataTableCell className="text-sm text-admin-text-secondary">
                {formatCreatedDate(shop.createdAt)}
              </AdminDataTableCell>
              <AdminDataTableCell className="text-right">
                <AdminFilterButton
                  type="button"
                  variant="secondary"
                  disabled={disabled}
                  onClick={() => onModerate?.(shop)}
                >
                  <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
                    gavel
                  </span>
                  Kiểm duyệt
                </AdminFilterButton>
              </AdminDataTableCell>
            </AdminDataTableRow>
          ))}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
