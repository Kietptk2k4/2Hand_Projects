import { useRef, useState } from "react";
import { formatVndPrice } from "../../social/utils/formatPrice";
import { ProductImageStickers } from "./ProductImageStickers";

// Curated high quality fallback images pool for 2hand products when thumbnail is broken/missing
const FALLBACK_IMAGES = [
  "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500&auto=format&fit=crop&q=80",
];

function getRandomFallback(productId) {
  if (!productId) return FALLBACK_IMAGES[0];
  let hash = 0;
  const str = String(productId);
  for (let i = 0; i < str.length; i += 1) {
    hash = (hash << 5) - hash + str.charCodeAt(i);
    hash |= 0;
  }
  const index = Math.abs(hash) % FALLBACK_IMAGES.length;
  return FALLBACK_IMAGES[index];
}

export function ProductCard({
  product,
  onOpenProduct,
  onOpenShop,
  onAddToCart,
  onBuyNow,
  isAddingToCart = false,
  isBuyingNow = false,
  disabledActions = false,
}) {
  const [imgError, setImgError] = useState(false);
  const imageRef = useRef(null);

  const priceVal = product.price != null ? Number(product.price) : null;
  const salePriceVal =
    product.salePrice != null
      ? Number(product.salePrice)
      : product.effectivePrice != null && priceVal != null && Number(product.effectivePrice) < priceVal
      ? Number(product.effectivePrice)
      : null;

  const isOnSale =
    salePriceVal != null && priceVal != null && salePriceVal < priceVal;
  const isOutOfStock = !product.inStock || product.status === "OUT_OF_STOCK";
  const actionsDisabled = disabledActions || isOutOfStock || isAddingToCart || isBuyingNow;

  const canOpenProduct = Boolean(product?.productId && onOpenProduct);

  const discountPercent =
    isOnSale && priceVal
      ? Math.round(((priceVal - salePriceVal) / priceVal) * 100)
      : 0;

  const handleAddToCart = (event) => {
    event.stopPropagation();
    if (actionsDisabled) return;
    const fromRect = imageRef.current?.getBoundingClientRect();
    onAddToCart?.(product.productId, 1, {
      imageUrl: product.thumbnailUrl,
      fromRect,
      sourceElement: event.currentTarget,
    });
  };

  const handleOpen = () => {
    if (!canOpenProduct) return;
    onOpenProduct(product.productId);
  };

  const handleCardClick = () => {
    if (!canOpenProduct) return;
    handleOpen();
  };

  const handleCardKeyDown = (event) => {
    if (!canOpenProduct) return;
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      handleOpen();
    }
  };

  const displayImageSrc =
    !imgError && product.thumbnailUrl
      ? product.thumbnailUrl
      : getRandomFallback(product.productId || product.title);

  return (
    <article
      onClick={canOpenProduct ? handleCardClick : undefined}
      onKeyDown={canOpenProduct ? handleCardKeyDown : undefined}
      tabIndex={canOpenProduct ? 0 : undefined}
      role={canOpenProduct ? "link" : undefined}
      aria-label={canOpenProduct ? `Xem chi tiết ${product.title}` : undefined}
      className={[
        "group flex h-full flex-col overflow-hidden rounded-2xl border border-outline-variant/70 bg-surface-container-lowest transition-all duration-300 shadow-xs",
        canOpenProduct
          ? "cursor-pointer hover:-translate-y-1 hover:border-primary/40 hover:shadow-lg focus:outline-none focus-visible:ring-2 focus-visible:ring-primary"
          : "",
      ].join(" ")}
    >
      {/* Product Image Area */}
      <div ref={imageRef} className="relative aspect-square w-full overflow-hidden bg-surface-container-low">
        <img
          src={displayImageSrc}
          alt={product.title || "Sản phẩm"}
          onError={() => setImgError(true)}
          className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
          loading="lazy"
        />

        {/* Discount Badge */}
        {discountPercent > 0 ? (
          <div className="absolute right-2 top-2 z-10 rounded-lg bg-red-600 px-2 py-0.5 text-xs font-black text-white shadow-md">
            -{discountPercent}%
          </div>
        ) : null}

        {/* Top-left Quality / Sale Badge */}
        <div className="absolute left-2 top-2 z-10 flex flex-col gap-1">
          {isOnSale ? (
            <span className="rounded-md bg-red-600 px-2 py-0.5 text-[10px] font-black text-white shadow-md uppercase tracking-wider border border-white/30">
              GIẢM GIÁ
            </span>
          ) : (
            <span className="rounded-md bg-slate-900/80 px-2 py-0.5 text-[10px] font-bold text-amber-400 backdrop-blur-sm shadow-xs">
              2HAND SELECT
            </span>
          )}
        </div>

        <ProductImageStickers
          isOnSale={isOnSale}
          isOutOfStock={isOutOfStock}
          lowStock={product.lowStock}
        />
      </div>

      {/* Product Details Area */}
      <div className="flex flex-1 flex-col p-3.5 sm:p-4">
        {/* Title */}
        <h3 className="mb-1 text-sm font-bold text-on-surface line-clamp-2 transition-colors group-hover:text-primary sm:text-base">
          {product.title}
        </h3>

        {/* Rating and Sold Stats */}
        <div className="mb-2 flex items-center gap-1.5 text-xs">
          <div className="flex items-center text-amber-500">
            <span className="material-symbols-outlined fill text-sm" aria-hidden="true">
              star
            </span>
            <span className="ml-0.5 font-bold text-on-surface">{product.ratingAvg || "4.8"}</span>
          </div>
          <span className="text-outline">•</span>
          <span className="text-on-surface-variant">
            {product.ratingCount ? `${product.ratingCount} đánh giá` : "Đã bán 80+"}
          </span>
        </div>

        {/* Shop Badge */}
        <div className="mb-3 flex items-center gap-1.5">
          <span className="material-symbols-outlined text-sm text-outline" aria-hidden="true">
            storefront
          </span>
          {onOpenShop && product.shopId ? (
            <button
              type="button"
              onClick={(event) => {
                event.stopPropagation();
                onOpenShop(product.shopId);
              }}
              className="relative z-10 text-left text-xs font-semibold text-on-surface-variant transition-colors hover:text-primary line-clamp-1"
            >
              {product.shopName || "Shop Đã Xác Minh"}
            </button>
          ) : (
            <span className="text-xs text-on-surface-variant line-clamp-1">
              {product.shopName || "Shop Đã Xác Minh"}
            </span>
          )}
        </div>

        {product.shopVacation ? (
          <p className="mb-2 text-xs text-amber-600 font-medium">
            {product.vacationMessage || "Shop đang tạm nghỉ"}
          </p>
        ) : null}

        {/* Price & Action Footer */}
        <div className="mt-auto border-t border-surface-container-high pt-3">
          <div className="mb-3 flex items-baseline justify-between gap-1">
            <div className="flex flex-col">
              {isOnSale && priceVal ? (
                <span className="text-[11px] text-outline line-through">
                  {formatVndPrice(priceVal)}
                </span>
              ) : null}
              <span
                className={`text-base font-black sm:text-lg ${
                  isOnSale ? "text-red-600" : "text-primary"
                }`}
              >
                {formatVndPrice(isOnSale ? salePriceVal : (product.effectivePrice || priceVal))}
              </span>
            </div>
          </div>

          <div className="flex gap-2">
            <button
              type="button"
              disabled={actionsDisabled}
              onClick={(event) => {
                event.stopPropagation();
                if (!actionsDisabled) {
                  onBuyNow?.(product.productId);
                }
              }}
              className="relative z-10 flex flex-1 items-center justify-center rounded-xl bg-primary px-3 py-2 text-xs font-bold text-on-primary shadow-xs transition-all hover:bg-[#0050cb] active:scale-95 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {isBuyingNow ? "..." : "Mua ngay"}
            </button>

            <button
              type="button"
              disabled={actionsDisabled}
              onClick={handleAddToCart}
              className="relative z-10 flex items-center justify-center rounded-xl border border-outline-variant bg-surface-container-lowest p-2 text-on-surface transition-all hover:border-primary hover:bg-primary/10 hover:text-primary active:scale-95 disabled:cursor-not-allowed disabled:opacity-50"
              aria-label={isAddingToCart ? "Đang thêm vào giỏ" : "Thêm vào giỏ"}
            >
              <span className="material-symbols-outlined text-lg" aria-hidden="true">
                add_shopping_cart
              </span>
            </button>
          </div>
        </div>
      </div>
    </article>
  );
}
