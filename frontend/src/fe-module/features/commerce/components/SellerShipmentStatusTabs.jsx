import { STATUS_TABS } from "../constants/sellerShipmentConstants";

export function SellerShipmentStatusTabs({ activeTabId, tabCounts, onChange, disabled }) {
  return (
    <div className="mb-6 flex items-center gap-3 overflow-x-auto rounded-2xl border border-outline-variant bg-surface-container-lowest p-3 shadow-xs no-scrollbar">
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
              "flex flex-1 min-w-[7rem] flex-col items-start rounded-xl px-4 py-3 text-left transition-all cursor-pointer",
              active
                ? "bg-primary text-on-primary shadow-xs"
                : "bg-surface-container-low text-on-surface hover:bg-surface-container-high",
              disabled ? "cursor-not-allowed opacity-60" : "",
            ].join(" ")}
          >
            <span
              className={[
                "text-[11px] font-bold uppercase tracking-wider",
                active ? "text-on-primary/80" : "text-on-surface-variant",
              ].join(" ")}
            >
              {tab.label}
            </span>
            <span
              className={[
                "mt-1 text-xl font-black",
                active ? "text-on-primary" : "text-on-surface",
              ].join(" ")}
            >
              {count}
            </span>
          </button>
        );
      })}
    </div>
  );
}
