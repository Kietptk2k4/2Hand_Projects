export function CartTableHeader({
  allEligibleSelected = false,
  eligibleCount = 0,
  isMutating = false,
  onToggleSelectAll,
  selectAllRef,
}) {
  return (
    <div className="hidden items-center justify-between rounded-2xl border border-outline-variant bg-surface-container-lowest px-5 py-3.5 shadow-xs sm:flex text-xs font-bold text-on-surface-variant">
      <div className="flex items-center gap-3 w-5/12">
        <input
          ref={selectAllRef}
          type="checkbox"
          checked={allEligibleSelected}
          disabled={eligibleCount === 0 || isMutating}
          onChange={onToggleSelectAll}
          className="h-4 w-4 rounded border-outline-variant text-primary focus:ring-primary/20 disabled:opacity-40 cursor-pointer"
          aria-label="Chọn tất cả sản phẩm có thể thanh toán"
        />
        <span className="text-sm font-bold text-on-surface">
          Sản phẩm ({eligibleCount} sản phẩm khả dụng)
        </span>
      </div>

      <div className="grid w-7/12 grid-cols-4 items-center text-center">
        <span>Đơn giá</span>
        <span>Số lượng</span>
        <span>Số tiền</span>
        <span className="text-right">Thao tác</span>
      </div>
    </div>
  );
}
