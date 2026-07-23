import { SHIPMENT_SUPPORT_VIEW_MODES } from "../constants/shipmentSupportListConstants.js";
import { formatCarrierLabel } from "../utils/orderSupportDisplayUtils.js";
import { ShipmentSupportDetailPanelView } from "./ShipmentSupportDetailPanelView.jsx";
import { SupportListSkeleton } from "./ui/SupportListSkeleton.jsx";
import { SupportRetryPanel } from "./ui/SupportRetryPanel.jsx";
import { SupportForbiddenState } from "./SupportForbiddenState.jsx";
import { SupportUnavailableState } from "./SupportUnavailableState.jsx";
import { SupportStatusBadge } from "./SupportStatusBadge.jsx";

export function ShipmentSupportDrawerView({
  open,
  shipmentId,
  detail,
  loading,
  errorMessage,
  status,
  shipmentView,
  canReadShipment,
  canWriteShipment,
  canForceWriteShipment,
  formatDateTime,
  formatVndPrice,
  onClose,
  onViewChange,
  onRetry,
  onOverrideSuccess,
  onNotify,
  onNavigateToOrder,
  onNavigateToWebhook,
}) {
  if (!open || !shipmentId) return null;

  const tabClass = (active) =>
    [
      "min-h-11 rounded-lg px-3 py-2 text-sm font-medium transition-colors",
      active
        ? "bg-admin-accent-soft text-admin-accent-strong"
        : "text-admin-text-secondary hover:bg-admin-surface-muted",
    ].join(" ");

  const isTimeline = shipmentView === SHIPMENT_SUPPORT_VIEW_MODES.TIMELINE;
  const isWebhooks = shipmentView === SHIPMENT_SUPPORT_VIEW_MODES.WEBHOOKS;
  const isOverride = shipmentView === SHIPMENT_SUPPORT_VIEW_MODES.OVERRIDE;
  const isSummary = !isTimeline && !isWebhooks && !isOverride;

  return (
    <div className="fixed inset-0 z-[100] flex min-h-dvh justify-end bg-admin-text/40 backdrop-blur-sm">
      <button type="button" aria-label="Đóng" className="absolute inset-0" onClick={onClose} />
      <aside className="relative flex h-full min-h-dvh w-full max-w-2xl flex-col border-l border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)]">
        <div className="flex items-start justify-between gap-3 border-b border-admin-border bg-admin-surface-muted px-4 py-4 sm:px-6">
          <div className="min-w-0">
            <p className="text-xs font-medium tracking-wide text-admin-text-muted uppercase">
              Chi tiết vận chuyển
            </p>
            {detail ? (
              <div className="mt-2 flex flex-wrap items-center gap-2">
                <SupportStatusBadge status={detail.internal_status} kind="shipment" />
                {detail.carrier ? (
                  <span className="text-xs text-admin-text-muted">
                    {formatCarrierLabel(detail.carrier)}
                  </span>
                ) : null}
              </div>
            ) : null}
            <p className="mt-2 font-mono text-sm text-admin-text">{shipmentId}</p>
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
              className={tabClass(isSummary)}
              onClick={() => onViewChange?.(SHIPMENT_SUPPORT_VIEW_MODES.SUMMARY)}
            >
              Tóm tắt
            </button>
            <button
              type="button"
              className={tabClass(isTimeline)}
              onClick={() => onViewChange?.(SHIPMENT_SUPPORT_VIEW_MODES.TIMELINE)}
            >
              Timeline
            </button>
            <button
              type="button"
              className={tabClass(isWebhooks)}
              onClick={() => onViewChange?.(SHIPMENT_SUPPORT_VIEW_MODES.WEBHOOKS)}
            >
              Webhook
            </button>
            {canWriteShipment ? (
              <button
                type="button"
                className={tabClass(isOverride)}
                onClick={() => onViewChange?.(SHIPMENT_SUPPORT_VIEW_MODES.OVERRIDE)}
              >
                Ghi đè
              </button>
            ) : null}
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
            <ShipmentSupportDetailPanelView
              detail={detail}
              shipmentId={shipmentId}
              canReadShipment={canReadShipment}
              canWriteShipment={canWriteShipment}
              canForceWriteShipment={canForceWriteShipment}
              formatDateTime={formatDateTime}
              formatVndPrice={formatVndPrice}
              onNavigateToOrder={onNavigateToOrder}
              onNavigateToWebhook={onNavigateToWebhook}
              onOverrideSuccess={onOverrideSuccess}
              onNotify={onNotify}
              viewMode={shipmentView}
            />
          ) : null}
        </div>
      </aside>
    </div>
  );
}
