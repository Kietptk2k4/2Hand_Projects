import { SHOP_STATUS_FILTER_TABS, SORT_OPTIONS } from "../constants/adminShopModerationConstants";

export function AdminShopModerationFilters({
  activeStatusTabId,
  onStatusChange,
  sort,
  onSortChange,
  searchInput,
  onSearchInputChange,
  onSearchSubmit,
  disabled,
}) {
  return (
    <div className="mb-6 flex flex-col gap-4 rounded-xl border border-outline-variant bg-surface-container-lowest p-4 shadow-sm lg:flex-row lg:items-center lg:justify-between">
      <div className="flex flex-wrap gap-2">
        {SHOP_STATUS_FILTER_TABS.map((tab) => {
          const active = tab.id === activeStatusTabId;
          return (
            <button
              key={tab.id}
              type="button"
              disabled={disabled}
              onClick={() => onStatusChange(tab.id)}
              className={[
                "rounded-full border px-4 py-1.5 text-label-sm transition-colors disabled:opacity-50",
                active
                  ? "border-primary bg-primary/10 font-medium text-primary"
                  : "border-outline-variant text-on-surface-variant hover:border-primary/50 hover:text-primary",
              ].join(" ")}
            >
              {tab.label}
            </button>
          );
        })}
      </div>

      <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
        <form
          className="relative min-w-[200px] flex-1"
          onSubmit={(e) => {
            e.preventDefault();
            onSearchSubmit?.();
          }}
        >
          <span
            className="material-symbols-outlined pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-[20px] text-on-surface-variant"
            aria-hidden="true"
          >
            search
          </span>
          <input
            type="search"
            value={searchInput}
            onChange={(e) => onSearchInputChange(e.target.value)}
            disabled={disabled}
            placeholder="Tìm shop, seller..."
            className="w-full rounded-lg border border-outline-variant bg-surface-container-lowest py-2 pl-10 pr-3 text-body-sm disabled:opacity-50"
          />
        </form>

        <div className="relative min-w-[180px]">
          <span
            className="material-symbols-outlined pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-[18px] text-on-surface-variant"
            aria-hidden="true"
          >
            filter_list
          </span>
          <select
            value={sort}
            onChange={(e) => onSortChange(e.target.value)}
            disabled={disabled}
            className="w-full appearance-none rounded-lg border border-outline-variant bg-surface-container-lowest py-2 pl-10 pr-8 text-body-sm disabled:opacity-50"
            aria-label="Sắp xếp"
          >
            {SORT_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>
      </div>
    </div>
  );
}
