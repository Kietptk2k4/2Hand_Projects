import {
  FINANCE_GRANULARITY_OPTIONS,
  FINANCE_RANGE_PRESETS,
  FINANCE_TOP_SELLERS_LIMIT_OPTIONS,
} from "../constants/financeOverviewConstants.js";

export function FinanceDateRangeToolbar({
  activeRangeId,
  granularity,
  onRangeChange,
  onGranularityChange,
  onRefresh,
  isLoading,
  showGranularity = true,
  limit,
  onLimitChange,
  showLimit = false,
}) {
  return (
    <div className="flex w-full flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
      <div
        className="flex flex-wrap gap-2"
        role="group"
        aria-label="Chọn khoảng thời gian"
      >
        {FINANCE_RANGE_PRESETS.map((preset) => (
          <button
            key={preset.id}
            type="button"
            onClick={() => onRangeChange?.(preset.id)}
            aria-pressed={activeRangeId === preset.id}
            className={[
              "min-h-10 rounded-lg px-3 py-2 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft",
              activeRangeId === preset.id
                ? "bg-admin-accent text-white"
                : "border border-admin-border text-admin-text-secondary hover:bg-admin-surface-muted hover:text-admin-text",
            ].join(" ")}
          >
            {preset.label}
          </button>
        ))}
      </div>

      <div className="flex flex-wrap items-center gap-2">
        {showLimit ? (
          <div
            className="flex flex-wrap gap-2"
            role="group"
            aria-label="Số lượng top sellers"
          >
            {FINANCE_TOP_SELLERS_LIMIT_OPTIONS.map((option) => (
              <button
                key={option.value}
                type="button"
                onClick={() => onLimitChange?.(option.value)}
                aria-pressed={Number(limit) === option.value}
                className={[
                  "min-h-10 rounded-lg px-3 py-2 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft",
                  Number(limit) === option.value
                    ? "border border-admin-accent-border bg-admin-accent-soft text-admin-accent-strong"
                    : "border border-admin-border text-admin-text-secondary hover:bg-admin-surface-muted",
                ].join(" ")}
              >
                {option.label}
              </button>
            ))}
          </div>
        ) : null}

        {showGranularity ? (
          <div
            className="flex flex-wrap gap-2"
            role="group"
            aria-label="Chọn độ chi tiết biểu đồ"
          >
            {FINANCE_GRANULARITY_OPTIONS.map((option) => (
              <button
                key={option.value}
                type="button"
                onClick={() => onGranularityChange?.(option.value)}
                aria-pressed={granularity === option.value}
                className={[
                  "min-h-10 rounded-lg px-3 py-2 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft",
                  granularity === option.value
                    ? "border border-admin-accent-border bg-admin-accent-soft text-admin-accent-strong"
                    : "border border-admin-border text-admin-text-secondary hover:bg-admin-surface-muted",
                ].join(" ")}
              >
                {option.label}
              </button>
            ))}
          </div>
        ) : null}

        <button
          type="button"
          onClick={onRefresh}
          disabled={isLoading}
          className="inline-flex min-h-10 items-center gap-2 rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-sm font-medium text-admin-text-secondary transition-colors hover:bg-admin-surface-muted hover:text-admin-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft disabled:cursor-not-allowed disabled:opacity-60"
        >
          <span className="material-symbols-outlined text-base" aria-hidden="true">
            refresh
          </span>
          Làm mới
        </button>
      </div>
    </div>
  );
}
