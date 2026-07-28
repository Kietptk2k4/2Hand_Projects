import { ORDER_STATUS_FILTERS } from "../constants/orderListConstants";

export function OrderListFilters({ activeFilterId, onChange, disabled }) {
  return (
    <div
      className="flex items-center gap-2 overflow-x-auto rounded-2xl border border-outline-variant bg-surface-container-lowest p-2 shadow-xs no-scrollbar"
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
              "shrink-0 rounded-xl px-4 py-2.5 text-xs font-bold transition-all sm:text-sm cursor-pointer",
              active
                ? "bg-primary text-on-primary shadow-xs"
                : "text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface",
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
