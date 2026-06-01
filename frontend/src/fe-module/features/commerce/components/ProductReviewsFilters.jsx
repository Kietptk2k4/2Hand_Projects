import { RATING_TABS, SORT_OPTIONS } from "../constants/productReviewsConstants";

export function ProductReviewsFilters({
  sort,
  ratingFilter,
  onSortChange,
  onRatingFilterChange,
  onComingSoon,
  disabled = false,
}) {
  return (
    <div className="mt-4 space-y-4 rounded-xl border border-outline-variant bg-surface-container-lowest p-4 shadow-sm">
      <div>
        <label htmlFor="review-sort" className="mb-2 block text-label-sm font-medium text-on-surface">
          Sắp xếp
        </label>
        <select
          id="review-sort"
          value={sort}
          disabled={disabled}
          onChange={(event) => onSortChange?.(event.target.value)}
          className="w-full rounded-lg border border-outline-variant bg-surface px-3 py-2 text-sm text-on-surface focus:border-primary focus:outline-none disabled:opacity-50"
        >
          {SORT_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>

      <div>
        <p className="mb-2 text-label-sm font-medium text-on-surface">Lọc theo sao</p>
        <div className="flex flex-wrap gap-2">
          {RATING_TABS.map((tab) => {
            const active = ratingFilter === tab.value;
            return (
              <button
                key={tab.label}
                type="button"
                disabled={disabled}
                onClick={() => onRatingFilterChange?.(tab.value)}
                className={[
                  "rounded-full px-3 py-1.5 text-xs font-medium transition-colors",
                  active
                    ? "bg-primary text-on-primary"
                    : "border border-outline-variant bg-surface text-on-surface-variant hover:bg-surface-container-low",
                  disabled ? "cursor-not-allowed opacity-50" : "",
                ].join(" ")}
              >
                {tab.label}
              </button>
            );
          })}
        </div>
      </div>

      <div className="space-y-2 border-t border-outline-variant pt-4">
        <label className="flex cursor-not-allowed items-center gap-2 text-sm text-on-surface-variant opacity-60">
          <input type="checkbox" disabled className="rounded border-outline-variant" />
          Có hình ảnh
          <span className="text-xs">(sắp có)</span>
        </label>
        <label className="flex cursor-not-allowed items-center gap-2 text-sm text-on-surface-variant opacity-60">
          <input type="checkbox" disabled className="rounded border-outline-variant" />
          Đã mua hàng
          <span className="text-xs">(sắp có)</span>
        </label>
        <button
          type="button"
          onClick={onComingSoon}
          className="text-xs text-primary hover:underline"
        >
          Báo cáo / Hữu ích — sắp có
        </button>
      </div>
    </div>
  );
}
