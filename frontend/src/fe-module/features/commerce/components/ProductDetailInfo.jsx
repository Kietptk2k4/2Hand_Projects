import { formatVndPrice } from "../../social/utils/formatPrice";
import { getStockLabel, isProductOnSale } from "../utils/productDetailDisplay";

export function ProductDetailInfo({ product }) {
  if (!product) return null;

  const isOnSale = isProductOnSale(product);
  const stockLabel = getStockLabel(product);

  return (
    <div>
      <h1 className="text-headline-lg-mobile font-bold text-on-surface md:text-headline-xl">
        {product.title}
      </h1>

      <div className="mt-3 flex flex-wrap items-center gap-3 text-body-sm text-on-surface-variant">
        {product.ratingCount > 0 ? (
          <span className="flex items-center gap-1 text-primary">
            <span className="material-symbols-outlined fill text-[18px]" aria-hidden="true">
              star
            </span>
            {product.ratingAvg} · {product.ratingCount} đánh giá
          </span>
        ) : null}
        {stockLabel ? (
          <>
            <span aria-hidden="true">·</span>
            <span className="flex items-center gap-1">
              <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
                inventory_2
              </span>
              {stockLabel}
            </span>
          </>
        ) : null}
      </div>

      <div className="mt-6 flex flex-wrap items-end gap-3">
        <span
          className={`text-headline-lg font-bold ${isOnSale ? "text-error" : "text-on-surface"}`}
        >
          {formatVndPrice(product.effectivePrice)}
        </span>
        {isOnSale ? (
          <span className="text-body-lg text-outline-variant line-through">
            {formatVndPrice(product.price)}
          </span>
        ) : null}
      </div>
    </div>
  );
}
