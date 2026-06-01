export function SellerShipmentListHeader({
  clientSearch,
  onSearchChange,
  onCreateClick,
  searchDisabled,
}) {
  return (
    <header className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 className="text-headline-lg-mobile font-semibold text-on-surface md:text-headline-lg">
          Quản lý Vận chuyển
        </h1>
        <p className="mt-1 text-body-sm text-on-surface-variant">Vận đơn đang hoạt động</p>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        <div className="relative min-w-[240px]">
          <span
            className="material-symbols-outlined pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-[20px] text-on-surface-variant"
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
            className="w-full rounded-lg border border-outline-variant bg-surface-container-lowest py-2.5 pl-10 pr-3 text-body-sm text-on-surface placeholder:text-on-surface-variant disabled:opacity-50"
          />
        </div>
        <button
          type="button"
          onClick={onCreateClick}
          disabled={searchDisabled}
          className="inline-flex items-center justify-center gap-2 rounded-lg bg-primary px-5 py-2.5 text-label-md font-medium text-on-primary hover:bg-[#0050cb] disabled:opacity-50"
        >
          <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
            add
          </span>
          Tạo vận đơn
        </button>
      </div>
    </header>
  );
}
