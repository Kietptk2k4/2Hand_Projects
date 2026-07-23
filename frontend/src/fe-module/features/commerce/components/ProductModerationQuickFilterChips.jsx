import { PRODUCT_MODERATION_QUICK_FILTER_PRESETS } from "../constants/productModerationListConstants.js";
import { isProductModerationQuickPresetActive } from "../utils/productModerationFilterHelpers.js";

export function ProductModerationQuickFilterChips({ filters, onQuickFilter }) {
  return (
    <div className="flex flex-wrap gap-2">
      {PRODUCT_MODERATION_QUICK_FILTER_PRESETS.map((preset) => {
        const active = isProductModerationQuickPresetActive(filters, preset.id);
        return (
          <button
            key={preset.id}
            type="button"
            onClick={() => onQuickFilter?.(preset.id)}
            className={`inline-flex min-h-9 items-center rounded-full border px-3 text-xs font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft ${
              active
                ? "border-admin-accent bg-admin-accent-soft text-admin-accent"
                : "border-admin-border bg-admin-surface text-admin-text-secondary hover:border-admin-accent-border hover:bg-admin-surface-muted"
            }`}
          >
            {preset.label}
          </button>
        );
      })}
    </div>
  );
}
