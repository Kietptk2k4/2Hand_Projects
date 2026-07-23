import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { formatVndPrice } from "../../../../social/utils/formatPrice";
import { AdminFilterButton } from "../../components/ui";
import { PayoutStatusBadge } from "./ui/PayoutStatusBadge.jsx";
import { buildPayoutTimeline } from "../utils/payoutQueueHelpers.js";
import { truncateSellerId } from "../utils/topSellersHelpers.js";

function Timeline({ item }) {
  const steps = buildPayoutTimeline(item);
  if (!steps.length) {
    return <p className="text-sm text-admin-text-muted">Chưa có mốc thời gian.</p>;
  }

  return (
    <ol className="space-y-3">
      {steps.map((step) => (
        <li key={step.key} className="flex gap-3">
          <span className="mt-1.5 h-2 w-2 shrink-0 rounded-full bg-admin-accent" aria-hidden="true" />
          <div>
            <p className="text-sm font-medium text-admin-text">{step.label}</p>
            <p className="text-xs text-admin-text-secondary">{formatDateTime(step.at)}</p>
          </div>
        </li>
      ))}
    </ol>
  );
}

export function PayoutRequestDetailDrawer({
  open,
  item,
  onClose,
  onSellerDetail,
  onAction,
  actionId,
}) {
  if (!open || !item) return null;

  return (
    <div className="fixed inset-0 z-[100] flex min-h-dvh justify-end bg-admin-text/40 backdrop-blur-sm">
      <button type="button" aria-label="Đóng" className="absolute inset-0" onClick={onClose} />
      <aside className="relative flex h-full min-h-dvh w-full max-w-lg flex-col border-l border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)]">
        <div className="flex items-start justify-between gap-3 border-b border-admin-border bg-admin-surface-muted px-4 py-4 sm:px-6">
          <div className="min-w-0">
            <p className="text-xs font-medium tracking-wide text-admin-text-muted uppercase">
              Chi tiết yêu cầu rút
            </p>
            <p className="mt-1 text-lg font-semibold tabular-nums text-admin-text">
              {formatVndPrice(item.amount)}
            </p>
            <div className="mt-2">
              <PayoutStatusBadge status={item.status} />
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="inline-flex h-10 w-10 items-center justify-center rounded-lg border border-admin-border text-admin-text-secondary transition-colors hover:bg-admin-surface focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
            aria-label="Đóng panel"
          >
            <span className="material-symbols-outlined text-xl" aria-hidden="true">
              close
            </span>
          </button>
        </div>

        <div className="flex-1 space-y-6 overflow-y-auto px-4 py-4 sm:px-6">
          <section>
            <h3 className="text-sm font-semibold text-admin-text">Seller</h3>
            <p className="mt-2 font-mono text-xs text-admin-text-secondary" title={item.sellerId}>
              {truncateSellerId(item.sellerId, 10, 6)}
            </p>
            <button
              type="button"
              onClick={() => onSellerDetail?.(item.sellerId)}
              className="mt-2 text-sm font-medium text-admin-accent hover:text-admin-accent-strong"
            >
              Mở chi tiết tài chính seller
            </button>
          </section>

          <section>
            <h3 className="text-sm font-semibold text-admin-text">Tài khoản nhận</h3>
            <p className="mt-2 text-sm text-admin-text">{item.bankName || "—"}</p>
            <p className="text-sm text-admin-text-secondary">{item.bankAccountName || "—"}</p>
            <p className="font-mono text-xs text-admin-text-muted">{item.bankAccountNumber || "—"}</p>
          </section>

          <section>
            <h3 className="text-sm font-semibold text-admin-text">Timeline</h3>
            <div className="mt-3">
              <Timeline item={item} />
            </div>
          </section>

          {item.adminNote ? (
            <section>
              <h3 className="text-sm font-semibold text-admin-text">Ghi chú admin</h3>
              <p className="mt-2 text-sm text-admin-text-secondary">{item.adminNote}</p>
            </section>
          ) : null}

          {item.bankTransferRef ? (
            <section>
              <h3 className="text-sm font-semibold text-admin-text">Mã tham chiếu CK</h3>
              <p className="mt-2 font-mono text-sm text-admin-text">{item.bankTransferRef}</p>
            </section>
          ) : null}
        </div>

        <div className="flex flex-col gap-2 border-t border-admin-border bg-admin-surface-muted px-4 py-4 sm:flex-row sm:justify-end sm:px-6">
          {item.status === "REQUESTED" ? (
            <>
              <AdminFilterButton
                type="button"
                variant="secondary"
                className="border-admin-danger/30 text-admin-danger hover:bg-admin-danger-soft"
                disabled={actionId === item.id}
                onClick={() => onAction?.(item, "reject")}
              >
                Từ chối
              </AdminFilterButton>
              <AdminFilterButton
                type="button"
                variant="primary"
                disabled={actionId === item.id}
                onClick={() => onAction?.(item, "approve")}
              >
                Duyệt
              </AdminFilterButton>
            </>
          ) : null}
          {item.status === "APPROVED" ? (
            <AdminFilterButton
              type="button"
              variant="primary"
              disabled={actionId === item.id}
              onClick={() => onAction?.(item, "mark-paid")}
            >
              Ghi nhận chuyển khoản
            </AdminFilterButton>
          ) : null}
        </div>
      </aside>
    </div>
  );
}
