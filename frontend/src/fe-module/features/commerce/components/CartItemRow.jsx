import { formatVndPrice } from "../../social/utils/formatPrice";
import { getUnavailableLabel, getLineTotal, isCartItemInvalid } from "../utils/cartDisplay";
import { CartQuantityStepper } from "./CartQuantityStepper";

export function CartItemRow({
  item,
  isMutating = false,
  onOpenProduct,
  onRemove,
  onDecrease,
  onIncrease,
}) {
  const invalid = isCartItemInvalid(item);
  const isOutOfStock =
    !item.inStock || item.unavailableReason === "OUT_OF_STOCK";
  const isVacation = item.unavailableReason === "SHOP_ON_VACATION";
  const unavailableLabel = item.validateMessage || getUnavailableLabel(item);
  const lineTotal = getLineTotal(item);

  const borderClass = isOutOfStock
    ? "border-error-container"
    : "border-outline-variant";

  return (
    <article
      className={`relative flex flex-col gap-4 overflow-hidden rounded-lg border bg-surface-container-lowest p-4 shadow-sm transition-shadow hover:shadow-md sm:flex-row sm:gap-6 ${
        borderClass
      } ${isOutOfStock ? "opacity-90" : ""}`}
    >
      {isOutOfStock ? (
        <div className="absolute left-0 top-0 h-1 w-full bg-error" aria-hidden="true" />
      ) : null}
      {isVacation ? (
        <div className="absolute left-0 top-0 h-1 w-full bg-surface-tint" aria-hidden="true" />
      ) : null}

      <button
        type="button"
        onClick={() => onOpenProduct?.(item.productId)}
        className="h-32 w-full shrink-0 overflow-hidden rounded-lg bg-surface-container sm:w-32"
      >
        <img
          src={item.imageUrl}
          alt={item.productName}
          className={`h-full w-full object-cover ${isOutOfStock ? "grayscale" : ""}`}
        />
      </button>

      <div className="flex flex-1 flex-col justify-between">
        <div>
          <div className="flex items-start justify-between gap-2">
            <div className="min-w-0 flex-1">
              {invalid ? (
                <div className="mb-1 flex items-center gap-1">
                  <span
                    className={`material-symbols-outlined text-sm ${
                      isOutOfStock ? "text-error" : "text-surface-tint"
                    }`}
                    style={{ fontVariationSettings: isOutOfStock ? "'FILL' 1" : undefined }}
                    aria-hidden="true"
                  >
                    {isOutOfStock ? "warning" : "flight_takeoff"}
                  </span>
                  <span
                    className={`text-xs font-semibold uppercase ${
                      isOutOfStock ? "text-error" : "text-surface-tint"
                    }`}
                  >
                    {unavailableLabel}
                  </span>
                </div>
              ) : null}
              <button
                type="button"
                onClick={() => onOpenProduct?.(item.productId)}
                className={`text-left text-headline-sm font-semibold text-on-surface hover:text-primary ${
                  isOutOfStock ? "line-through" : ""
                }`}
              >
                {item.productName}
              </button>
            </div>
            <button
              type="button"
              aria-label="Xóa sản phẩm"
              disabled={isMutating}
              onClick={() => onRemove?.(item.cartItemId)}
              className="rounded-full p-1 text-on-surface-variant transition-colors hover:bg-error-container hover:text-error disabled:opacity-50"
            >
              <span className="material-symbols-outlined" aria-hidden="true">
                delete
              </span>
            </button>
          </div>

          {isVacation ? (
            <p className="mt-3 inline-block rounded-lg border border-outline-variant bg-surface-container-low p-3 text-sm text-on-surface-variant">
              Shop đang nghỉ — sản phẩm tạm thời không thể mua.
            </p>
          ) : null}
        </div>

        <div className="mt-4 flex items-end justify-between gap-4">
          <CartQuantityStepper
            quantity={item.quantity}
            disabled={invalid}
            isLoading={isMutating}
            maxQuantity={item.availableQuantity}
            onDecrease={onDecrease}
            onIncrease={onIncrease}
          />
          <p
            className={`text-right text-headline-md font-semibold ${
              invalid ? "text-on-surface-variant line-through" : "text-on-surface"
            }`}
          >
            {formatVndPrice(lineTotal)}
          </p>
        </div>
      </div>
    </article>
  );
}
