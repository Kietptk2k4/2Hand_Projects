import { CHECKOUT_COD_ONLY_ENABLED, PAYMENT_METHODS } from "../constants/checkoutConstants";

export function CheckoutPaymentMethod({ paymentMethod, disabled = false, onSelect }) {
  return (
    <section className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
      <h2 className="mb-4 text-headline-sm font-semibold text-on-surface">Phương thức thanh toán</h2>
      {CHECKOUT_COD_ONLY_ENABLED ? (
        <p className="mb-4 text-body-sm text-on-surface-variant">
          Hiện chỉ hỗ trợ thanh toán khi nhận hàng (COD). Bạn thanh toán trực tiếp cho shipper khi nhận hàng.
        </p>
      ) : null}
      <div className="flex flex-col gap-3">
        {PAYMENT_METHODS.map((option) => {
          const selected = paymentMethod === option.value;
          return (
            <label
              key={option.value}
              className={[
                "flex cursor-pointer items-center gap-3 rounded-lg border p-4 transition-colors",
                selected
                  ? "border-primary bg-surface-container-low"
                  : "border-outline-variant hover:border-primary/50",
                disabled ? "pointer-events-none opacity-60" : "",
              ].join(" ")}
            >
              <input
                type="radio"
                name="checkout-payment"
                checked={selected}
                disabled={disabled}
                onChange={() => onSelect?.(option.value)}
                className="h-4 w-4 border-outline text-primary focus:ring-primary"
              />
              <span className="font-medium text-on-surface">{option.label}</span>
            </label>
          );
        })}
      </div>
    </section>
  );
}
