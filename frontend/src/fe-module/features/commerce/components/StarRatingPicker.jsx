import { useCallback, useState } from "react";

export function StarRatingPicker({ value = 0, onChange, disabled = false }) {
  const [hoverValue, setHoverValue] = useState(0);
  const display = hoverValue || value;

  const select = useCallback(
    (star) => {
      if (disabled) return;
      onChange?.(star);
    },
    [disabled, onChange],
  );

  return (
    <div>
      <div
        className="flex gap-2 text-amber-500"
        role="radiogroup"
        aria-label="Chọn số sao đánh giá"
        onMouseLeave={() => setHoverValue(0)}
      >
        {[1, 2, 3, 4, 5].map((star) => {
          const filled = star <= display;
          return (
            <button
              key={star}
              type="button"
              disabled={disabled}
              className="rounded p-0.5 transition-transform hover:scale-110 disabled:cursor-not-allowed disabled:opacity-50"
              onMouseEnter={() => !disabled && setHoverValue(star)}
              onClick={() => select(star)}
              aria-label={`${star} sao`}
              aria-checked={value === star}
              role="radio"
            >
              <span
                className={`material-symbols-outlined text-[32px] ${
                  filled ? "text-amber-500" : "text-outline-variant"
                }`}
                style={filled ? { fontVariationSettings: "'FILL' 1" } : undefined}
                aria-hidden="true"
              >
                star
              </span>
            </button>
          );
        })}
      </div>
      <p className="mt-1 text-body-sm text-on-surface-variant">
        {value ? `Bạn chọn ${value} sao` : "Chạm để chọn số sao"}
      </p>
    </div>
  );
}
