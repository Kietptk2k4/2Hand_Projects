import { STATUS_TABS } from "../constants/sellerProductConstants";

export function SellerProductStatusTabs({ activeTabId, onChange, disabled }) {
  return (
    <div className="mb-4 flex items-center gap-2 overflow-x-auto rounded-2xl border border-outline-variant bg-surface-container-lowest p-2 shadow-xs no-scrollbar">
      {STATUS_TABS.map((tab) => {
        const active = tab.id === activeTabId;
        return (
          <button
            key={tab.id}
            type="button"
            disabled={disabled}
            onClick={() => onChange(tab.status)}
            className={[
              "shrink-0 rounded-xl px-4 py-2.5 text-xs font-bold transition-all sm:text-sm cursor-pointer",
              active
                ? "bg-primary text-on-primary shadow-xs"
                : "text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface",
              disabled ? "cursor-not-allowed opacity-60" : "",
            ].join(" ")}
          >
            {tab.label}
          </button>
        );
      })}
    </div>
  );
}
