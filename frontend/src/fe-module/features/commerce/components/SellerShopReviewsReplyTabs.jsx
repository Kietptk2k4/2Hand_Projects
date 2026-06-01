import { REPLY_FILTER_TABS } from "../constants/sellerShopReviewsConstants";

export function SellerShopReviewsReplyTabs({ activeTabId, tabCounts, onChange, disabled }) {
  return (
    <div className="flex flex-wrap gap-6 border-b border-outline-variant bg-surface px-6 pt-4">
      {REPLY_FILTER_TABS.map((tab) => {
        const active = tab.id === activeTabId;
        const count = tabCounts[tab.id] ?? 0;

        return (
          <button
            key={tab.id}
            type="button"
            disabled={disabled}
            onClick={() => onChange(tab.id)}
            className={[
              "border-b-2 px-2 pb-3 text-label-md transition-colors disabled:opacity-50",
              active
                ? "border-primary font-medium text-primary"
                : "border-transparent text-on-surface-variant hover:text-on-surface",
            ].join(" ")}
          >
            {tab.label} ({count})
          </button>
        );
      })}
    </div>
  );
}
