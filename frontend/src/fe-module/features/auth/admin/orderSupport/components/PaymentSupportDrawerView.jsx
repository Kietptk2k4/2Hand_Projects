import { PAYMENT_SUPPORT_VIEW_MODES } from "../constants/paymentSupportListConstants.js";
import { formatPaymentMethodLabel } from "../utils/orderSupportDisplayUtils.js";
import { PaymentSupportDetailPanelView } from "./PaymentSupportDetailPanelView.jsx";
import { SupportListSkeleton } from "./ui/SupportListSkeleton.jsx";
import { SupportRetryPanel } from "./ui/SupportRetryPanel.jsx";
import { SupportForbiddenState } from "./SupportForbiddenState.jsx";
import { SupportUnavailableState } from "./SupportUnavailableState.jsx";
import { SupportStatusBadge } from "./SupportStatusBadge.jsx";

export function PaymentSupportDrawerView({
  open,
  paymentId,
  detail,
  loading,
  errorMessage,
  status,
  paymentView,
  orderId,
  canReadPayment,
  formatDateTime,
  formatVndPrice,
  onClose,
  onViewChange,
  onRetry,
  onNavigateToOrder,
  onNavigateToWebhookLogs,
}) {
  if (!open || !paymentId) return null;

  const tabClass = (active) =>
    [
      "min-h-11 rounded-lg px-3 py-2 text-sm font-medium transition-colors",
      active
        ? "bg-admin-accent-soft text-admin-accent-strong"
        : "text-admin-text-secondary hover:bg-admin-surface-muted",
    ].join(" ");

  const isTimeline = paymentView === PAYMENT_SUPPORT_VIEW_MODES.TIMELINE;
  const isWebhooks = paymentView === PAYMENT_SUPPORT_VIEW_MODES.WEBHOOKS;

  return (
    <div className="fixed inset-0 z-[100] flex min-h-dvh justify-end bg-admin-text/40 backdrop-blur-sm">
      <button type="button" aria-label="Đóng" className="absolute inset-0" onClick={onClose} />
      <aside className="relative flex h-full min-h-dvh w-full max-w-2xl flex-col border-l border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)]">
        <div className="flex items-start justify-between gap-3 border-b border-admin-border bg-admin-surface-muted px-4 py-4 sm:px-6">
          <div className="min-w-0">
            <p className="text-xs font-medium tracking-wide text-admin-text-muted uppercase">
              Chi tiết thanh toán
            </p>
            {detail ? (
              <div className="mt-2 flex flex-wrap items-center gap-2">
                <SupportStatusBadge status={detail.status} kind="payment" />
                <SupportStatusBadge status={detail.reconciliation_status} kind="reconciliation" />
                {detail.payment_method ? (
                  <span className="text-xs text-admin-text-muted">
                    {formatPaymentMethodLabel(detail.payment_method)}
                  </span>
                ) : null}
              </div>
            ) : null}
            <p className="mt-2 font-mono text-sm text-admin-text">{paymentId}</p>
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

        <div className="border-b border-admin-border px-4 py-3 sm:px-6">
          <div className="flex flex-wrap gap-2">
            <button
              type="button"
              className={tabClass(!isTimeline && !isWebhooks)}
              onClick={() => onViewChange?.(PAYMENT_SUPPORT_VIEW_MODES.SUMMARY)}
            >
              Tóm tắt
            </button>
            <button
              type="button"
              className={tabClass(isTimeline)}
              onClick={() => onViewChange?.(PAYMENT_SUPPORT_VIEW_MODES.TIMELINE)}
            >
              Timeline
            </button>
            <button
              type="button"
              className={tabClass(isWebhooks)}
              onClick={() => onViewChange?.(PAYMENT_SUPPORT_VIEW_MODES.WEBHOOKS)}
            >
              Webhook
            </button>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto px-4 py-4 sm:px-6">
          {loading ? <SupportListSkeleton rows={4} /> : null}
          {status === "forbidden" ? <SupportForbiddenState message={errorMessage} /> : null}
          {status === "unavailable" ? <SupportUnavailableState message={errorMessage} /> : null}
          {status === "error" ? (
            <SupportRetryPanel message={errorMessage} onRetry={onRetry} />
          ) : null}
          {status === "ready" && detail ? (
            <PaymentSupportDetailPanelView
              detail={detail}
              orderId={orderId}
              canReadPayment={canReadPayment}
              formatDateTime={formatDateTime}
              formatVndPrice={formatVndPrice}
              onNavigateToOrder={onNavigateToOrder}
              onNavigateToWebhookLogs={onNavigateToWebhookLogs}
              viewMode={paymentView}
            />
          ) : null}
        </div>
      </aside>
    </div>
  );
}
