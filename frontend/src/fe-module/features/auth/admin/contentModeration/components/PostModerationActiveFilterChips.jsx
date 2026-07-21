import { buildPostModerationActiveFilterChips } from "../utils/postModerationFilterHelpers.js";

export function PostModerationActiveFilterChips({ filters, onRemoveChip }) {
  const chips = buildPostModerationActiveFilterChips(filters);
  if (!chips.length) return null;

  return (
    <div className="flex flex-wrap items-center gap-2">
      <span className="text-xs font-medium text-admin-text-muted">Đang lọc:</span>
      {chips.map((chip) => (
        <button
          key={chip.key}
          type="button"
          onClick={() => onRemoveChip?.(chip.key)}
          className="inline-flex min-h-8 items-center gap-1 rounded-full border border-admin-accent-border bg-admin-accent-soft px-2.5 text-xs font-medium text-admin-accent-strong transition-colors hover:bg-admin-accent/10 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
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
