import { ShopVacationBanner } from "./ShopVacationBanner";

export function ProductDetailShopCard({ product, onVisitShop, onViewAllReviews }) {
  if (!product?.shop) return null;

  const { shop } = product;

  return (
    <aside className="mt-6 rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
      {product.shopVacation ? (
        <div className="mb-4">
          <ShopVacationBanner message={product.vacationMessage} />
        </div>
      ) : null}

      <div className="flex items-center gap-4">
        <div className="h-14 w-14 shrink-0 overflow-hidden rounded-full border border-outline-variant bg-surface-container-low">
          {shop.avatarUrl ? (
            <img src={shop.avatarUrl} alt={shop.shopName} className="h-full w-full object-cover" />
          ) : (
            <div className="flex h-full w-full items-center justify-center">
              <span className="material-symbols-outlined text-outline" aria-hidden="true">
                storefront
              </span>
            </div>
          )}
        </div>
        <div className="min-w-0 flex-1">
          {/* <p className="text-label-sm text-on-surface-variant">Bán bởi</p> */}
          <p className="truncate text-headline-sm font-semibold text-on-surface">{shop.shopName}</p>
          {product.ratingCount > 0 ? (
            <p className="mt-1 text-body-sm text-on-surface-variant">
              {product.ratingAvg} · {product.ratingCount} đánh giá shop
            </p>
          ) : null}
        </div>
      </div>

      <button
        type="button"
        onClick={() => onVisitShop?.(shop.shopId)}
        className="mt-4 w-full rounded-lg border-2 border-primary py-2 text-label-md font-medium text-primary transition-colors hover:bg-surface-container-low"
      >
        Xem shop
      </button>

      <button
        type="button"
        onClick={() => onViewAllReviews?.(product.productId)}
        className="mt-2 w-full rounded-lg py-2 text-label-md text-primary hover:underline"
      >
        Xem tất cả đánh giá
      </button>
    </aside>
  );
}
