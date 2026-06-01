import { STATUS_TABS } from "../constants/sellerShipmentConstants";

export function SellerShipmentStatusTabs({ activeTabId, tabCounts, onChange, disabled }) {
  return (
    <div className="mb-6 flex flex-wrap gap-2">
      {STATUS_TABS.map((tab) => {
        const active = tab.id === activeTabId;
        const count = tabCounts[tab.id] ?? 0;

        return (
          <button
            key={tab.id}
            type="button"
            disabled={disabled}
            onClick={() => onChange(tab.id)}
            className={[
              "flex min-w-[7rem] flex-col items-start rounded-xl border px-4 py-3 text-left transition-colors",
              active
                ? "border-primary bg-surface-container-low shadow-sm"
                : "border-outline-variant/60 bg-surface-container-lowest hover:bg-surface-container-low",
              disabled ? "opacity-50" : "",
            ].join(" ")}
          >
            <span
              className={[
                "text-label-sm font-semibold uppercase tracking-wide",
                active ? "text-primary" : "text-on-surface-variant",
              ].join(" ")}
            >
              {tab.label}
            </span>
            <span className="mt-1 text-headline-sm font-bold text-on-surface">{count}</span>
          </button>
        );
      })}
    </div>
  );
}
