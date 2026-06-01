import { useMemo, useRef, useEffect } from "react";
import { SellerOrderTableRow } from "./SellerOrderTableRow";

export function SellerOrderTable({
  items,
  disabled,
  selectedIds,
  onToggleSelect,
  onToggleSelectAllPending,
  onPrepareRow,
  isProcessing,
}) {
  const pendingOnPage = useMemo(
    () => items.filter((item) => item.itemStatus === "PENDING"),
    [items],
  );

  const pendingIds = useMemo(
    () => pendingOnPage.map((item) => item.orderItemId),
    [pendingOnPage],
  );

  const allPendingSelected =
    pendingIds.length > 0 && pendingIds.every((id) => selectedIds.has(id));

  const somePendingSelected =
    pendingIds.some((id) => selectedIds.has(id)) && !allPendingSelected;

  const selectAllRef = useRef(null);

  useEffect(() => {
    if (selectAllRef.current) {
      selectAllRef.current.indeterminate = somePendingSelected;
    }
  }, [somePendingSelected]);

  return (
    <div className="overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-sm">
      <div className="overflow-x-auto">
        <table className="min-w-full text-left">
          <thead className="border-b border-outline-variant bg-surface-bright text-label-md text-on-surface-variant">
            <tr>
              <th className="w-10 px-3 py-3" scope="col">
                <input
                  ref={selectAllRef}
                  type="checkbox"
                  checked={allPendingSelected}
                  disabled={disabled || pendingIds.length === 0}
                  onChange={() => onToggleSelectAllPending?.(pendingIds, !allPendingSelected)}
                  className="h-4 w-4 rounded border-outline-variant text-primary disabled:opacity-40"
                  aria-label="Chọn tất cả đơn chờ xử lý trên trang"
                />
              </th>
              <th className="px-3 py-3 font-medium">Mã đơn hàng</th>
              <th className="min-w-[200px] px-3 py-3 font-medium">Sản phẩm</th>
              <th className="w-16 px-3 py-3 text-center font-medium">SL</th>
              <th className="px-3 py-3 text-right font-medium">Tổng tiền</th>
              <th className="px-3 py-3 font-medium">Trạng thái / TT</th>
              <th className="min-w-[140px] px-3 py-3 font-medium">Vận chuyển</th>
              <th className="w-24 px-3 py-3 text-right font-medium">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <SellerOrderTableRow
                key={item.orderItemId}
                item={item}
                disabled={disabled}
                isProcessing={isProcessing}
                selected={selectedIds.has(item.orderItemId)}
                onToggleSelect={onToggleSelect}
                onPrepareRow={onPrepareRow}
              />
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
