import { PRODUCT_STATUS_FILTER_TABS } from "../constants/adminProductRemovalConstants";

function StatusTab({ active, disabled, onClick, children }) {
  return (
    <button
      type="button"
      disabled={disabled}
      onClick={onClick}
      className={[
        "whitespace-nowrap border-b-2 px-4 py-3 text-sm font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-50 sm:px-6",
        active
          ? "border-admin-accent text-admin-accent"
          : "border-transparent text-admin-text-secondary hover:border-admin-border hover:text-admin-accent",
      ].join(" ")}
    >
      {children}
    </button>
  );
}

export function AdminProductRemovalFilters({
  activeStatusTabId,
  onStatusChange,
  disabled,
}) {
  return (
    <div className="flex items-center gap-0 overflow-x-auto border-b border-admin-border-subtle">
      {PRODUCT_STATUS_FILTER_TABS.map((tab) => (
        <StatusTab
          key={tab.id}
          active={tab.id === activeStatusTabId}
          disabled={disabled}
          onClick={() => onStatusChange(tab.id)}
        >
          {tab.label}
        </StatusTab>
      ))}
    </div>
  );
}
