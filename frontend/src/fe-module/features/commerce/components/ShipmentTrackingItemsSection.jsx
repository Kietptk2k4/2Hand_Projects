import { formatVndPrice } from "../../social/utils/formatPrice";
import { ITEM_STATUS_BADGE_CLASS, ITEM_STATUS_LABELS } from "../constants/orderDetailConstants";

function parseAttributes(attributesSnapshot) {
  if (!attributesSnapshot) return null;
  try {
    const parsed = JSON.parse(attributesSnapshot);
    if (!parsed || typeof parsed !== "object") return null;
    return Object.entries(parsed)
      .map(([key, value]) => `${key}: ${value}`)
      .join(" · ");
  } catch {
    return attributesSnapshot;
  }
}

export function ShipmentTrackingItemsSection({ items }) {
  if (!items?.length) return null;

  return (
    <section className="overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-sm">
      <div className="border-b border-outline-variant bg-surface-bright px-4 py-3 md:px-6">
        <h2 className="text-headline-sm font-semibold text-on-surface">Sản phẩm trong lô</h2>
        <p className="text-body-sm text-on-surface-variant">{items.length} sản phẩm</p>
      </div>

      <div className="flex flex-col gap-4 p-4 md:p-6">
        {items.map((item) => {
          const statusLabel = ITEM_STATUS_LABELS[item.status] || item.status;
          const statusClass =
            ITEM_STATUS_BADGE_CLASS[item.status] ||
            "bg-surface-container-high text-on-surface-variant";
          const attributesText = parseAttributes(item.attributesSnapshot);

          return (
            <div
              key={item.orderItemId}
              className="flex flex-col gap-3 border-b border-outline-variant pb-4 last:border-0 last:pb-0 sm:flex-row sm:items-start"
            >
              <div className="h-20 w-20 shrink-0 overflow-hidden rounded-lg border border-outline-variant bg-surface-container-high">
                {item.imageSnapshot ? (
                  <img
                    src={item.imageSnapshot}
                    alt=""
                    className="h-full w-full object-cover"
                    loading="lazy"
                  />
                ) : (
                  <div className="flex h-full w-full items-center justify-center">
                    <span className="material-symbols-outlined text-2xl text-outline" aria-hidden="true">
                      inventory_2
                    </span>
                  </div>
                )}
              </div>

              <div className="min-w-0 flex-1">
                <h3 className="text-body-md font-medium text-on-surface">
                  {item.productNameSnapshot}
                </h3>
                {item.shopNameSnapshot ? (
                  <p className="mt-0.5 text-body-sm text-on-surface-variant">
                    {item.shopNameSnapshot}
                  </p>
                ) : null}
                {attributesText ? (
                  <p className="mt-1 text-body-sm text-on-surface-variant">{attributesText}</p>
                ) : null}
                <p className="mt-2 text-label-md text-on-surface">
                  Số lượng: {item.quantity}
                  {item.finalPrice != null ? (
                    <span className="ml-3 font-semibold">{formatVndPrice(item.finalPrice)}</span>
                  ) : null}
                </p>
              </div>

              <span className={`self-start rounded-full px-2.5 py-0.5 text-label-sm ${statusClass}`}>
                {statusLabel}
              </span>
            </div>
          );
        })}
      </div>
    </section>
  );
}
