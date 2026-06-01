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
    <aside className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-md lg:sticky lg:top-24">
      <h2 className="mb-4 border-b border-outline-variant pb-3 text-headline-md font-semibold text-on-surface">
        Đơn hàng của bạn
      </h2>

      {isLoading ? (
        <div className="space-y-3 animate-pulse">
          <div className="h-16 rounded bg-surface-container" />
          <div className="h-4 rounded bg-surface-container" />
          <div className="h-4 rounded bg-surface-container" />
        </div>
      ) : quote ? (
        <>
          <CheckoutOrderReview quote={quote} cartItemsCache={cartItemsCache} />

          <div className="space-y-2 text-sm">
            <div className="flex justify-between text-on-surface">
              <span>Tạm tính</span>
              <span>{formatVndPrice(quote.totalAmount)}</span>
            </div>
            <div className="flex justify-between text-on-surface">
              <span>Phí vận chuyển</span>
              <span>{formatVndPrice(quote.shippingFee)}</span>
            </div>
          </div>

          <div className="mt-4 border-t border-outline-variant pt-4">
            <div className="flex items-center justify-between">
              <span className="text-headline-sm font-semibold text-on-surface">Tổng thanh toán</span>
              <span className="text-headline-lg font-bold text-primary">
                {formatVndPrice(quote.finalAmount)}
              </span>
            </div>
            <p className="mt-2 text-xs text-on-surface-variant">{QUOTE_DISCLAIMER}</p>
          </div>
        </>
      ) : (
        <p className="text-sm text-on-surface-variant">
          Chọn địa chỉ giao hàng để xem tổng tiền.
        </p>
      )}

      <button
        type="button"
        disabled={!canSubmit || isSubmitting}
        onClick={onPlaceOrder}
        className="mt-6 flex w-full items-center justify-center gap-2 rounded-lg bg-primary-container py-3 text-sm font-bold text-on-primary shadow-sm transition-colors hover:bg-[#0050cb] disabled:cursor-not-allowed disabled:opacity-50"
      >
        {isSubmitting ? (
          <span className="h-5 w-5 animate-spin rounded-full border-2 border-on-primary border-t-transparent" />
        ) : (
          <>
            <span>Đặt hàng</span>
            <span className="material-symbols-outlined text-sm" aria-hidden="true">
              lock
            </span>
          </>
        )}
      </button>

      <p className="mt-3 flex items-center justify-center gap-1 text-xs text-on-surface-variant">
        <span className="material-symbols-outlined text-sm" aria-hidden="true">
          verified_user
        </span>
        Thanh toán an toàn
      </p>
    </aside>
  );
}
