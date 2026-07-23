import { ORDER_SUPPORT_VIEW_MODES } from "../constants/orderSupportListConstants.js";
import { formatPaymentMethodLabel } from "../utils/orderSupportDisplayUtils.js";
import { OrderSupportDetailPanelView } from "./OrderSupportDetailPanelView.jsx";
import { SupportListSkeleton } from "./ui/SupportListSkeleton.jsx";
import { SupportRetryPanel } from "./ui/SupportRetryPanel.jsx";
import { SupportForbiddenState } from "./SupportForbiddenState.jsx";
import { SupportUnavailableState } from "./SupportUnavailableState.jsx";
import { SupportStatusBadge } from "./SupportStatusBadge.jsx";

export function OrderSupportDrawerView({
  open,
  orderId,
  detail,
  loading,
  errorMessage,
  status,
  orderView,
  canReadOrder,
  formatDateTime,
  formatVndPrice,
  onClose,
  onViewChange,
  onRetry,
  onNavigateToPayment,
  onNavigateToShipment,
}) {
  if (!open || !orderId) return null;

  const tabClass = (active) =>
    [
      "min-h-11 rounded-lg px-3 py-2 text-sm font-medium transition-colors",
      active
        ? "bg-admin-accent-soft text-admin-accent-strong"
        : "text-admin-text-secondary hover:bg-admin-surface-muted",
    ].join(" ");

  const isItems = orderView === ORDER_SUPPORT_VIEW_MODES.ITEMS;
  const isTimeline = orderView === ORDER_SUPPORT_VIEW_MODES.TIMELINE;

  return (
    <div className="fixed inset-0 z-[100] flex min-h-dvh justify-end bg-admin-text/40 backdrop-blur-sm">
      <button type="button" aria-label="Đóng" className="absolute inset-0" onClick={onClose} />
      <aside className="relative flex h-full min-h-dvh w-full max-w-2xl flex-col border-l border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)]">
        <div className="flex items-start justify-between gap-3 border-b border-admin-border bg-admin-surface-muted px-4 py-4 sm:px-6">
          <div className="min-w-0">
            <p className="text-xs font-medium tracking-wide text-admin-text-muted uppercase">
              Chi tiết đơn hàng
            </p>
            {detail ? (
              <div className="mt-2 flex flex-wrap items-center gap-2">
                <SupportStatusBadge status={detail.order_status} />
                <SupportStatusBadge status={detail.order_payment_status} kind="payment" />
                {detail.payment_method ? (
                  <span className="text-xs text-admin-text-muted">
                    {formatPaymentMethodLabel(detail.payment_method)}
                  </span>
                ) : null}
              </div>
            ) : null}
            <p className="mt-2 font-mono text-sm text-admin-text">{orderId}</p>
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
              className={tabClass(!isItems && !isTimeline)}
              onClick={() => onViewChange?.(ORDER_SUPPORT_VIEW_MODES.SUMMARY)}
            >
              Tóm tắt
            </button>
            <button
              type="button"
              className={tabClass(isItems)}
              onClick={() => onViewChange?.(ORDER_SUPPORT_VIEW_MODES.ITEMS)}
            >
              Sản phẩm
            </button>
            <button
              type="button"
              className={tabClass(isTimeline)}
              onClick={() => onViewChange?.(ORDER_SUPPORT_VIEW_MODES.TIMELINE)}
            >
              Timeline
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
            <OrderSupportDetailPanelView
              detail={detail}
              canReadOrder={canReadOrder}
              formatDateTime={formatDateTime}
              formatVndPrice={formatVndPrice}
              onNavigateToPayment={onNavigateToPayment}
              onNavigateToShipment={onNavigateToShipment}
              viewMode={orderView}
            />
          ) : null}
        </div>
      </aside>
    </div>
  );
}
