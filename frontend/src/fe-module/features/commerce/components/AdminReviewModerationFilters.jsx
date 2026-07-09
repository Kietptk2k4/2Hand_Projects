import {
  AdminFilterBar,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../auth/admin/components/ui";
import {
  RATING_FILTER_OPTIONS,
  REVIEW_STATUS_FILTER_TABS,
} from "../constants/adminReviewModerationConstants";

function StatusChip({ active, disabled, onClick, children }) {
  return (
    <button
      type="button"
      disabled={disabled}
      onClick={onClick}
      className={[
        "inline-flex min-h-9 items-center rounded-full border px-3 py-1.5 text-xs font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-50",
        active
          ? "border-admin-accent bg-admin-accent-soft text-admin-accent-strong"
          : "border-admin-border text-admin-text-secondary hover:border-admin-accent/40 hover:text-admin-accent",
      ].join(" ")}
    >
      {children}
    </button>
  );
}

export function AdminReviewModerationFilters({
  activeStatusTabId,
  onStatusChange,
  ratingFilter,
  onRatingChange,
  searchInput,
  onSearchInputChange,
  onSearchSubmit,
  disabled,
}) {
  return (
    <div className="space-y-4">
      <div className="flex flex-wrap gap-2">
        {REVIEW_STATUS_FILTER_TABS.map((tab) => (
          <StatusChip
            key={tab.id}
            active={tab.id === activeStatusTabId}
            disabled={disabled}
            onClick={() => onStatusChange(tab.id)}
          >
            {tab.label}
          </StatusChip>
        ))}
      </div>

      <AdminFilterBar
        onSubmit={(event) => {
          event.preventDefault();
          onSearchSubmit?.();
        }}
      >
        <AdminFilterField label="Tìm kiếm" htmlFor="review-mod-search" className="lg:col-span-2">
          <AdminFilterInput
            id="review-mod-search"
            type="search"
            value={searchInput}
            onChange={(event) => onSearchInputChange(event.target.value)}
            disabled={disabled}
            placeholder="Tìm theo Order ID…"
          />
        </AdminFilterField>

        <AdminFilterField label="Số sao" htmlFor="review-mod-rating">
          <AdminFilterSelect
            id="review-mod-rating"
            value={ratingFilter ?? ""}
            onChange={(event) =>
              onRatingChange(event.target.value ? Number(event.target.value) : null)
            }
            disabled={disabled}
          >
            {RATING_FILTER_OPTIONS.map((opt) => (
              <option key={opt.value || "all"} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>
      </AdminFilterBar>
    </div>
  );
}
