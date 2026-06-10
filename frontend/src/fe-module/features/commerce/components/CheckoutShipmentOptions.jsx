import { formatVndPrice } from "../../social/utils/formatPrice";
import { DEFAULT_SHIPMENT_LABEL } from "../constants/checkoutConstants";
import { formatDeliveryDate } from "../utils/formatDeliveryDate";

export function CheckoutShipmentOptions({ quote, shippingFee, isLoading = false }) {
  const estimatedDeliveryDate = shippingFee?.sellerGroups?.[0]?.estimatedDeliveryDate;

  return (
    <section className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
      <h2 className="mb-4 text-headline-sm font-semibold text-on-surface">Hình thức giao hàng</h2>
      <div className="rounded-lg border border-outline-variant bg-surface-container-low p-4">
        <div className="flex items-center justify-between gap-2">
          <span className="font-medium text-on-surface">{DEFAULT_SHIPMENT_LABEL}</span>
          <span className="text-sm font-semibold text-primary">
            {isLoading || !quote ? "—" : formatVndPrice(quote.shippingFee)}
          </span>
        </div>
        {estimatedDeliveryDate ? (
          <p className="mt-1 text-xs text-on-surface-variant">
            Dự kiến giao: {formatDeliveryDate(estimatedDeliveryDate)}
          </p>
        ) : null}
      </div>
    </section>
  );
}
