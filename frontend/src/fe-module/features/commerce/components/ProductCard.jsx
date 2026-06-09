import { useRef } from "react";
import { formatVndPrice } from "../../social/utils/formatPrice";
import { ProductImageStickers } from "./ProductImageStickers";

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
  const isOnSale =
    product.salePrice != null &&
    product.price != null &&
    Number(product.salePrice) < Number(product.price);
  const isOutOfStock = !product.inStock || product.status === "OUT_OF_STOCK";
  const actionsDisabled = disabledActions || isOutOfStock || isAddingToCart || isBuyingNow;

  const imageRef = useRef(null);
  const canOpenProduct = Boolean(product?.productId && onOpenProduct);

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

  return (
    <article
      onClick={canOpenProduct ? handleCardClick : undefined}
      onKeyDown={canOpenProduct ? handleCardKeyDown : undefined}
      tabIndex={canOpenProduct ? 0 : undefined}
      role={canOpenProduct ? "link" : undefined}
      aria-label={canOpenProduct ? `Xem chi tiết ${product.title}` : undefined}
      className={[
        "group flex h-full flex-col overflow-hidden rounded-lg border border-outline-variant bg-surface-container-lowest transition-shadow",
        canOpenProduct
          ? "cursor-pointer hover:shadow-md focus:outline-none focus-visible:ring-2 focus-visible:ring-primary"
          : "",
      ].join(" ")}
    >
      <div ref={imageRef} className="relative h-48 w-full overflow-hidden bg-surface-container-low">
        {product.thumbnailUrl ? (
          <img
            src={product.thumbnailUrl}
            alt=""
            className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
            loading="lazy"
          />
        ) : (
          <div className="flex h-full min-h-48 items-center justify-center text-outline">
            <span className="material-symbols-outlined text-4xl" aria-hidden="true">
              image
            </span>
          </div>
        )}

        <ProductImageStickers
          isOnSale={isOnSale}
          isOutOfStock={isOutOfStock}
          lowStock={product.lowStock}
        />
      </div>

      <div className="flex flex-1 flex-col p-4">
        <h3 className="mb-2 text-headline-sm font-semibold text-on-surface line-clamp-2 group-hover:text-primary">
          {product.title}
        </h3>

        {product.ratingCount > 0 ? (
          <div className="mb-3 flex items-center gap-1">
            <span
              className="material-symbols-outlined fill text-[16px] text-primary"
              aria-hidden="true"
            >
              star
            </span>
            <span className="text-label-sm text-on-surface">{product.ratingAvg}</span>
            <span className="text-label-sm text-on-surface-variant">
              · {product.ratingCount} đánh giá
            </span>
          </div>
        ) : null}

        <div className="mb-3 flex items-center gap-2">
          <span className="material-symbols-outlined text-[16px] text-outline" aria-hidden="true">
            storefront
          </span>
          {onOpenShop && product.shopId ? (
            <button
              type="button"
              onClick={(event) => {
                event.stopPropagation();
                onOpenShop(product.shopId);
              }}
              className="relative z-10 text-left text-label-sm text-on-surface-variant transition-colors hover:text-primary"
            >
              {product.shopName}
            </button>
          ) : (
            <span className="text-label-sm text-on-surface-variant">{product.shopName}</span>
          )}
        </div>

        {product.shopVacation ? (
          <p className="mb-3 text-xs text-on-surface-variant">
            {product.vacationMessage || "Shop đang nghỉ"}
          </p>
        ) : null}

        <div className="mt-auto flex flex-col gap-2 border-t border-surface-container-high pt-4">
          <div className="flex flex-col">
            {isOnSale ? (
              <span className="text-label-sm text-outline-variant line-through">
                {formatVndPrice(product.price)}
              </span>
            ) : null}
            <span
              className={`text-headline-md font-bold ${isOnSale ? "text-error" : "text-on-surface"}`}
            >
              {formatVndPrice(product.effectivePrice)}
            </span>
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
              className="relative z-10 flex flex-1 items-center justify-center rounded-md bg-primary px-2 py-1.5 text-label-md font-semibold text-on-primary transition-colors hover:bg-[#0050cb] disabled:cursor-not-allowed disabled:opacity-50"
            >
              {isBuyingNow ? "..." : "Mua ngay"}
            </button>
            <button
              type="button"
              disabled={actionsDisabled}
              onClick={handleAddToCart}
              className="relative z-10 flex items-center justify-center rounded-md border border-outline-variant bg-surface-container-lowest px-2.5 py-1.5 text-on-surface transition-colors hover:bg-surface-container-low disabled:cursor-not-allowed disabled:opacity-50"
              aria-label={isAddingToCart ? "Đang thêm vào giỏ" : "Thêm vào giỏ"}
            >
              <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
                add_shopping_cart
              </span>
            </button>
          </div>
        </div>
      </div>
    </article>
  );
}
