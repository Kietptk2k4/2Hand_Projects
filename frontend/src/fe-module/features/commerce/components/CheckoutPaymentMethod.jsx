import { CHECKOUT_COD_ONLY_ENABLED, PAYMENT_METHODS } from "../constants/checkoutConstants";

export function CheckoutPaymentMethod({ paymentMethod, disabled = false, onSelect }) {
  return (
    <section className="rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-6 shadow-xs sm:p-7">
      <h2 className="mb-4 text-base font-black text-on-surface sm:text-lg flex items-center gap-2 border-b border-outline-variant/60 pb-3.5">
        <span className="material-symbols-outlined text-primary text-xl" aria-hidden="true">
          payments
        </span>
        PHƯƠNG THỨC THANH TOÁN
      </h2>
      {CHECKOUT_COD_ONLY_ENABLED ? (
        <p className="mb-4 text-xs font-semibold text-on-surface-variant">
          Hiện chỉ hỗ trợ thanh toán khi nhận hàng (COD). Bạn thanh toán trực tiếp cho shipper khi nhận hàng.
        </p>
      ) : (
        <p className="mb-4 text-xs font-semibold text-on-surface-variant">
          Chọn VNPay để thanh toán online ngay sau khi đặt hàng, hoặc COD để thanh toán khi nhận hàng.
        </p>
      )}
      <div className="flex flex-col gap-3">
        {PAYMENT_METHODS.map((option) => {
          const selected = paymentMethod === option.value;
          return (
            <label
              key={option.value}
              className={[
                "flex cursor-pointer items-center gap-3.5 rounded-2xl border p-4 transition-all shadow-xs",
                selected
                  ? "border-primary bg-primary/5 ring-2 ring-primary/20"
                  : "border-outline-variant/80 bg-surface-container-lowest hover:bg-surface-container-low/50",
                disabled ? "pointer-events-none opacity-60" : "",
              ].join(" ")}
            >
              <input
                type="radio"
                name="checkout-payment"
                checked={selected}
                disabled={disabled}
                onChange={() => onSelect?.(option.value)}
                className="h-4 w-4 border-outline text-primary focus:ring-primary cursor-pointer"
              />
              <div className="flex items-center gap-2">
                <span className="material-symbols-outlined text-primary text-lg" aria-hidden="true">
                  {option.value === "COD" ? "local_atm" : "credit_card"}
                </span>
                <span className="text-xs font-bold text-on-surface sm:text-sm">{option.label}</span>
              </div>
            </label>
          );
        })}
      </div>
    </section>
  );
}
