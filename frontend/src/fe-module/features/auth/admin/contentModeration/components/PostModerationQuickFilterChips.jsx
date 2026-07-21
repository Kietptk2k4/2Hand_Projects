import { POST_MODERATION_QUICK_FILTER_PRESETS } from "../constants/postModerationListConstants.js";
import { isPostModerationQuickPresetActive } from "../utils/postModerationFilterHelpers.js";

const chipBase =
  "inline-flex min-h-9 items-center rounded-full border px-3 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft";

export function PostModerationQuickFilterChips({ filters, onQuickFilter }) {
  return (
    <div className="flex flex-wrap gap-2">
      {POST_MODERATION_QUICK_FILTER_PRESETS.map((preset) => {
        const isActive = isPostModerationQuickPresetActive(filters, preset.id);
        return (
          <button
            key={preset.id}
            type="button"
            onClick={() => onQuickFilter?.(preset.id)}
            className={[
              chipBase,
              isActive
                ? "border-admin-accent bg-admin-accent-soft text-admin-accent-strong"
                : "border-admin-border bg-admin-surface text-admin-text-secondary hover:border-admin-accent-border hover:bg-admin-surface-muted hover:text-admin-text",
            ].join(" ")}
            aria-pressed={isActive}
          >
            {preset.label}
          </button>
        );
      })}
    </div>
  );
}
