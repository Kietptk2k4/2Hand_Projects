import { formatVndPrice } from "../../social/utils/formatPrice";
import { QUOTE_DISCLAIMER } from "../constants/checkoutConstants";
import { CheckoutOrderReview } from "./CheckoutOrderReview";

export function CheckoutQuoteSummary({
  quote,
  cartItemsCache,
  isLoading = false,
  onPlaceOrder,
  canSubmit = false,
  isSubmitting = false,
}) {
  return (
    <aside className="rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-6 shadow-xs sm:p-7 lg:sticky lg:top-24">
      <h2 className="mb-4 border-b border-outline-variant/60 pb-3.5 text-base font-black text-on-surface sm:text-lg flex items-center gap-2">
        <span className="material-symbols-outlined text-primary text-xl" aria-hidden="true">
          receipt_long
        </span>
        ĐƠN HÀNG CỦA BẠN
      </h2>

      {isLoading ? (
        <div className="space-y-3 animate-pulse">
          <div className="h-16 rounded-xl bg-surface-container" />
          <div className="h-4 rounded bg-surface-container" />
          <div className="h-4 rounded bg-surface-container" />
        </div>
      ) : quote ? (
        <>
          <CheckoutOrderReview quote={quote} cartItemsCache={cartItemsCache} />

          <div className="space-y-2.5 text-xs font-semibold sm:text-sm">
            <div className="flex justify-between text-on-surface-variant">
              <span>Tạm tính</span>
              <span className="font-bold text-on-surface">{formatVndPrice(quote.totalAmount)}</span>
            </div>
            <div className="flex justify-between text-on-surface-variant">
              <span>Phí vận chuyển</span>
              <span className="font-bold text-on-surface">{formatVndPrice(quote.shippingFee)}</span>
            </div>
          </div>

          <div className="mt-4 border-t border-outline-variant/60 pt-4">
            <div className="flex items-center justify-between">
              <span className="text-sm font-black text-on-surface sm:text-base">Tổng thanh toán</span>
              <span className="text-xl font-black text-red-600 sm:text-2xl">
                {formatVndPrice(quote.finalAmount)}
              </span>
            </div>
            <p className="mt-2 text-[11px] font-semibold text-on-surface-variant">{QUOTE_DISCLAIMER}</p>
          </div>
        </>
      ) : (
        <p className="text-xs font-semibold text-on-surface-variant">
          Vui lòng chọn địa chỉ giao hàng để tính toán chính xác tổng tiền đơn hàng.
        </p>
      )}

      {/* Primary High-Contrast Order Button */}
      <button
        type="button"
        disabled={!canSubmit || isSubmitting}
        onClick={onPlaceOrder}
        className="mt-6 flex w-full items-center justify-center gap-2 rounded-xl bg-primary py-3.5 text-xs font-black text-on-primary shadow-md transition-all hover:bg-[#0050cb] active:scale-95 disabled:cursor-not-allowed disabled:opacity-50 cursor-pointer"
      >
        {isSubmitting ? (
          <span className="h-5 w-5 animate-spin rounded-full border-2 border-on-primary border-t-transparent" />
        ) : (
          <>
            <span>Đặt hàng ngay</span>
            <span className="material-symbols-outlined text-base" aria-hidden="true">
              lock
            </span>
          </>
        )}
      </button>

      <p className="mt-3 flex items-center justify-center gap-1.5 text-xs font-bold text-emerald-700">
        <span className="material-symbols-outlined text-sm text-emerald-600" aria-hidden="true">
          verified_user
        </span>
        Thanh toán an toàn 100% qua 2Hands
      </p>
    </aside>
  );
}
