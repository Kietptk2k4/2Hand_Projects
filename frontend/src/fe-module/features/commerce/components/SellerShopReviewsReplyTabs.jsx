import { REPLY_FILTER_TABS } from "../constants/sellerShopReviewsConstants";

export function SellerShopReviewsReplyTabs({ activeTabId, tabCounts, onChange, disabled }) {
  return (
    <div className="flex items-center gap-2 overflow-x-auto rounded-2xl border border-outline-variant bg-surface-container-lowest p-2 shadow-xs no-scrollbar">
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
              "shrink-0 rounded-xl px-4 py-2.5 text-xs font-bold transition-all sm:text-sm cursor-pointer",
              active
                ? "bg-primary text-on-primary shadow-xs"
                : "text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface",
              disabled ? "cursor-not-allowed opacity-60" : "",
            ].join(" ")}
          >
            {tab.label} ({count})
          </button>
        );
      })}
    </div>
  );
}
