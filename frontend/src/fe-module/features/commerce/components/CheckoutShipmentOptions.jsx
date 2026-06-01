import { formatVndPrice } from "../../social/utils/formatPrice";
import {
  SHIPMENT_TYPES,
  SHIPPING_FEE_HINT_PER_SELLER,
} from "../constants/checkoutConstants";
import { formatDeliveryDate } from "../utils/formatDeliveryDate";

export function CheckoutShipmentOptions({
  shipmentType,
  quote,
  shippingFee,
  disabled = false,
  onSelect,
}) {
  const sellerCount = quote?.sellerShippingGroups?.length || shippingFee?.sellerGroups?.length || 1;

  const etaByType = {};
  for (const group of shippingFee?.sellerGroups || []) {
    etaByType[group.shipmentType || shipmentType] = group.estimatedDeliveryDate;
  }

  return (
    <section className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
      <h2 className="mb-4 text-headline-sm font-semibold text-on-surface">Hình thức giao hàng</h2>
      <div className="flex flex-col gap-3">
        {SHIPMENT_TYPES.map((option) => {
          const selected = shipmentType === option.value;
          const hintFee = (SHIPPING_FEE_HINT_PER_SELLER[option.value] || 0) * sellerCount;
          const displayFee =
            selected && quote ? formatVndPrice(quote.shippingFee) : `từ ${formatVndPrice(hintFee)}`;

          const eta =
            selected && shippingFee?.sellerGroups?.[0]?.estimatedDeliveryDate
              ? shippingFee.sellerGroups[0].estimatedDeliveryDate
              : etaByType[option.value];

          return (
            <label
              key={option.value}
              className={[
                "flex cursor-pointer items-start gap-3 rounded-lg border p-4 transition-colors",
                selected
                  ? "border-primary bg-surface-container-low"
                  : "border-outline-variant hover:border-primary/50",
                disabled ? "pointer-events-none opacity-60" : "",
              ].join(" ")}
            >
              <input
                type="radio"
                name="checkout-shipment"
                checked={selected}
                disabled={disabled}
                onChange={() => onSelect?.(option.value)}
                className="mt-1 h-4 w-4 border-outline text-primary focus:ring-primary"
              />
              <div className="flex-1">
                <div className="flex items-center justify-between gap-2">
                  <span className="font-medium text-on-surface">{option.label}</span>
                  <span className="text-sm font-semibold text-primary">{displayFee}</span>
                </div>
                {eta ? (
                  <p className="mt-1 text-xs text-on-surface-variant">
                    Dự kiến giao: {formatDeliveryDate(eta)}
                  </p>
                ) : null}
              </div>
            </label>
          );
        })}
      </div>
    </section>
  );
}
