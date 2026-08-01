import { formatVndPrice } from "../../social/utils/formatPrice";
import { getStockLabel, isProductOnSale } from "../utils/productDetailDisplay";

export function ProductDetailInfo({ product, onOpenReviews }) {
  if (!product) return null;

  const isOnSale = isProductOnSale(product);
  const stockLabel = getStockLabel(product);
  const discountPercent =
    isOnSale && product.price > 0
      ? Math.round(((product.price - product.effectivePrice) / product.price) * 100)
      : 0;

  return (
    <div>
      <h1 className="text-xl font-black text-on-surface sm:text-2xl lg:text-3xl leading-snug">
        {product.title}
      </h1>

      {/* Meta Bar: Rating & Stock */}
      <div className="mt-3 flex flex-wrap items-center gap-3 text-xs font-semibold text-on-surface-variant sm:text-sm">
        {onOpenReviews ? (
          <button
            type="button"
            onClick={onOpenReviews}
            className="flex items-center gap-1.5 text-primary transition-opacity hover:opacity-80 cursor-pointer"
          >
            <span className="material-symbols-outlined fill text-amber-500 text-base" aria-hidden="true">
              star
            </span>
            {product.ratingCount > 0 ? (
              <span className="font-bold">
                {product.ratingAvg} <span className="text-slate-400 font-normal">({product.ratingCount} đánh giá)</span>
              </span>
            ) : (
              <span className="font-bold underline">Chưa có đánh giá</span>
            )}
          </button>
        ) : null}

        {stockLabel ? (
          <>
            <span className="text-slate-300" aria-hidden="true">•</span>
            <span className="flex items-center gap-1 font-bold text-emerald-600">
              <span className="material-symbols-outlined text-base" aria-hidden="true">
                check_circle
              </span>
              {stockLabel}
            </span>
          </>
        ) : null}
      </div>

      {/* Shopee-style Price Banner */}
      <div className="mt-5 rounded-2xl border border-rose-100 bg-rose-50/60 p-4 sm:p-5 shadow-xs">
        <div className="flex flex-wrap items-baseline gap-3">
          <span className="text-2xl font-black text-red-600 sm:text-3xl">
            {formatVndPrice(product.effectivePrice)}
          </span>
          {isOnSale ? (
            <>
              <span className="text-sm font-semibold text-slate-400 line-through sm:text-base">
                {formatVndPrice(product.price)}
              </span>
              <span className="rounded-md bg-red-600 px-2 py-0.5 text-xs font-bold text-white shadow-xs">
                -{discountPercent}%
              </span>
            </>
          ) : null}
        </div>

        <div className="mt-2.5 flex items-center gap-1.5 text-xs font-bold text-amber-800">
          <span className="material-symbols-outlined text-sm" aria-hidden="true">
            verified
          </span>
          <span>Bảo vệ Người Mua 100% — Kiểm tra hàng trước khi nhận</span>
        </div>
      </div>
    </div>
  );
}
