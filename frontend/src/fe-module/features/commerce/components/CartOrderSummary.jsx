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
    <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-4 shadow-md lg:sticky lg:top-24">
      <h2 className="mb-4 border-b border-outline-variant pb-3 text-headline-md font-semibold text-on-surface">
        Tóm tắt đơn hàng
      </h2>

      <div className="mb-4 space-y-3">
        <div className="flex justify-between text-sm text-on-surface">
          <span>
            Tạm tính
            {selectedCount > 0 ? ` (${selectedCount} sản phẩm đã chọn)` : ""}
          </span>
          <span>{formatVndPrice(selectedSubtotal)}</span>
        </div>

        {summary?.invalidItemCount > 0 ? (
          <div className="flex justify-between text-sm text-on-surface-variant line-through">
            <span>Sản phẩm không khả dụng ({summary.invalidItemCount})</span>
            <span>{formatVndPrice(invalidDisplayTotal)}</span>
          </div>
        ) : null}
      </div>

      <div className="mb-6 border-t border-outline-variant pt-4">
        <div className="flex items-center justify-between">
          <span className="text-headline-sm font-semibold text-on-surface">Tổng cộng</span>
          <span className="text-headline-lg font-semibold text-on-surface">
            {formatVndPrice(selectedSubtotal)}
          </span>
        </div>
        <p className="mt-1 text-right text-xs text-on-surface-variant">
          Phí vận chuyển tính khi thanh toán
        </p>
      </div>

      <button
        type="button"
        disabled={!checkoutEnabled || isMutating}
        onClick={onCheckout}
        className="flex w-full items-center justify-center gap-2 rounded-lg bg-primary-container py-3 text-sm font-bold text-on-primary shadow-sm transition-colors hover:bg-[#0050cb] disabled:cursor-not-allowed disabled:opacity-50"
      >
        <span>Tiến hành thanh toán</span>
        <span className="material-symbols-outlined text-sm" aria-hidden="true">
          arrow_forward
        </span>
      </button>

      <div className="mt-4 flex items-center justify-center gap-1 text-on-surface-variant">
        <span className="material-symbols-outlined text-sm" aria-hidden="true">
          lock
        </span>
        <span className="text-xs">Thanh toán an toàn</span>
      </div>
    </div>
  );
}
