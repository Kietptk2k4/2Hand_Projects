import { ProductStickerBadge } from "./ProductStickerBadge";

export function ProductImageStickers({
  isOnSale = false,
  isOutOfStock = false,
  lowStock = false,
  conditionLabel = null,
}) {
  const hasLeftStickers = Boolean(conditionLabel) || isOnSale || (!isOutOfStock && lowStock);

  return (
    <>
      {isOutOfStock ? (
        <div
          className="pointer-events-none absolute inset-0 z-[5] bg-neutral-900/20"
          aria-hidden="true"
        />
      ) : null}

      {hasLeftStickers ? (
        <div className="absolute left-0 top-0 z-10 flex flex-col items-start gap-1.5 p-2">
          {conditionLabel ? (
            <ProductStickerBadge variant="condition" rotate="-rotate-3">
              {conditionLabel}
            </ProductStickerBadge>
          ) : null}
          {isOnSale ? (
            <ProductStickerBadge variant="sale" rotate={conditionLabel ? "rotate-2" : "-rotate-6"}>
              Giảm giá
            </ProductStickerBadge>
          ) : null}
          {!isOutOfStock && lowStock ? (
            <ProductStickerBadge
              variant="lowStock"
              rotate={isOnSale || conditionLabel ? "-rotate-3" : "-rotate-6"}
            >
              <span className="material-symbols-outlined text-[11px]" aria-hidden="true">
                warning
              </span>
              Sắp hết
            </ProductStickerBadge>
          ) : null}
        </div>
      ) : null}

      {isOutOfStock ? (
        <div className="absolute right-0 top-0 z-10 p-2">
          <ProductStickerBadge variant="soldOut" rotate="rotate-6">
            Hết hàng
          </ProductStickerBadge>
        </div>
      ) : null}
    </>
  );
}
