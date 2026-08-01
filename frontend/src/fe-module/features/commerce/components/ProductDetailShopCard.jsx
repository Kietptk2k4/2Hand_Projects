import { useState } from "react";
import { ShopVacationBanner } from "./ShopVacationBanner";

const FALLBACK_SHOP_AVATAR = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80";

export function ProductDetailShopCard({ product, onVisitShop, onViewAllReviews }) {
  const [imgError, setImgError] = useState(false);
  if (!product?.shop) return null;

  const { shop } = product;
  const avatarSrc = !imgError && shop.avatarUrl ? shop.avatarUrl : FALLBACK_SHOP_AVATAR;

  return (
    <aside className="rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-5 shadow-xs">
      {product.shopVacation ? (
        <div className="mb-4">
          <ShopVacationBanner message={product.vacationMessage} />
        </div>
      ) : null}

      <div className="flex items-center justify-between gap-4">
        <div className="flex items-center gap-3.5 min-w-0">
          <div className="h-14 w-14 shrink-0 overflow-hidden rounded-2xl border border-outline-variant/60 bg-surface-container-low shadow-xs">
            <img
              src={avatarSrc}
              alt={shop.shopName}
              onError={() => setImgError(true)}
              className="h-full w-full object-cover"
            />
          </div>
          <div className="min-w-0">
            <div className="flex items-center gap-1.5">
              <span className="material-symbols-outlined text-primary text-base" aria-hidden="true">
                storefront
              </span>
              <p className="truncate text-sm font-black text-on-surface sm:text-base">
                {shop.shopName}
              </p>
            </div>
            {product.ratingCount > 0 ? (
              <p className="mt-1 text-xs font-semibold text-on-surface-variant flex items-center gap-1">
                <span className="material-symbols-outlined text-amber-500 text-sm fill">star</span>
                <span className="font-bold text-on-surface">{product.ratingAvg}</span>
                <span>({product.ratingCount} đánh giá shop)</span>
              </p>
            ) : (
              <p className="mt-1 text-xs font-semibold text-slate-400">Cửa hàng chính hãng 2Hands</p>
            )}
          </div>
        </div>

        {/* Action Buttons Right Side */}
        <div className="flex shrink-0 items-center gap-2">
          <button
            type="button"
            onClick={() => onVisitShop?.(shop.shopId)}
            className="rounded-xl border border-primary bg-primary/5 px-4 py-2 text-xs font-bold text-primary transition-all hover:bg-primary hover:text-on-primary shadow-xs cursor-pointer active:scale-95"
          >
            Xem Shop
          </button>
          <button
            type="button"
            onClick={() => onViewAllReviews?.(shop.shopId)}
            className="rounded-xl border border-outline-variant bg-surface-container-lowest px-3 py-2 text-xs font-bold text-on-surface-variant transition-all hover:bg-surface-container-low shadow-xs cursor-pointer"
            title="Xem tất cả đánh giá của shop"
          >
            Đánh giá
          </button>
        </div>
      </div>
    </aside>
  );
}
