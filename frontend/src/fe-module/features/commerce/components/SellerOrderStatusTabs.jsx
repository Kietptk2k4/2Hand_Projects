import { STATUS_TABS } from "../constants/sellerOrderConstants";

export function SellerOrderStatusTabs({ activeTabId, pendingCount, onChange, disabled }) {
  return (
    <div className="-mx-1 mb-4 flex gap-1 overflow-x-auto border-b border-outline-variant pb-1">
      {STATUS_TABS.map((tab) => {
        const active = tab.id === activeTabId;
        const showPendingBadge = tab.id === "pending" && pendingCount > 0;

        return (
          <button
            key={tab.id}
            type="button"
            disabled={disabled}
            onClick={() => onChange(tab.status)}
            className={[
              "flex shrink-0 items-center gap-2 rounded-t-lg px-4 py-2 text-label-md transition-colors",
              active
                ? "border-b-2 border-primary font-semibold text-primary"
                : "text-on-surface-variant hover:text-on-surface",
              disabled ? "opacity-50" : "",
            ].join(" ")}
          >
            {tab.label}
            {showPendingBadge ? (
              <span className="rounded-full bg-amber-100 px-2 py-0.5 text-[11px] font-semibold text-amber-900">
                {pendingCount}
              </span>
            ) : null}
          </button>
        );
      })}
    </div>
  );
}
