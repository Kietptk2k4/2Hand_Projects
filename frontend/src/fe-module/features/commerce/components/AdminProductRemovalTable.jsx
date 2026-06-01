import { formatVndPrice } from "../../social/utils/formatPrice";
import { formatShortSellerId, formatShortShopId } from "../utils/formatShortShopId";
import { AdminProductStatusBadge } from "./AdminProductStatusBadge";

export function AdminProductRemovalTable({ items, disabled, onRemove, onViewCase }) {
  if (!items?.length) return null;

  return (
    <div className="overflow-x-auto">
      <table className="w-full border-collapse text-left">
        <thead>
          <tr className="border-b border-outline-variant bg-surface">
            <th className="p-4 text-label-sm font-semibold uppercase tracking-wide text-on-surface-variant">
              Sản phẩm
            </th>
            <th className="p-4 text-label-sm font-semibold uppercase tracking-wide text-on-surface-variant">
              Seller / Shop
            </th>
            <th className="p-4 text-label-sm font-semibold uppercase tracking-wide text-on-surface-variant">
              Danh mục
            </th>
            <th className="p-4 text-label-sm font-semibold uppercase tracking-wide text-on-surface-variant">
              Trạng thái
            </th>
            <th className="p-4 text-right text-label-sm font-semibold uppercase tracking-wide text-on-surface-variant">
              Thao tác
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-outline-variant">
          {items.map((product) => {
            const isRemoved = product.status === "REMOVED";
            const shopRef = formatShortShopId(product.shopId);
            const sellerRef = formatShortSellerId(product.sellerId);

            return (
              <tr
                key={product.productId}
                className={[
                  "group transition-colors hover:bg-surface-container-low/40",
                  isRemoved ? "bg-error-container/10" : "",
                ].join(" ")}
              >
                <td className="p-4">
                  <div className="flex items-center gap-4">
                    <div className="flex h-12 w-12 shrink-0 items-center justify-center overflow-hidden rounded-lg border border-outline-variant bg-surface-container-high">
                      {product.thumbnailUrl ? (
                        <img
                          src={product.thumbnailUrl}
                          alt=""
                          className="h-full w-full object-cover"
                        />
                      ) : (
                        <span className="material-symbols-outlined text-on-surface-variant">
                          inventory_2
                        </span>
                      )}
                    </div>
                    <div className="min-w-0">
                      <p className="line-clamp-1 text-label-md font-medium text-on-surface group-hover:text-primary">
                        {product.title || "—"}
                      </p>
                      <p className="mt-0.5 text-body-sm text-on-surface-variant">
                        {formatVndPrice(product.effectivePrice ?? product.price)}
                      </p>
                    </div>
                  </div>
                </td>
                <td className="p-4 text-body-sm text-on-surface-variant">
                  <p title={product.sellerId}>
                    <span className="font-mono">{sellerRef}</span>
                  </p>
                  <p className="mt-0.5 font-mono text-label-sm" title={product.shopId}>
                    {shopRef}
                  </p>
                  {product.shopName ? (
                    <p className="mt-0.5 line-clamp-1 text-label-sm">{product.shopName}</p>
                  ) : null}
                </td>
                <td className="p-4 text-body-sm text-on-surface-variant">
                  {product.categoryName || "—"}
                </td>
                <td className="p-4">
                  <AdminProductStatusBadge status={product.status} />
                </td>
                <td className="p-4 text-right">
                  {isRemoved ? (
                    <button
                      type="button"
                      disabled={disabled}
                      onClick={() => onViewCase?.(product)}
                      className="inline-flex items-center gap-1 rounded-lg border border-outline-variant px-4 py-2 text-label-md text-on-surface-variant transition-colors hover:border-primary hover:text-primary disabled:opacity-50"
                    >
                      <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
                        folder_open
                      </span>
                      Xem hồ sơ
                    </button>
                  ) : (
                    <button
                      type="button"
                      disabled={disabled}
                      onClick={() => onRemove?.(product)}
                      className="inline-flex items-center gap-1 rounded-lg border border-primary px-4 py-2 text-label-md font-medium text-primary transition-colors hover:bg-primary/5 disabled:opacity-50"
                    >
                      <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
                        gavel
                      </span>
                      Kiểm duyệt
                    </button>
                  )}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
