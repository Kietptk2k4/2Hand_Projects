export function CartQuantityStepper({
  quantity,
  disabled = false,
  isLoading = false,
  maxQuantity,
  onDecrease,
  onIncrease,
}) {
  const atMin = quantity <= 1;
  const atMax = maxQuantity != null && quantity >= maxQuantity;

  return (
    <div
      className={`flex items-center overflow-hidden rounded-full border border-outline-variant bg-surface ${
        disabled ? "pointer-events-none opacity-50" : ""
      }`}
    >
      <button
        type="button"
        aria-label="Giảm số lượng"
        disabled={disabled || isLoading || atMin}
        onClick={onDecrease}
        className="px-3 py-1 text-on-surface-variant transition-colors hover:bg-surface-container disabled:cursor-not-allowed disabled:text-outline"
      >
        <span className="material-symbols-outlined text-sm" aria-hidden="true">
          remove
        </span>
      </button>
      <span className="w-8 text-center text-sm font-medium text-on-surface">{quantity}</span>
      <button
        type="button"
        aria-label="Tăng số lượng"
        disabled={disabled || isLoading || atMax}
        onClick={onIncrease}
        className="px-3 py-1 text-on-surface-variant transition-colors hover:bg-surface-container disabled:cursor-not-allowed disabled:text-outline"
      >
        <span className="material-symbols-outlined text-sm" aria-hidden="true">
          add
        </span>
      </button>
    </div>
  );
}
