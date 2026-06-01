import { STATUS_TABS } from "../constants/sellerProductConstants";

export function SellerProductStatusTabs({ activeTabId, onChange, disabled }) {
  return (
    <div className="mb-4 flex flex-wrap gap-2 border-b border-outline-variant pb-1">
      {STATUS_TABS.map((tab) => {
        const active = tab.id === activeTabId;
        return (
          <button
            key={tab.id}
            type="button"
            disabled={disabled}
            onClick={() => onChange(tab.status)}
            className={[
              "rounded-t-lg px-4 py-2 text-label-md transition-colors",
              active
                ? "border-b-2 border-primary font-semibold text-primary"
                : "text-on-surface-variant hover:text-on-surface",
              disabled ? "opacity-50" : "",
            ].join(" ")}
          >
            {tab.label}
          </button>
        );
      })}
    </div>
  );
}
