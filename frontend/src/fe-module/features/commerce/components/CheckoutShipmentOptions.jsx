import { formatVndPrice } from "../../social/utils/formatPrice";
import { DEFAULT_SHIPMENT_LABEL } from "../constants/checkoutConstants";
import { formatDeliveryDate } from "../utils/formatDeliveryDate";

export function CheckoutShipmentOptions({ quote, shippingFee, isLoading = false }) {
  const estimatedDeliveryDate = shippingFee?.sellerGroups?.[0]?.estimatedDeliveryDate;

  return (
    <section className="rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-6 shadow-xs sm:p-7">
      <h2 className="mb-4 text-base font-black text-on-surface sm:text-lg flex items-center gap-2 border-b border-outline-variant/60 pb-3.5">
        <span className="material-symbols-outlined text-primary text-xl" aria-hidden="true">
          local_shipping
        </span>
        HÌNH THỨC GIAO HÀNG
      </h2>
      <div className="rounded-2xl border border-outline-variant/80 bg-surface-container-low/50 p-4">
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <span className="material-symbols-outlined text-emerald-600 text-lg" aria-hidden="true">
              verified
            </span>
            <span className="text-xs font-bold text-on-surface sm:text-sm">{DEFAULT_SHIPMENT_LABEL}</span>
          </div>
          <span className="text-xs font-black text-primary sm:text-sm">
            {isLoading || !quote ? "—" : formatVndPrice(quote.shippingFee)}
          </span>
        </div>
        {estimatedDeliveryDate ? (
          <p className="mt-2 text-xs font-semibold text-on-surface-variant flex items-center gap-1">
            <span className="material-symbols-outlined text-sm">schedule</span>
            Dự kiến giao hàng: {formatDeliveryDate(estimatedDeliveryDate)}
          </p>
        ) : null}
      </div>
    </section>
  );
}
