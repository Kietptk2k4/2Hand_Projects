export function SellerOrderListHeader({
  totalItems,
  clientSearch,
  onSearchChange,
  onBulkPrepare,
  bulkDisabled,
  searchDisabled,
}) {
  return (
    <header className="mb-6">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-headline-md font-bold text-on-surface md:text-headline-lg">
            Quản lý đơn hàng
          </h1>
          <p className="mt-1 text-body-sm text-on-surface-variant">
            Theo dõi và xử lý đơn bán của shop bạn.
            {totalItems > 0 ? (
              <span className="text-on-surface">
                {" "}
                · {totalItems} mục
              </span>
            ) : null}
          </p>
        </div>

        <button
          type="button"
          disabled={bulkDisabled}
          onClick={onBulkPrepare}
          className="inline-flex items-center gap-1 rounded-lg bg-primary px-4 py-2.5 text-label-md font-medium text-on-primary hover:bg-[#0050cb] disabled:cursor-not-allowed disabled:opacity-50"
        >
          <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
            inventory_2
          </span>
          Xác nhận chuẩn bị hàng
        </button>
      </div>

      <div className="mt-4">
        <label className="relative block max-w-md">
          <span className="sr-only">Tìm kiếm</span>
          <span
            className="material-symbols-outlined pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-[20px] text-on-surface-variant"
            aria-hidden="true"
          >
            search
          </span>
          <input
            type="search"
            value={clientSearch}
            disabled={searchDisabled}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder="Tìm theo mã đơn hoặc tên sản phẩm (trang hiện tại)"
            className="w-full rounded-lg border border-outline-variant bg-surface-container-lowest py-2.5 pl-10 pr-4 text-body-sm text-on-surface focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary disabled:opacity-50"
          />
        </label>
        <p className="mt-1 text-body-sm text-on-surface-variant">
          Tìm kiếm chỉ áp dụng trên trang hiện tại — API chưa hỗ trợ tham số tìm kiếm.
        </p>
      </div>
    </header>
  );
}
