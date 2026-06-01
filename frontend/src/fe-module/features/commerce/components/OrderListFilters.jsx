import { ORDER_STATUS_FILTERS } from "../constants/orderListConstants";

export function OrderListFilters({ activeFilterId, onChange, disabled }) {
  return (
    <div
      className="-mx-1 flex gap-2 overflow-x-auto overscroll-x-contain px-1 pb-1 scrollbar-thin"
      role="tablist"
      aria-label="Lọc trạng thái đơn hàng"
    >
      {ORDER_STATUS_FILTERS.map((filter) => {
        const active = activeFilterId === filter.id;

        return (
          <button
            key={filter.id}
            type="button"
            role="tab"
            aria-selected={active}
            disabled={disabled}
            onClick={() => onChange(filter.status)}
            className={[
              "shrink-0 rounded-full px-4 py-2 text-label-md transition-colors",
              active
                ? "bg-primary text-on-primary"
                : "border border-outline-variant bg-surface-container-lowest text-on-surface-variant hover:bg-surface-container-low",
              disabled ? "cursor-not-allowed opacity-60" : "",
            ].join(" ")}
          >
            {filter.label}
          </button>
        );
      })}
    </div>
  );
}
