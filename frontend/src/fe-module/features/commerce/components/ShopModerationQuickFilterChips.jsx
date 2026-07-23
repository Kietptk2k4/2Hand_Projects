import { SHOP_MODERATION_QUICK_FILTER_PRESETS } from "../constants/shopModerationListConstants.js";
import { isShopModerationQuickPresetActive } from "../utils/shopModerationFilterHelpers.js";

function StatusChip({ active, onClick, children }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={[
        "inline-flex min-h-9 items-center rounded-full border px-3 py-1.5 text-xs font-medium transition-colors",
        active
          ? "border-admin-accent bg-admin-accent-soft text-admin-accent-strong"
          : "border-admin-border text-admin-text-secondary hover:border-admin-accent/40 hover:text-admin-accent",
      ].join(" ")}
    >
      {children}
    </button>
  );
}

export function ShopModerationQuickFilterChips({ filters, onQuickFilter }) {
  return (
    <div className="flex flex-wrap gap-2">
      {SHOP_MODERATION_QUICK_FILTER_PRESETS.map((preset) => (
        <StatusChip
          key={preset.id}
          active={isShopModerationQuickPresetActive(filters, preset.id)}
          onClick={() => onQuickFilter?.(preset.id)}
        >
          {preset.label}
        </StatusChip>
      ))}
    </div>
  );
}
