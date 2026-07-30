import { getConditionLabel } from "../constants/productDetailConstants";
import {
  isAddToCartDisabled,
  isOwnListing,
} from "../utils/productDetailDisplay";

export function ProductDetailActionCard({
  product,
  productImageUrl,
  onAddToCart,
  onBuyNow,
  isAddingToCart = false,
  isBuyingNow = false,
  currentUserId = null,
}) {
  if (!product) return null;

  const ownListing = isOwnListing(product, currentUserId);
  const addDisabled = isAddToCartDisabled(product, currentUserId);
  const actionsDisabled = addDisabled || isAddingToCart || isBuyingNow;
  const conditionLabel = getConditionLabel(product.condition);

  return (
    <aside className="rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-6 shadow-xs">
      {/* Product Condition Explanation Box */}
      <div className="mb-5 rounded-xl border border-blue-100 bg-blue-50/50 p-4">
        <div className="flex items-center gap-1.5 text-xs font-bold text-blue-900">
          <span className="material-symbols-outlined text-sm text-primary">verified_user</span>
          Tình trạng sản phẩm 2Hand
        </div>
        <p className="mt-1 text-xs font-semibold text-slate-700">
          <span className="font-bold text-primary">{conditionLabel}</span> — Được đội ngũ kiểm duyệt kỹ lưỡng về độ mới & chất liệu trước khi đăng bán.
        </p>
      </div>

      {ownListing ? (
        <p className="mb-4 rounded-xl border border-amber-200 bg-amber-50 p-3 text-center text-xs font-bold text-amber-800">
          Đây là sản phẩm từ Cửa hàng của bạn — Không thể tự đặt mua.
        </p>
      ) : null}

      {/* Action Buttons Row */}
      <div className="flex flex-col gap-3 sm:flex-row">
        {/* Add to Cart Button */}
        <button
          type="button"
          disabled={actionsDisabled}
          onClick={(event) => {
            if (!actionsDisabled) {
              onAddToCart?.({
                imageUrl: productImageUrl,
                sourceElement: event.currentTarget,
              });
            }
          }}
          className="flex flex-1 items-center justify-center gap-2 rounded-xl border-2 border-primary py-3 text-xs font-bold text-primary transition-all hover:bg-primary/5 active:scale-95 disabled:cursor-not-allowed disabled:opacity-50 cursor-pointer"
        >
          <span className="material-symbols-outlined text-lg" aria-hidden="true">
            shopping_cart
          </span>
          {isAddingToCart ? "Đang thêm..." : "Thêm vào giỏ"}
        </button>

        {/* Buy Now Button */}
        <button
          type="button"
          disabled={actionsDisabled}
          onClick={() => {
            if (!actionsDisabled) onBuyNow?.();
          }}
          className="flex flex-1 items-center justify-center gap-2 rounded-xl bg-primary py-3 text-xs font-bold text-on-primary shadow-xs transition-all hover:bg-[#0050cb] active:scale-95 disabled:cursor-not-allowed disabled:opacity-50 cursor-pointer"
        >
          <span className="material-symbols-outlined text-lg" aria-hidden="true">
            bolt
          </span>
          {isBuyingNow ? "Đang chuyển..." : "Mua ngay"}
        </button>
      </div>
    </aside>
  );
}
