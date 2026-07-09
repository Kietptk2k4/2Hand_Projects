import { AdminFilterButton, AdminSurfaceCard } from "../../components/ui";
import { SupportDetailRow } from "./ui/SupportDetailRow.jsx";
import { SupportListSkeleton } from "./ui/SupportListSkeleton.jsx";
import { SupportRetryPanel } from "./ui/SupportRetryPanel.jsx";

export function AdminRefundApprovalDetailDrawerView({
  refundRequestId,
  status,
  errorMessage,
  item,
  statusLabels,
  requestedByLabels,
  formatDateTime,
  onClose,
  onRetry,
}) {
  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      <button
        type="button"
        aria-label="Đóng chi tiết hoàn tiền"
        className="absolute inset-0 bg-admin-text/40"
        onClick={onClose}
      />
      <aside className="relative flex h-full min-h-dvh w-full flex-col border-l border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)] lg:max-w-xl">
        <div className="flex items-start justify-between border-b border-admin-border-subtle px-4 py-4 sm:px-6">
          <div className="min-w-0 pr-4">
            <h2 className="text-lg font-semibold text-admin-text">Chi tiết yêu cầu hoàn tiền</h2>
            <p className="mt-1 break-all font-mono text-xs text-admin-text-muted">{refundRequestId}</p>
          </div>
          <AdminFilterButton type="button" variant="secondary" onClick={onClose}>
            Đóng
          </AdminFilterButton>
        </div>

        <div className="flex-1 overflow-y-auto px-4 py-4 sm:px-6">
          {status === "loading" ? <SupportListSkeleton rows={4} /> : null}
          {status === "error" ? <SupportRetryPanel message={errorMessage} onRetry={onRetry} /> : null}
          {status === "ready" && item ? (
            <div className="space-y-6">
              <div className="grid gap-4 sm:grid-cols-2">
                <SupportDetailRow
                  label="Trạng thái"
                  value={statusLabels[item.status] || item.status}
                />
                <SupportDetailRow label="Số tiền" value={item.amountLabel} />
                <SupportDetailRow
                  label="Người yêu cầu"
                  value={requestedByLabels[item.requestedBy] || item.requestedBy}
                />
                <SupportDetailRow label="Lý do" value={item.reason} />
                <SupportDetailRow label="Thanh toán" value={item.paymentMethod} />
                <SupportDetailRow label="TT thanh toán đơn" value={item.orderPaymentStatus} />
                <SupportDetailRow label="TT đơn hàng" value={item.orderStatus} />
                <SupportDetailRow label="Thời gian yêu cầu" value={formatDateTime(item.requestedAt)} />
                <SupportDetailRow label="Xác nhận lúc" value={formatDateTime(item.confirmedAt)} />
                <SupportDetailRow label="Từ chối lúc" value={formatDateTime(item.rejectedAt)} />
                <SupportDetailRow label="Đơn hàng" value={item.orderId} mono />
                <SupportDetailRow label="Thanh toán ID" value={item.paymentId} mono />
                <SupportDetailRow label="Người mua" value={item.buyerId} mono />
                <SupportDetailRow label="Người yêu cầu ID" value={item.requestedByUserId} mono />
              </div>
              {item.adminNote ? (
                <AdminSurfaceCard padding="md" className="bg-admin-surface-muted/50">
                  <h3 className="mb-2 text-sm font-semibold text-admin-text">Ghi chú admin</h3>
                  <p className="text-sm text-admin-text">{item.adminNote}</p>
                </AdminSurfaceCard>
              ) : null}
            </div>
          ) : null}
        </div>
      </aside>
    </div>
  );
}
