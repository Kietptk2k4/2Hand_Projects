import { formatVndPrice } from "../../social/utils/formatPrice";

export function CheckoutOrderReview({ quote, cartItemsCache = [] }) {
  if (!quote?.items?.length) return null;

  const cacheById = new Map(cartItemsCache.map((item) => [item.cartItemId, item]));

  return (
    <div className="mb-4 space-y-3 border-b border-outline-variant pb-4">
      {quote.items.map((line) => {
        const cached = cacheById.get(line.cartItemId);
        return (
          <div key={line.cartItemId} className="flex gap-3">
            <div className="h-14 w-14 shrink-0 overflow-hidden rounded-lg bg-surface-container">
              {cached?.imageUrl ? (
                <img
                  src={cached.imageUrl}
                  alt={cached.productName || ""}
                  className="h-full w-full object-cover"
                />
              ) : (
                <div className="flex h-full w-full items-center justify-center">
                  <span className="material-symbols-outlined text-outline" aria-hidden="true">
                    inventory_2
                  </span>
                </div>
              )}
            </div>
            <div className="min-w-0 flex-1">
              <p className="line-clamp-2 text-sm font-medium text-on-surface">
                {cached?.productName || `Sản phẩm ${line.cartItemId.slice(-8)}`}
              </p>
              <p className="text-xs text-on-surface-variant">
                SL: {line.quantity} × {formatVndPrice(line.unitPrice)}
              </p>
            </div>
            <p className="text-sm font-semibold text-on-surface">
              {formatVndPrice(line.itemTotal)}
            </p>
          </div>
        );
      })}
    </div>
  );
}
