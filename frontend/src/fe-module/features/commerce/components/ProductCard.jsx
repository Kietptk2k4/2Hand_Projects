import { formatVndPrice } from "../../social/utils/formatPrice";

function ProductBadge({ children, className = "" }) {
  return (
    <span
      className={`pointer-events-none absolute left-2 top-2 rounded px-2 py-0.5 text-xs font-semibold ${className}`}
    >
      {children}
    </span>
  );
}

export function ProductCard({
  product,
  onOpenProduct,
  onOpenShop,
  onComingSoon,
  disabledActions = false,
}) {
  const isOnSale =
    product.salePrice != null &&
    product.price != null &&
    Number(product.salePrice) < Number(product.price);
  const isOutOfStock = !product.inStock || product.status === "OUT_OF_STOCK";
  const addDisabled = disabledActions || isOutOfStock;

  const canOpenProduct = Boolean(product?.productId && onOpenProduct);

  const handleOpen = () => {
    if (!canOpenProduct) return;
    onOpenProduct(product.productId);
  };

  const handleCardClick = (event) => {
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
        canOpenProduct ? "cursor-pointer hover:shadow-md focus:outline-none focus-visible:ring-2 focus-visible:ring-primary" : "",
      ].join(" ")}
    >
      <div className="relative h-48 w-full overflow-hidden bg-surface-container-low">
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

        {isOnSale ? (
          <ProductBadge className="z-10 bg-error text-on-error">Giảm giá</ProductBadge>
        ) : null}
        {isOutOfStock ? (
          <ProductBadge className="left-auto right-2 z-10 bg-on-surface-variant text-surface">
            Hết hàng
          </ProductBadge>
        ) : null}
        {!isOutOfStock && product.lowStock ? (
          <ProductBadge className="top-10 z-10 bg-secondary-container text-on-secondary-container">
            Sắp hết hàng
          </ProductBadge>
        ) : null}

        <button
          type="button"
          onClick={(event) => {
            event.stopPropagation();
            onComingSoon?.();
          }}
          className="absolute right-2 top-2 z-10 rounded-full bg-surface-container-lowest/80 p-2 text-on-surface-variant backdrop-blur-sm transition-all hover:bg-surface-container-lowest hover:text-primary"
          aria-label="Yêu thích"
        >
          <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
            favorite
          </span>
        </button>
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

        <div className="mt-auto flex items-center justify-between border-t border-surface-container-high pt-4">
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
          <button
            type="button"
            disabled={addDisabled}
            onClick={(event) => {
              event.stopPropagation();
              onComingSoon?.();
            }}
            className="relative z-10 flex items-center gap-1 rounded-md bg-primary-container px-3 py-1.5 text-label-md text-on-primary-container transition-colors hover:bg-primary hover:text-on-primary disabled:cursor-not-allowed disabled:opacity-50"
          >
            <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
              add_shopping_cart
            </span>
            Thêm
          </button>
        </div>
      </div>
    </article>
  );
}
