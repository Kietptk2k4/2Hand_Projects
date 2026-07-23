import { buildWebhookSupportFilterChips } from "../utils/webhookSupportFilterHelpers.js";

export function WebhookSupportActiveFilterChips({ filters, onRemoveChip }) {
  const chips = buildWebhookSupportFilterChips(filters || {});
  if (!chips.length) return null;

  return (
    <div className="flex flex-wrap gap-2">
      {chips.map((chip) => (
        <button
          key={chip.key}
          type="button"
          onClick={() => onRemoveChip?.(chip.key)}
          className="inline-flex min-h-8 items-center gap-1 rounded-full border border-admin-border bg-admin-surface-muted px-3 py-1 text-xs text-admin-text-secondary hover:border-admin-accent-border"
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
