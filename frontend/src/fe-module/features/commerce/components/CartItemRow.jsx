import { useState } from "react";
import { formatVndPrice } from "../../social/utils/formatPrice";
import { getUnavailableLabel, getLineTotal, isCartItemInvalid } from "../utils/cartDisplay";
import { CartQuantityStepper } from "./CartQuantityStepper";

// Curated high quality fallback images for 2hand cart items when image fails/missing
const FALLBACK_CART_IMAGES = [
  "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&auto=format&fit=crop&q=80",
];

function getRandomCartFallback(itemId) {
  if (!itemId) return FALLBACK_CART_IMAGES[0];
  let hash = 0;
  const str = String(itemId);
  for (let i = 0; i < str.length; i += 1) {
    hash = (hash << 5) - hash + str.charCodeAt(i);
    hash |= 0;
  }
  return FALLBACK_CART_IMAGES[Math.abs(hash) % FALLBACK_CART_IMAGES.length];
}

export function CartItemRow({
  item,
  isMutating = false,
  selected = false,
  canSelect = true,
  onToggleSelect,
  onOpenProduct,
  onRemove,
  onDecrease,
  onIncrease,
  isLastInGroup = false,
}) {
  const [imgError, setImgError] = useState(false);

  const invalid = isCartItemInvalid(item);
  const isOutOfStock = !item.inStock || item.unavailableReason === "OUT_OF_STOCK";
  const isVacation = item.unavailableReason === "SHOP_ON_VACATION";
  const unavailableLabel = item.validateMessage || getUnavailableLabel(item);
  const lineTotal = getLineTotal(item);
  const unitPrice = item.price || item.unitPrice || (lineTotal / (item.quantity || 1));

  const displayImgSrc =
    !imgError && item.imageUrl
      ? item.imageUrl
      : getRandomCartFallback(item.cartItemId || item.productId);

  const borderBottomClass = isLastInGroup ? "rounded-b-2xl border-b" : "border-b border-b-outline-variant/40";

  return (
    <article
      className={`relative flex flex-col gap-4 border-x border-outline-variant/80 bg-surface-container-lowest p-4 transition-all hover:bg-surface-container-lowest/80 sm:flex-row sm:items-center ${borderBottomClass} ${
        invalid ? "bg-slate-50/60 opacity-80" : ""
      }`}
    >
      {/* Checkbox & Product Info (Left section) */}
      <div className="flex flex-1 items-center gap-3 min-w-0 sm:w-5/12">
        <input
          type="checkbox"
          checked={Boolean(selected)}
          disabled={!canSelect || isMutating}
          onChange={() => onToggleSelect?.(item.cartItemId)}
          className="h-4 w-4 shrink-0 rounded border-outline-variant text-primary focus:ring-primary/20 disabled:cursor-not-allowed disabled:opacity-40 cursor-pointer"
          aria-label={canSelect ? `Chọn ${item.productName}` : "Sản phẩm không thể chọn để thanh toán"}
        />

        {/* Thumbnail Image */}
        <button
          type="button"
          onClick={() => onOpenProduct?.(item.productId)}
          className="relative h-20 w-20 shrink-0 overflow-hidden rounded-xl border border-outline-variant/60 bg-surface-container-low cursor-pointer"
        >
          <img
            src={displayImgSrc}
            alt={item.productName || "Sản phẩm"}
            onError={() => setImgError(true)}
            className={`h-full w-full object-cover transition-transform duration-300 hover:scale-105 ${
              invalid ? "grayscale opacity-75" : ""
            }`}
            loading="lazy"
          />
        </button>

        {/* Product Titles & Badges */}
        <div className="min-w-0 flex-1">
          {invalid ? (
            <div className="mb-1 flex items-center gap-1">
              <span
                className={`material-symbols-outlined text-xs ${
                  isOutOfStock ? "text-error" : "text-amber-600"
                }`}
                aria-hidden="true"
              >
                {isOutOfStock ? "error" : "warning"}
              </span>
              <span
                className={`text-[10px] font-bold uppercase tracking-wider ${
                  isOutOfStock ? "text-error" : "text-amber-700"
                }`}
              >
                {unavailableLabel}
              </span>
            </div>
          ) : null}

          <button
            type="button"
            onClick={() => onOpenProduct?.(item.productId)}
            className={`text-left text-xs font-bold text-on-surface hover:text-primary line-clamp-2 sm:text-sm cursor-pointer ${
              invalid ? "line-through text-slate-500" : ""
            }`}
          >
            {item.productName}
          </button>

          {isVacation ? (
            <p className="mt-1 text-[11px] font-medium text-amber-600">
              Shop tạm nghỉ — Không thể thanh toán
            </p>
          ) : null}
        </div>
      </div>

      {/* Desktop Column Layout (Right section) */}
      <div className="flex flex-wrap items-center justify-between gap-3 sm:w-7/12 sm:grid sm:grid-cols-4 sm:items-center sm:text-center">
        {/* Unit Price */}
        <div className="text-left sm:text-center">
          <span className="text-xs font-bold text-on-surface sm:text-sm">
            {formatVndPrice(unitPrice)}
          </span>
        </div>

        {/* Quantity Stepper */}
        <div className="flex justify-center">
          <CartQuantityStepper
            quantity={item.quantity}
            disabled={invalid}
            isLoading={isMutating}
            maxQuantity={item.availableQuantity}
            onDecrease={onDecrease}
            onIncrease={onIncrease}
          />
        </div>

        {/* Line Total */}
        <div className="text-right sm:text-center">
          <span
            className={`text-xs font-black sm:text-sm ${
              invalid ? "text-slate-400 line-through" : "text-red-600"
            }`}
          >
            {formatVndPrice(lineTotal)}
          </span>
        </div>

        {/* Remove Button */}
        <div className="flex justify-end">
          <button
            type="button"
            aria-label="Xóa sản phẩm"
            disabled={isMutating}
            onClick={() => onRemove?.(item.cartItemId)}
            className="flex h-8 w-8 items-center justify-center rounded-xl text-slate-400 transition-colors hover:bg-error-container/20 hover:text-error disabled:opacity-50 cursor-pointer"
          >
            <span className="material-symbols-outlined text-lg" aria-hidden="true">
              delete
            </span>
          </button>
        </div>
      </div>
    </article>
  );
}
