import { formatPostListDateTime } from "../../contentModeration/utils/postDateTimeDisplay.js";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminMobileCard,
  AdminMobileCardList,
} from "../../components/ui";
import { formatCarrierLabel } from "../utils/orderSupportDisplayUtils.js";
import { OrderSupportUuidCell } from "./OrderSupportUuidCell.jsx";
import { SupportStatusBadge } from "./SupportStatusBadge.jsx";

function DateTimeCell({ value }) {
  if (!value) return <span className="text-admin-text-muted">—</span>;
  const formatted = formatPostListDateTime(value);
  return (
    <div className="text-xs tabular-nums text-admin-text-secondary">
      <div>{formatted.date}</div>
      <div className="text-admin-text-muted">{formatted.time}</div>
    </div>
  );
}

function ShipmentMobileCard({ row, selected, onShipmentSelect, onCopied }) {
  const formatted = formatPostListDateTime(row.updated_at);
  return (
    <AdminMobileCard
      isSelected={selected}
      onClick={() => onShipmentSelect?.(row.shipment_id)}
      ariaLabel={`Chọn vận đơn ${row.shipment_id}`}
    >
      <OrderSupportUuidCell value={row.shipment_id} onCopied={onCopied} />
      <p className="mt-1 text-xs text-admin-text-secondary">
        Order {row.order_id ? row.order_id.slice(0, 8) : "—"}…
      </p>
      <div className="mt-2 flex flex-wrap items-center gap-2">
        <SupportStatusBadge status={row.internal_status} kind="shipment" />
        <span className="text-xs text-admin-text-muted">{formatCarrierLabel(row.carrier)}</span>
      </div>
      <p className="mt-1 text-xs tabular-nums text-admin-text-muted">
        {row.tracking_number || "—"} · {formatted.date} {formatted.time}
      </p>
    </AdminMobileCard>
  );
}

export function ShipmentSupportTable({
  shipments,
  selectedShipmentId,
  onShipmentSelect,
  onCopied,
}) {
  if (!shipments?.length) {
    return null;
  }

  return (
    <>
      <AdminMobileCardList className="md:hidden">
        {shipments.map((row) => (
          <ShipmentMobileCard
            key={row.shipment_id}
            row={row}
            selected={row.shipment_id === selectedShipmentId}
            onShipmentSelect={onShipmentSelect}
            onCopied={onCopied}
          />
        ))}
      </AdminMobileCardList>

      <AdminDataTable
        minWidth="0"
        tableLayout="auto"
        ariaLabel="Danh sách vận đơn hỗ trợ"
        className="w-full"
      >
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header className="w-[16%] px-4 py-3">
              Shipment ID
            </AdminDataTableCell>
            <AdminDataTableCell header className="w-[16%] px-4 py-3">
              Order ID
            </AdminDataTableCell>
            <AdminDataTableCell header className="w-[8%] px-4 py-3">
              Đơn vị
            </AdminDataTableCell>
            <AdminDataTableCell header className="w-[12%] px-4 py-3">
              Trạng thái
            </AdminDataTableCell>
            <AdminDataTableCell header className="w-[10%] px-4 py-3">
              Tracking
            </AdminDataTableCell>
            <AdminDataTableCell header className="w-[12%] px-4 py-3 whitespace-nowrap">
              Gửi hàng
            </AdminDataTableCell>
            <AdminDataTableCell header className="w-[12%] px-4 py-3 whitespace-nowrap">
              Cập nhật
            </AdminDataTableCell>
            <AdminDataTableCell header className="w-12 px-2" />
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {shipments.map((row) => {
            const isSelected = selectedShipmentId === row.shipment_id;
            return (
              <AdminDataTableRow
                key={row.shipment_id}
                isSelected={isSelected}
                onClick={() => onShipmentSelect?.(row.shipment_id)}
              >
                <AdminDataTableCell className="px-4 py-3.5">
                  <OrderSupportUuidCell value={row.shipment_id} onCopied={onCopied} />
                </AdminDataTableCell>
                <AdminDataTableCell className="px-4 py-3.5">
                  <OrderSupportUuidCell value={row.order_id} onCopied={onCopied} />
                </AdminDataTableCell>
                <AdminDataTableCell className="px-4 py-3.5">
                  {formatCarrierLabel(row.carrier)}
                </AdminDataTableCell>
                <AdminDataTableCell className="px-4 py-3.5">
                  <SupportStatusBadge status={row.internal_status} kind="shipment" />
                </AdminDataTableCell>
                <AdminDataTableCell className="px-4 py-3.5 font-mono text-xs tabular-nums">
                  {row.tracking_number || "—"}
                </AdminDataTableCell>
                <AdminDataTableCell className="px-4 py-3.5">
                  <DateTimeCell value={row.shipped_at} />
                </AdminDataTableCell>
                <AdminDataTableCell className="px-4 py-3.5">
                  <DateTimeCell value={row.updated_at} />
                </AdminDataTableCell>
                <AdminDataTableCell className="px-2">
                  <span className="material-symbols-outlined text-admin-text-muted" aria-hidden="true">
                    chevron_right
                  </span>
                </AdminDataTableCell>
              </AdminDataTableRow>
            );
          })}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
