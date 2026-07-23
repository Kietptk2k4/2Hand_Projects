import { PAYMENT_SUPPORT_VIEW_MODES } from "../constants/paymentSupportListConstants.js";
import { formatPaymentMethodLabel } from "../utils/orderSupportDisplayUtils.js";
import { AdminSurfaceCard } from "../../components/ui";
import { SupportCrossLinkButton } from "./ui/SupportCrossLinkButton.jsx";
import { SupportDetailRow } from "./ui/SupportDetailRow.jsx";
import { SupportTimeline } from "./ui/SupportTimeline.jsx";
import { SupportForbiddenState } from "./SupportForbiddenState.jsx";
import { SupportStatusBadge } from "./SupportStatusBadge.jsx";

export function PaymentSupportDetailPanelView({
  detail,
  orderId,
  canReadPayment,
  formatDateTime,
  formatVndPrice,
  onNavigateToOrder,
  onNavigateToWebhookLogs,
  viewMode = "summary",
}) {
  if (!detail) return null;

  const showSummary = viewMode === PAYMENT_SUPPORT_VIEW_MODES.SUMMARY || viewMode === "summary" || !viewMode;
  const showTimeline = viewMode === PAYMENT_SUPPORT_VIEW_MODES.TIMELINE;
  const showWebhooks = viewMode === PAYMENT_SUPPORT_VIEW_MODES.WEBHOOKS;
  const showOutstandingAlert =
    showSummary && detail.reconciliation_status === "OUTSTANDING";

  return (
    <div className="space-y-4">
      {!canReadPayment ? (
        <SupportForbiddenState message="Tài khoản thiếu quyền PAYMENT_SUPPORT_READ." />
      ) : null}

      {showOutstandingAlert ? (
        <AdminSurfaceCard padding="lg" className="border-admin-warning/30 bg-admin-warning-soft/50">
          <p className="text-sm font-medium text-admin-text">Cảnh báo đối soát</p>
          <p className="mt-1 text-sm text-admin-text-secondary">
            Giao dịch đã thanh toán tại gateway nhưng hệ thống chưa nhận webhook hợp lệ. Kiểm tra
            nhật ký webhook và đối soát thủ công nếu cần.
          </p>
        </AdminSurfaceCard>
      ) : null}

      {showSummary ? (
        <AdminSurfaceCard padding="lg">
          <div className="mb-4 flex flex-wrap items-center gap-2">
            <SupportStatusBadge status={detail.status} kind="payment" />
            <SupportStatusBadge status={detail.reconciliation_status} kind="reconciliation" />
          </div>
          <dl className="space-y-3">
            <SupportDetailRow label="Payment ID" value={detail.payment_id} mono />
            <SupportDetailRow
              label="Order ID"
              value={
                detail.order_id ? (
                  <button
                    type="button"
                    onClick={onNavigateToOrder}
                    className="break-all font-mono text-xs text-admin-accent hover:underline"
                  >
                    {detail.order_id}
                  </button>
                ) : (
                  orderId || "—"
                )
              }
            />
            <SupportDetailRow label="Payer ID" value={detail.payer_id} mono />
            <SupportDetailRow
              label="Phương thức"
              value={formatPaymentMethodLabel(detail.payment_method)}
            />
            <SupportDetailRow label="Số tiền" value={formatVndPrice(detail.amount)} />
            <SupportDetailRow label="Provider order code" value={detail.provider_order_code} mono />
            <SupportDetailRow label="Provider transaction" value={detail.provider_transaction_id} mono />
            <SupportDetailRow label="Thanh toán lúc" value={formatDateTime(detail.paid_at)} />
            <SupportDetailRow label="Tạo lúc" value={formatDateTime(detail.created_at)} />
          </dl>
        </AdminSurfaceCard>
      ) : null}

      {showTimeline && detail.status_timeline?.length > 0 ? (
        <AdminSurfaceCard padding="lg">
          <SupportTimeline
            title="Timeline thanh toán"
            entries={detail.status_timeline}
            formatDateTime={formatDateTime}
          />
        </AdminSurfaceCard>
      ) : null}

      {showTimeline && !detail.status_timeline?.length ? (
        <p className="text-sm text-admin-text-secondary">Chưa có timeline.</p>
      ) : null}

      {showWebhooks ? (
        <AdminSurfaceCard padding="lg" className="max-w-full min-w-0">
          <div className="mb-3 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
            <h3 className="text-base font-semibold text-admin-text">Lịch sử webhook</h3>
            {detail.provider_order_code ? (
              <SupportCrossLinkButton onClick={onNavigateToWebhookLogs}>
                Xem trong Webhook logs
              </SupportCrossLinkButton>
            ) : null}
          </div>
          {detail.webhook_events?.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[560px] text-left text-sm">
                <thead>
                  <tr className="border-b border-admin-border text-admin-text-secondary">
                    <th className="py-2 pr-4 font-medium">Provider</th>
                    <th className="py-2 pr-4 font-medium">Event</th>
                    <th className="py-2 pr-4 font-medium">Chữ ký</th>
                    <th className="py-2 pr-4 font-medium">Processed</th>
                    <th className="py-2 font-medium">Nhận lúc</th>
                  </tr>
                </thead>
                <tbody>
                  {detail.webhook_events.map((event, index) => (
                    <tr key={`${event.received_at}-${index}`} className="border-b border-admin-border-subtle">
                      <td className="py-3 pr-4">{event.provider}</td>
                      <td className="py-3 pr-4">{event.event_type}</td>
                      <td className="py-3 pr-4">{event.signature_valid ? "Hợp lệ" : "Không hợp lệ"}</td>
                      <td className="py-3 pr-4">{event.processed ? "Có" : "Chưa"}</td>
                      <td className="py-3">{formatDateTime(event.received_at)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-sm text-admin-text-secondary">Chưa có webhook event.</p>
          )}
        </AdminSurfaceCard>
      ) : null}
    </div>
  );
}
