import { buildProductModerationActiveFilterChips } from "../utils/productModerationFilterHelpers.js";

export function ProductModerationActiveFilterChips({ filters, onRemoveChip }) {
  const chips = buildProductModerationActiveFilterChips(filters);
  if (!chips.length) return null;

  return (
    <div className="flex flex-wrap items-center gap-2 text-sm">
      <span className="text-admin-text-muted">Đang lọc:</span>
      {chips.map((chip) => (
        <button
          key={chip.key}
          type="button"
          onClick={() => onRemoveChip?.(chip.key)}
          className="inline-flex min-h-8 items-center gap-1 rounded-full border border-admin-border bg-admin-surface-muted px-2.5 text-xs text-admin-text-secondary hover:border-admin-danger/40 hover:text-admin-danger"
        >
          <span>{chip.label}</span>
          <span className="material-symbols-outlined text-[14px]" aria-hidden="true">
            close
          </span>
        </button>
      ))}
    </div>
  );
}
