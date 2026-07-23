import { CATEGORY_STATUS_OPTIONS } from "../utils/categoryHelpers.js";

export function CategoryStatusChips({ statusFilter, onStatusChange, disabled }) {
  return (
    <div
      className="flex flex-wrap gap-2"
      role="group"
      aria-label="Lọc theo trạng thái danh mục"
    >
      {CATEGORY_STATUS_OPTIONS.map((option) => {
        const isActive = statusFilter === option.value;
        return (
          <button
            key={option.value || "all"}
            type="button"
            disabled={disabled}
            aria-pressed={isActive}
            onClick={() => onStatusChange?.(option.value)}
            className={[
              "min-h-10 rounded-lg px-3 py-2 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft disabled:cursor-not-allowed disabled:opacity-60",
              isActive
                ? "bg-admin-accent text-white"
                : "border border-admin-border text-admin-text-secondary hover:bg-admin-surface-muted hover:text-admin-text",
            ].join(" ")}
          >
            {option.label}
          </button>
        );
      })}
    </div>
  );
}
