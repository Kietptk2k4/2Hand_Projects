import { AdminFilterButton } from "../../components/ui";
import { SupportForbiddenState } from "./SupportForbiddenState.jsx";
import { SupportListSkeleton } from "./ui/SupportListSkeleton.jsx";
import { SupportRetryPanel } from "./ui/SupportRetryPanel.jsx";
import { SupportStatusBadge } from "./SupportStatusBadge.jsx";

function CopyButton({ value, label, onCopied }) {
  if (!value) return null;
  return (
    <button
      type="button"
      className="text-xs font-medium text-admin-accent hover:underline"
      onClick={async () => {
        await navigator.clipboard.writeText(String(value));
        onCopied?.();
      }}
    >
      {label}
    </button>
  );
}

function PayloadTable({ summary }) {
  if (!summary || typeof summary !== "object") {
    return <p className="text-sm text-admin-text-muted">Không có payload.</p>;
  }

  if (summary.parse_error) {
    return (
      <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
        Không parse được payload webhook.
      </div>
    );
  }

  const entries = Object.entries(summary);
  return (
    <div className="overflow-hidden rounded-lg border border-admin-border">
      <table className="min-w-full text-sm">
        <tbody>
          {entries.map(([key, value]) => (
            <tr key={key} className="border-b border-admin-border-subtle last:border-b-0">
              <th className="w-40 bg-admin-surface-muted px-3 py-2 text-left font-medium text-admin-text-secondary">
                {key}
              </th>
              <td className="px-3 py-2 font-mono text-xs text-admin-text">{String(value)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export function WebhookSupportDrawerView({
  open,
  webhookLogId,
  detail,
  loading,
  errorMessage,
  status,
  canReadWebhook,
  formatDateTime,
  statusLabel,
  onClose,
  onRetry,
  onCopied,
  onNavigateToOrder,
  onNavigateToPayment,
  onNavigateToShipment,
}) {
  if (!open || !webhookLogId) return null;

  return (
    <div className="fixed inset-0 z-[100] flex min-h-dvh justify-end bg-admin-text/40 backdrop-blur-sm">
      <button type="button" aria-label="Đóng" className="absolute inset-0" onClick={onClose} />
      <aside className="relative flex h-full min-h-dvh w-full max-w-2xl flex-col border-l border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)]">
        <div className="flex items-start justify-between gap-3 border-b border-admin-border bg-admin-surface-muted px-4 py-4 sm:px-6">
          <div className="min-w-0">
            <p className="text-xs font-medium tracking-wide text-admin-text-muted uppercase">
              Nhật ký webhook
            </p>
            {detail ? (
              <div className="mt-2 flex flex-wrap items-center gap-2">
                <span className="rounded-md bg-admin-accent-soft px-2 py-0.5 text-xs font-bold text-admin-accent-strong">
                  {detail.provider}
                </span>
                {statusLabel ? <SupportStatusBadge status={statusLabel} /> : null}
              </div>
            ) : null}
            <p className="mt-2 font-mono text-sm text-admin-text">{webhookLogId}</p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="inline-flex min-h-11 min-w-11 items-center justify-center rounded-lg text-admin-text-secondary hover:bg-admin-surface"
            aria-label="Đóng drawer"
          >
            <span className="material-symbols-outlined" aria-hidden="true">
              close
            </span>
          </button>
        </div>

        <div className="flex-1 overflow-y-auto px-4 py-4 sm:px-6">
          {!canReadWebhook ? <SupportForbiddenState message="Thiếu quyền WEBHOOK_SUPPORT_READ." /> : null}
          {loading ? <SupportListSkeleton /> : null}
          {status === "error" ? <SupportRetryPanel message={errorMessage} onRetry={onRetry} /> : null}

          {detail ? (
            <div className="space-y-6">
              <section className="grid gap-3 sm:grid-cols-2">
                <div>
                  <p className="text-xs text-admin-text-muted">Mã tham chiếu</p>
                  <div className="mt-1 flex items-center gap-2">
                    <p className="font-mono text-sm">{detail.reference_id}</p>
                    <CopyButton value={detail.reference_id} label="Sao chép" onCopied={onCopied} />
                  </div>
                </div>
                <div>
                  <p className="text-xs text-admin-text-muted">Nhận lúc</p>
                  <p className="mt-1 text-sm">{formatDateTime(detail.received_at)}</p>
                </div>
                <div>
                  <p className="text-xs text-admin-text-muted">Loại sự kiện</p>
                  <p className="mt-1 text-sm">{detail.event_type}</p>
                </div>
                <div>
                  <p className="text-xs text-admin-text-muted">Chữ ký</p>
                  <p className="mt-1 text-sm">
                    {detail.signature_valid == null
                      ? "—"
                      : detail.signature_valid
                        ? "Hợp lệ"
                        : "Không hợp lệ"}
                  </p>
                </div>
                <div className="sm:col-span-2">
                  <p className="text-xs text-admin-text-muted">Idempotency key</p>
                  <div className="mt-1 flex items-center gap-2">
                    <p className="break-all font-mono text-xs">{detail.idempotency_key}</p>
                    <CopyButton value={detail.idempotency_key} label="Sao chép" onCopied={onCopied} />
                  </div>
                </div>
              </section>

              <section className="flex flex-wrap gap-2">
                {detail.order_id ? (
                  <AdminFilterButton type="button" variant="secondary" onClick={onNavigateToOrder}>
                    Xem đơn hàng
                  </AdminFilterButton>
                ) : null}
                {detail.payment_id ? (
                  <AdminFilterButton type="button" variant="secondary" onClick={onNavigateToPayment}>
                    Xem thanh toán
                  </AdminFilterButton>
                ) : null}
                {detail.shipment_id ? (
                  <AdminFilterButton type="button" variant="secondary" onClick={onNavigateToShipment}>
                    Xem vận chuyển
                  </AdminFilterButton>
                ) : null}
              </section>

              <section>
                <div className="mb-2 flex items-center justify-between gap-2">
                  <h3 className="text-sm font-semibold text-admin-text">Payload (đã sanitize)</h3>
                  <CopyButton
                    value={JSON.stringify(detail.payload_summary, null, 2)}
                    label="Sao chép JSON"
                    onCopied={onCopied}
                  />
                </div>
                <PayloadTable summary={detail.payload_summary} />
              </section>
            </div>
          ) : null}
        </div>
      </aside>
    </div>
  );
}
