import { getConditionLabel } from "../constants/productDetailConstants";
import { formatVndPrice } from "../../social/utils/formatPrice";
import { getStockLabel, isAddToCartDisabled, isProductOnSale } from "../utils/productDetailDisplay";

export function ProductDetailActionCard({ product, onComingSoon }) {
  if (!product) return null;

  const addDisabled = isAddToCartDisabled(product);
  const isOnSale = isProductOnSale(product);
  const stockLabel = getStockLabel(product);

  return (
    <aside className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm lg:sticky lg:top-24">
      <div className="mb-4">
        <span
          className={`text-headline-md font-bold ${isOnSale ? "text-error" : "text-on-surface"}`}
        >
          {formatVndPrice(product.effectivePrice)}
        </span>
        {isOnSale ? (
          <div className="text-label-sm text-outline-variant line-through">
            {formatVndPrice(product.price)}
          </div>
        ) : null}
      </div>

      <p className="mb-4 text-body-sm text-on-surface-variant">{stockLabel}</p>

      <div className="mb-4 rounded-lg bg-surface-container-low p-3">
        <p className="text-label-sm font-medium text-on-surface">Tình trạng sản phẩm</p>
        <p className="mt-1 text-body-sm text-on-surface-variant">
          {getConditionLabel(product.condition)} — sản phẩm được kiểm tra trước khi đăng bán.
        </p>
      </div>

      <button
        type="button"
        disabled={addDisabled}
        onClick={() => {
          if (!addDisabled) onComingSoon?.();
        }}
        className="mb-3 w-full rounded-lg bg-primary py-3 text-label-md font-semibold text-on-primary transition-colors hover:bg-[#0050cb] disabled:cursor-not-allowed disabled:opacity-50"
      >
        Thêm vào giỏ
      </button>

      <div className="flex gap-2">
        <button
          type="button"
          onClick={onComingSoon}
          className="flex flex-1 items-center justify-center gap-1 rounded-lg border border-outline-variant py-2 text-label-md text-on-surface transition-colors hover:bg-surface-container-low"
        >
          <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
            favorite
          </span>
          Lưu
        </button>
        <button
          type="button"
          onClick={onComingSoon}
          className="flex flex-1 items-center justify-center gap-1 rounded-lg border border-outline-variant py-2 text-label-md text-on-surface transition-colors hover:bg-surface-container-low"
        >
          <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
            share
          </span>
          Chia sẻ
        </button>
      </div>

      <button
        type="button"
        onClick={onComingSoon}
        className="mt-3 w-full rounded-lg border-2 border-primary py-2 text-label-md font-medium text-primary transition-colors hover:bg-surface-container-low"
      >
        Đề xuất giá
      </button>
    </aside>
  );
}
