import { formatVndPrice } from "../../social/utils/formatPrice";
import { getLineTotal, isCartItemInvalid } from "../utils/cartDisplay";

export function CartOrderSummary({
  cart,
  selectedItems = [],
  onCheckout,
  isMutating = false,
  canCheckout,
}) {
  const summary = cart?.summary;
  const checkoutEnabled = canCheckout ?? summary?.canCheckout;
  const items = cart?.items || [];
  const invalidItems = items.filter(isCartItemInvalid);
  const invalidDisplayTotal = invalidItems.reduce((sum, item) => sum + getLineTotal(item), 0);
  const selectedSubtotal = selectedItems.reduce((sum, item) => sum + getLineTotal(item), 0);
  const selectedCount = selectedItems.length;

  return (
    <div className="rounded-2xl border border-outline-variant bg-surface-container-lowest p-5 shadow-xs lg:sticky lg:top-24">
      <h2 className="mb-4 border-b border-outline-variant/60 pb-3 text-base font-bold text-on-surface">
        Tóm tắt đơn hàng
      </h2>

      <div className="mb-4 space-y-2.5 text-xs">
        <div className="flex justify-between text-on-surface">
          <span className="text-on-surface-variant">
            Tạm tính
            {selectedCount > 0 ? ` (${selectedCount} sản phẩm đã chọn)` : ""}
          </span>
          <span className="font-bold text-on-surface">{formatVndPrice(selectedSubtotal)}</span>
        </div>

        {summary?.invalidItemCount > 0 ? (
          <div className="flex justify-between text-slate-400 line-through">
            <span>Sản phẩm không khả dụng ({summary.invalidItemCount})</span>
            <span>{formatVndPrice(invalidDisplayTotal)}</span>
          </div>
        ) : null}

        <div className="flex justify-between text-on-surface-variant">
          <span>Phí vận chuyển</span>
          <span className="italic text-slate-500">Tính khi thanh toán</span>
        </div>
      </div>

      <div className="mb-6 border-t border-outline-variant/60 pt-4">
        <div className="flex items-baseline justify-between">
          <span className="text-sm font-bold text-on-surface">Tổng thanh toán</span>
          <span className="text-xl font-black text-red-600">
            {formatVndPrice(selectedSubtotal)}
          </span>
        </div>
      </div>

      <button
        type="button"
        disabled={!checkoutEnabled || isMutating}
        onClick={onCheckout}
        className="flex w-full items-center justify-center gap-2 rounded-xl bg-primary py-3.5 text-xs font-bold text-on-primary shadow-xs transition-all hover:bg-[#0050cb] active:scale-95 disabled:cursor-not-allowed disabled:opacity-50 cursor-pointer"
      >
        <span>Tiến hành thanh toán</span>
        <span className="material-symbols-outlined text-base" aria-hidden="true">
          arrow_forward
        </span>
      </button>

      <div className="mt-4 flex items-center justify-center gap-1.5 rounded-xl bg-surface-container-low py-2 text-xs text-on-surface-variant">
        <span className="material-symbols-outlined text-sm text-emerald-600" aria-hidden="true">
          verified_user
        </span>
        <span className="font-semibold">Thanh toán an toàn 100% qua 2Hands</span>
      </div>
    </div>
  );
}
