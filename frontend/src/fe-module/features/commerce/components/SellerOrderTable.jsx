import { SellerOrderTableRow } from "./SellerOrderTableRow";

export function SellerOrderTable({ items, disabled, selectedIds, onToggleSelect }) {
  return (
    <div className="overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-sm">
      <div className="overflow-x-auto">
        <table className="min-w-full text-left">
          <thead className="border-b border-outline-variant bg-surface-bright text-label-md text-on-surface-variant">
            <tr>
              <th className="w-10 px-3 py-3" scope="col">
                <span className="sr-only">Chọn</span>
              </th>
              <th className="px-3 py-3 font-medium">Mã đơn hàng</th>
              <th className="min-w-[200px] px-3 py-3 font-medium">Sản phẩm</th>
              <th className="w-16 px-3 py-3 text-center font-medium">SL</th>
              <th className="px-3 py-3 text-right font-medium">Tổng tiền</th>
              <th className="px-3 py-3 font-medium">Trạng thái</th>
              <th className="min-w-[140px] px-3 py-3 font-medium">Vận chuyển</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <SellerOrderTableRow
                key={item.orderItemId}
                item={item}
                disabled={disabled}
                selected={selectedIds.has(item.orderItemId)}
                onToggleSelect={onToggleSelect}
              />
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
