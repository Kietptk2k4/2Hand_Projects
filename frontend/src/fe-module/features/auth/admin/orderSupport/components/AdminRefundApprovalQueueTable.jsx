import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminFilterButton,
  AdminMobileCard,
  AdminMobileCardList,
  AdminStatusBadge,
} from "../../components/ui";
import { supportStatusVariant } from "./ui/supportStatusVariant.js";

export function AdminRefundApprovalQueueTable({
  items,
  actionId,
  statusLabels,
  requestedByLabels,
  formatVndPrice,
  formatDateTime,
  onSelectDetail,
  onConfirm,
  onReject,
}) {
  if (!items?.length) {
    return (
      <p className="py-8 text-center text-sm text-admin-text-secondary">Không có yêu cầu hoàn tiền.</p>
    );
  }

  return (
    <>
      <AdminMobileCardList>
        {items.map((item) => (
          <AdminMobileCard key={item.id}>
            <div className="flex items-start justify-between gap-2">
              <p className="font-mono text-xs text-admin-text-muted">{item.orderId}</p>
              <AdminStatusBadge variant={supportStatusVariant(item.status)}>
                {statusLabels[item.status] || item.status}
              </AdminStatusBadge>
            </div>
            <p className="mt-2 text-sm font-medium text-admin-text">{formatVndPrice(item.amount)}</p>
            <p className="mt-1 text-xs text-admin-text-secondary">
              {requestedByLabels[item.requestedBy] || item.requestedBy} · {item.paymentMethod || "—"}
            </p>
            <p className="mt-1 text-xs text-admin-text-muted">
              {item.requestedAt ? formatDateTime(item.requestedAt) : "—"}
            </p>
            <div className="mt-3 flex flex-col gap-2 sm:flex-row sm:flex-wrap">
              <AdminFilterButton
                type="button"
                variant="secondary"
                className="w-full sm:w-auto"
                onClick={() => onSelectDetail(item.id)}
              >
                Chi tiết
              </AdminFilterButton>
              {item.status === "REQUESTED" ? (
                <AdminFilterButton
                  type="button"
                  variant="primary"
                  className="w-full sm:w-auto"
                  disabled={actionId === item.id}
                  onClick={() => onConfirm(item.id)}
                >
                  Xác nhận đã hoàn tiền
                </AdminFilterButton>
              ) : null}
            </div>
          </AdminMobileCard>
        ))}
      </AdminMobileCardList>

      <AdminDataTable minWidth="880px" ariaLabel="Hàng đợi duyệt hoàn tiền">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Thời gian</AdminDataTableCell>
            <AdminDataTableCell header>Đơn hàng</AdminDataTableCell>
            <AdminDataTableCell header>Người yêu cầu</AdminDataTableCell>
            <AdminDataTableCell header>Số tiền</AdminDataTableCell>
            <AdminDataTableCell header>Thanh toán</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header className="text-right">
              Thao tác
            </AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((item) => (
            <AdminDataTableRow key={item.id}>
              <AdminDataTableCell className="text-sm text-admin-text-secondary">
                {item.requestedAt ? formatDateTime(item.requestedAt) : "—"}
              </AdminDataTableCell>
              <AdminDataTableCell className="font-mono text-xs">{item.orderId}</AdminDataTableCell>
              <AdminDataTableCell>
                {requestedByLabels[item.requestedBy] || item.requestedBy}
              </AdminDataTableCell>
              <AdminDataTableCell className="font-medium">{formatVndPrice(item.amount)}</AdminDataTableCell>
              <AdminDataTableCell>{item.paymentMethod || "—"}</AdminDataTableCell>
              <AdminDataTableCell>
                <AdminStatusBadge variant={supportStatusVariant(item.status)}>
                  {statusLabels[item.status] || item.status}
                </AdminStatusBadge>
              </AdminDataTableCell>
              <AdminDataTableCell className="text-right">
                <div className="flex flex-wrap justify-end gap-2">
                  <AdminFilterButton
                    type="button"
                    variant="secondary"
                    onClick={() => onSelectDetail(item.id)}
                  >
                    Chi tiết
                  </AdminFilterButton>
                  {item.status === "REQUESTED" ? (
                    <AdminFilterButton
                      type="button"
                      variant="primary"
                      disabled={actionId === item.id}
                      onClick={() => onConfirm(item.id)}
                    >
                      Xác nhận đã hoàn tiền
                    </AdminFilterButton>
                  ) : null}
                </div>
              </AdminDataTableCell>
            </AdminDataTableRow>
          ))}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
