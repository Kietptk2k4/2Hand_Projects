export function SellerShipmentListHeader({
  clientSearch,
  onSearchChange,
  onCreateClick,
  searchDisabled,
}) {
  return (
    <header className="mb-6 flex flex-col gap-4 border-b border-outline-variant/60 pb-4 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 className="text-xl font-black text-on-surface sm:text-2xl">
          QUẢN LÝ VẬN CHUYỂN (SELLER)
        </h1>
        <p className="mt-1 text-xs font-semibold text-on-surface-variant">
          Theo dõi và quản lý vận đơn giao hàng của cửa hàng
        </p>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        <div className="relative min-w-[260px]">
          <span
            className="material-symbols-outlined pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 text-lg"
            aria-hidden="true"
          >
            search
          </span>
          <input
            type="search"
            value={clientSearch}
            onChange={(e) => onSearchChange(e.target.value)}
            disabled={searchDisabled}
            placeholder="Mã vận đơn, đơn hàng..."
            className="w-full rounded-2xl border border-outline-variant bg-surface-container-lowest py-3 pl-10 pr-4 text-xs text-on-surface shadow-xs transition-all focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20 sm:text-sm disabled:opacity-50"
          />
        </div>
        <button
          type="button"
          onClick={onCreateClick}
          disabled={searchDisabled}
          className="inline-flex items-center justify-center gap-1.5 rounded-xl bg-primary px-5 py-3 text-xs font-bold text-on-primary shadow-xs transition-all hover:bg-[#0050cb] active:scale-95 disabled:opacity-50 cursor-pointer"
        >
          <span className="material-symbols-outlined text-lg" aria-hidden="true">
            add
          </span>
          Tạo vận đơn
        </button>
      </div>
    </header>
  );
}
