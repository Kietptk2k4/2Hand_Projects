import { buildModelRegistryActiveFilterChips } from "../utils/modelRegistryFilterHelpers.js";

export function ModelRegistryActiveFilterChips({ filters, onRemoveChip }) {
  const chips = buildModelRegistryActiveFilterChips(filters);
  if (!chips.length) return null;

  return (
    <div className="flex flex-wrap gap-2">
      {chips.map((chip) => (
        <button
          key={chip.key}
          type="button"
          onClick={() => onRemoveChip?.(chip.key)}
          className="inline-flex min-h-8 items-center gap-1 rounded-full border border-admin-accent-border bg-admin-accent-soft px-3 text-xs font-medium text-admin-accent"
        >
          {chip.label}
          <span className="material-symbols-outlined text-[14px]" aria-hidden="true">
            close
          </span>
        </button>
      ))}
    </div>
  );
}
