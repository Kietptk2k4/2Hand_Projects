import { AdminFilterButton } from "../../components/ui";
import { REFUND_SUPPORT_VIEW_MODES } from "../constants/refundSupportListConstants.js";
import { RefundSupportDetailPanelView } from "./RefundSupportDetailPanelView.jsx";
import { SupportForbiddenState } from "./SupportForbiddenState.jsx";
import { SupportUnavailableState } from "./SupportUnavailableState.jsx";
import { SupportListSkeleton } from "./ui/SupportListSkeleton.jsx";
import { SupportRetryPanel } from "./ui/SupportRetryPanel.jsx";
import { SupportStatusBadge } from "./SupportStatusBadge.jsx";

export function RefundSupportDrawerView({
  open,
  refundRequestId,
  detail,
  loading,
  errorMessage,
  status,
  refundView,
  canReadRefund,
  canApproveRefund,
  actionPending,
  formatDateTime,
  formatVndPrice,
  onClose,
  onViewChange,
  onRetry,
  onConfirm,
  onReject,
  onNavigateToOrder,
  onNavigateToPayment,
  onCopied,
}) {
  if (!open || !refundRequestId) return null;

  const tabClass = (active) =>
    [
      "min-h-11 rounded-lg px-3 py-2 text-sm font-medium transition-colors",
      active
        ? "bg-admin-accent-soft text-admin-accent-strong"
        : "text-admin-text-secondary hover:bg-admin-surface-muted",
    ].join(" ");

  const isDetail = refundView === REFUND_SUPPORT_VIEW_MODES.DETAIL;
  const isNote = refundView === REFUND_SUPPORT_VIEW_MODES.NOTE;
  const isSummary = !isDetail && !isNote;
  const showActions = detail?.status === "REQUESTED" && canApproveRefund;

  return (
    <div className="fixed inset-0 z-[100] flex min-h-dvh justify-end bg-admin-text/40 backdrop-blur-sm">
      <button type="button" aria-label="Đóng" className="absolute inset-0" onClick={onClose} />
      <aside className="relative flex h-full min-h-dvh w-full max-w-2xl flex-col border-l border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)]">
        <div className="flex items-start justify-between gap-3 border-b border-admin-border bg-admin-surface-muted px-4 py-4 sm:px-6">
          <div className="min-w-0">
            <p className="text-xs font-medium tracking-wide text-admin-text-muted uppercase">
              Duyệt hoàn tiền
            </p>
            {detail ? (
              <div className="mt-2">
                <SupportStatusBadge status={detail.status} kind="refund" />
              </div>
            ) : null}
            <p className="mt-2 font-mono text-sm text-admin-text">{refundRequestId}</p>
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
              onClick={() => onViewChange?.(REFUND_SUPPORT_VIEW_MODES.SUMMARY)}
            >
              Tóm tắt
            </button>
            <button
              type="button"
              className={tabClass(isDetail)}
              onClick={() => onViewChange?.(REFUND_SUPPORT_VIEW_MODES.DETAIL)}
            >
              Chi tiết đơn
            </button>
            <button
              type="button"
              className={tabClass(isNote)}
              onClick={() => onViewChange?.(REFUND_SUPPORT_VIEW_MODES.NOTE)}
            >
              Ghi chú admin
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
            <RefundSupportDetailPanelView
              detail={detail}
              refundRequestId={refundRequestId}
              viewMode={refundView}
              formatDateTime={formatDateTime}
              formatVndPrice={formatVndPrice}
              onNavigateToOrder={onNavigateToOrder}
              onNavigateToPayment={onNavigateToPayment}
              onCopied={onCopied}
            />
          ) : null}
        </div>

        {showActions ? (
          <div className="flex flex-col-reverse gap-2 border-t border-admin-border bg-admin-surface-muted px-4 py-4 sm:flex-row sm:justify-end sm:px-6">
            <AdminFilterButton
              type="button"
              variant="secondary"
              disabled={actionPending}
              onClick={() => onReject?.()}
            >
              Từ chối
            </AdminFilterButton>
            <AdminFilterButton
              type="button"
              variant="primary"
              disabled={actionPending}
              onClick={() => onConfirm?.()}
            >
              Xác nhận đã hoàn tiền
            </AdminFilterButton>
          </div>
        ) : null}
      </aside>
    </div>
  );
}
