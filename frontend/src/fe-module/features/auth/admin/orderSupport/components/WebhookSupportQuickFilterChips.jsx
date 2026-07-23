import { WEBHOOK_QUICK_FILTER_PRESETS } from "../constants/webhookSupportListConstants.js";
import { isWebhookSupportQuickPresetActive } from "../utils/webhookSupportFilterHelpers.js";

export function WebhookSupportQuickFilterChips({ filters, onQuickFilter }) {
  return (
    <div className="flex flex-wrap gap-2">
      {WEBHOOK_QUICK_FILTER_PRESETS.map((preset) => {
        const active = isWebhookSupportQuickPresetActive(filters, preset.id);
        return (
          <button
            key={preset.id}
            type="button"
            onClick={() => onQuickFilter?.(preset.id)}
            className={[
              "min-h-9 rounded-full border px-3 py-1.5 text-sm font-medium transition-colors",
              active
                ? "border-admin-accent-border bg-admin-accent-soft text-admin-accent-strong"
                : "border-admin-border bg-admin-surface text-admin-text-secondary hover:border-admin-accent-border hover:bg-admin-surface-muted",
            ].join(" ")}
          >
            {preset.label}
          </button>
        );
      })}
    </div>
  );
}
