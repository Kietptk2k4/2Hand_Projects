import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminMobileCard,
  AdminMobileCardList,
} from "../../components/ui";
import { SupportStatusBadge } from "./SupportStatusBadge.jsx";

export function ShipmentSupportTable({
  shipments,
  selectedShipmentId,
  onShipmentSelect,
  formatDateTime,
}) {
  if (!shipments?.length) {
    return (
      <p className="py-6 text-center text-sm text-admin-text-secondary">
        Không có vận đơn phù hợp bộ lọc.
      </p>
    );
  }

  return (
    <>
      <AdminMobileCardList>
        {shipments.map((row) => {
          const isSelected = selectedShipmentId === row.shipment_id;
          return (
            <AdminMobileCard
              key={row.shipment_id}
              isSelected={isSelected}
              onClick={() => onShipmentSelect?.(row.shipment_id)}
              ariaLabel={`Chọn vận đơn ${row.shipment_id}`}
            >
              <p className="font-mono text-xs text-admin-text-muted">{row.shipment_id}</p>
              <p className="mt-1 text-xs text-admin-text-secondary">Order {row.order_id}</p>
              <div className="mt-2 flex flex-wrap items-center gap-2">
                <SupportStatusBadge status={row.internal_status} />
                <span className="text-xs text-admin-text-muted">{row.carrier}</span>
              </div>
              <p className="mt-1 text-xs text-admin-text-muted">
                {row.tracking_number || "—"} · {formatDateTime(row.updated_at)}
              </p>
            </AdminMobileCard>
          );
        })}
      </AdminMobileCardList>

      <AdminDataTable minWidth="960px" ariaLabel="Danh sách vận đơn hỗ trợ">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Shipment ID</AdminDataTableCell>
            <AdminDataTableCell header>Order ID</AdminDataTableCell>
            <AdminDataTableCell header>Carrier</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header>Tracking</AdminDataTableCell>
            <AdminDataTableCell header>Gửi hàng</AdminDataTableCell>
            <AdminDataTableCell header>Tạo lúc</AdminDataTableCell>
            <AdminDataTableCell header>Cập nhật</AdminDataTableCell>
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
                <AdminDataTableCell className="font-mono text-xs">{row.shipment_id}</AdminDataTableCell>
                <AdminDataTableCell className="font-mono text-xs">{row.order_id}</AdminDataTableCell>
                <AdminDataTableCell>{row.carrier}</AdminDataTableCell>
                <AdminDataTableCell>
                  <SupportStatusBadge status={row.internal_status} />
                </AdminDataTableCell>
                <AdminDataTableCell className="font-mono text-xs">
                  {row.tracking_number || "—"}
                </AdminDataTableCell>
                <AdminDataTableCell className="text-sm text-admin-text-secondary">
                  {formatDateTime(row.shipped_at)}
                </AdminDataTableCell>
                <AdminDataTableCell className="text-sm text-admin-text-secondary">
                  {formatDateTime(row.created_at)}
                </AdminDataTableCell>
                <AdminDataTableCell className="text-sm text-admin-text-secondary">
                  {formatDateTime(row.updated_at)}
                </AdminDataTableCell>
              </AdminDataTableRow>
            );
          })}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
