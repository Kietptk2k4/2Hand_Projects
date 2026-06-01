import { formatShortSellerId, formatShortShopId } from "../utils/formatShortShopId";
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
    <div className="overflow-x-auto">
      <table className="w-full border-collapse text-left">
        <thead>
          <tr className="border-b border-outline-variant bg-surface-container-low">
            <th className="p-4 text-label-sm font-semibold uppercase tracking-wide text-on-surface-variant">
              Thông tin shop
            </th>
            <th className="p-4 text-label-sm font-semibold uppercase tracking-wide text-on-surface-variant">
              Seller ID
            </th>
            <th className="p-4 text-label-sm font-semibold uppercase tracking-wide text-on-surface-variant">
              Trạng thái
            </th>
            <th className="p-4 text-label-sm font-semibold uppercase tracking-wide text-on-surface-variant">
              Ngày tạo
            </th>
            <th className="p-4 text-right text-label-sm font-semibold uppercase tracking-wide text-on-surface-variant">
              Thao tác
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-outline-variant">
          {items.map((shop) => (
            <tr
              key={shop.shopId}
              className="group transition-colors hover:bg-surface-container-low/40"
            >
              <td className="p-4">
                <div className="flex items-center gap-3">
                  <div className="flex h-10 w-10 shrink-0 items-center justify-center overflow-hidden rounded-lg border border-outline-variant bg-surface-container-high">
                    {shop.logoUrl ? (
                      <img src={shop.logoUrl} alt="" className="h-full w-full object-cover" />
                    ) : (
                      <span className="material-symbols-outlined text-on-surface-variant">
                        storefront
                      </span>
                    )}
                  </div>
                  <div className="min-w-0">
                    <p className="line-clamp-2 text-label-md font-medium text-on-surface group-hover:text-primary">
                      {shop.shopName}
                    </p>
                    <p className="font-mono text-body-sm text-on-surface-variant">
                      {formatShortShopId(shop.shopId)}
                    </p>
                  </div>
                </div>
              </td>
              <td className="p-4 font-mono text-body-sm text-on-surface-variant">
                {formatShortSellerId(shop.sellerId)}
              </td>
              <td className="p-4">
                <AdminShopStatusBadge status={shop.status} />
              </td>
              <td className="p-4 text-body-sm text-on-surface-variant">
                {formatCreatedDate(shop.createdAt)}
              </td>
              <td className="p-4 text-right">
                <button
                  type="button"
                  disabled={disabled}
                  onClick={() => onModerate?.(shop)}
                  className="inline-flex items-center gap-1 rounded-lg border border-outline-variant px-3 py-1.5 text-label-sm font-medium text-on-surface transition-colors hover:border-primary hover:text-primary disabled:opacity-50"
                >
                  <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
                    gavel
                  </span>
                  Moderate
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
