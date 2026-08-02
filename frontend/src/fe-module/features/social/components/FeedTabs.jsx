import { FEED_TAB_OPTIONS } from "../constants/feedTabs";

export function FeedTabs({ activeTab, onChange }) {
  return (
    <div className="sticky top-16 z-30 flex border-b border-outline-variant/40 bg-surface-container-lowest/90 backdrop-blur-md">
      {FEED_TAB_OPTIONS.map((tab) => {
        const isActive = tab.id === activeTab;
        return (
          <button
            key={tab.id}
            type="button"
            onClick={() => onChange(tab.id)}
            className="relative flex flex-1 items-center justify-center py-3.5 text-sm font-semibold transition-colors hover:bg-surface-container-low/50"
            aria-selected={isActive}
            role="tab"
          >
            <span className={isActive ? "font-bold text-on-surface" : "text-on-surface-variant"}>
              {tab.label}
            </span>
            {isActive ? (
              <span className="absolute bottom-0 h-1 w-14 rounded-full bg-primary" />
            ) : null}
          </button>
        );
      })}
    </div>
  );
}
