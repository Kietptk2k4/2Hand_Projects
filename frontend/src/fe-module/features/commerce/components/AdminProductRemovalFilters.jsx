import { PRODUCT_STATUS_FILTER_TABS } from "../constants/adminProductRemovalConstants";

export function AdminProductRemovalFilters({
  activeStatusTabId,
  onStatusChange,
  disabled,
}) {
  return (
    <div className="flex items-center gap-0 overflow-x-auto border-b border-outline-variant px-2">
      {PRODUCT_STATUS_FILTER_TABS.map((tab) => {
        const active = tab.id === activeStatusTabId;
        return (
          <button
            key={tab.id}
            type="button"
            disabled={disabled}
            onClick={() => onStatusChange(tab.id)}
            className={[
              "whitespace-nowrap px-6 py-4 text-label-md transition-colors disabled:opacity-50",
              active
                ? "border-b-2 border-primary font-medium text-primary"
                : "text-on-surface-variant hover:bg-surface-container-low hover:text-primary",
            ].join(" ")}
          >
            {tab.label}
          </button>
        );
      })}
    </div>
  );
}
