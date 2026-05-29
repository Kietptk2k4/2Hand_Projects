import { FEED_TAB_OPTIONS } from "../constants/feedTabs";

export function FeedTabs({ activeTab, onChange }) {
  return (
    <div className="flex overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-sm">
      {FEED_TAB_OPTIONS.map((tab) => {
        const isActive = tab.id === activeTab;
        return (
          <button
            key={tab.id}
            type="button"
            onClick={() => onChange(tab.id)}
            className={[
              "flex-1 py-4 text-center text-sm font-medium transition-colors",
              isActive
                ? "border-b-2 border-primary bg-surface-container-low text-primary"
                : "text-on-surface-variant hover:bg-surface-container-low",
            ].join(" ")}
            aria-selected={isActive}
            role="tab"
          >
            {tab.label}
          </button>
        );
      })}
    </div>
  );
}
