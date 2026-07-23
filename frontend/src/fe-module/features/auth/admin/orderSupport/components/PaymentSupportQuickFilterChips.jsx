import { PAYMENT_QUICK_FILTER_PRESETS } from "../constants/paymentSupportListConstants.js";
import { isPaymentSupportQuickPresetActive } from "../utils/paymentSupportFilterHelpers.js";

export function PaymentSupportQuickFilterChips({ filters, onQuickFilter }) {
  return (
    <div className="flex flex-wrap gap-2">
      {PAYMENT_QUICK_FILTER_PRESETS.map((preset) => {
        const active = isPaymentSupportQuickPresetActive(filters, preset.id);
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
