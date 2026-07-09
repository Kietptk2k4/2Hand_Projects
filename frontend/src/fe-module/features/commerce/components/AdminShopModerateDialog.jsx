import { useEffect, useMemo, useState } from "react";
import { AdminFilterButton } from "../../auth/admin/components/ui";
import {
  ACTION_DESCRIPTIONS,
  ACTION_LABELS,
  MODERATION_ACTIONS,
  REASON_MAX_LENGTH,
  RESTORE_WARNING,
  getAllowedActionsForStatus,
} from "../constants/adminShopModerationConstants";
import { formatShortShopId } from "../utils/formatShortShopId";
import { AdminShopStatusBadge } from "./AdminShopStatusBadge";

export function AdminShopModerateDialog({
  open,
  shop,
  isSubmitting,
  submitError,
  onClose,
  onSubmit,
}) {
  const allowedActions = useMemo(
    () => (shop ? getAllowedActionsForStatus(shop.status) : []),
    [shop],
  );

  const [action, setAction] = useState("");
  const [reason, setReason] = useState("");

  useEffect(() => {
    if (!open || !shop) return;
    const first = getAllowedActionsForStatus(shop.status)[0] || "";
    setAction(first);
    setReason("");
  }, [open, shop?.shopId, shop?.status]);

  if (!open || !shop) return null;

  const trimmedReason = reason.trim();
  const showRestoreWarning = action === MODERATION_ACTIONS.RESTORE;

  return (
    <div
      className="fixed inset-0 z-50 flex items-end justify-center bg-admin-text/40 p-0 sm:items-center sm:p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="moderate-shop-title"
    >
      <div className="flex max-h-[90dvh] w-full max-w-lg flex-col overflow-hidden rounded-t-2xl border border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)] sm:rounded-xl">
        <div className="border-b border-admin-border-subtle px-4 py-4 sm:px-6">
          <h2 id="moderate-shop-title" className="text-lg font-semibold text-admin-text">
            Kiểm duyệt shop
          </h2>
          <p className="mt-1 text-sm text-admin-text-secondary">{shop.shopName}</p>
          <div className="mt-2 flex flex-wrap items-center gap-2 text-xs text-admin-text-muted">
            <span className="font-mono">{formatShortShopId(shop.shopId)}</span>
            <AdminShopStatusBadge status={shop.status} />
          </div>
        </div>

        <div className="flex-1 space-y-4 overflow-y-auto px-4 py-4 sm:px-6">
          <label className="block">
            <span className="text-sm font-medium text-admin-text">Hành động</span>
            <select
              value={action}
              onChange={(event) => setAction(event.target.value)}
              disabled={isSubmitting || allowedActions.length === 0}
              className="mt-1 w-full min-h-11 rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-sm text-admin-text focus:border-admin-accent focus:outline-none focus:ring-2 focus:ring-admin-accent-soft disabled:opacity-50"
            >
              {allowedActions.map((value) => (
                <option key={value} value={value}>
                  {ACTION_LABELS[value]}
                </option>
              ))}
            </select>
            {action ? (
              <p className="mt-1 text-sm text-admin-text-secondary">{ACTION_DESCRIPTIONS[action]}</p>
            ) : null}
          </label>

          {showRestoreWarning ? (
            <p className="rounded-lg border border-admin-warning/30 bg-admin-warning-soft px-3 py-2 text-sm text-admin-text">
              {RESTORE_WARNING}
            </p>
          ) : null}

          <label className="block">
            <span className="text-sm font-medium text-admin-text">
              Lý do <span className="text-admin-danger">*</span>
            </span>
            <textarea
              value={reason}
              onChange={(event) => setReason(event.target.value)}
              rows={4}
              maxLength={REASON_MAX_LENGTH}
              disabled={isSubmitting}
              placeholder="Mô tả lý do kiểm duyệt…"
              className="mt-1 w-full resize-y rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-sm text-admin-text focus:border-admin-accent focus:outline-none focus:ring-2 focus:ring-admin-accent-soft disabled:opacity-50"
            />
          </label>
          <p className="text-right text-xs text-admin-text-muted">
            {trimmedReason.length}/{REASON_MAX_LENGTH}
          </p>

          {submitError ? <p className="text-sm text-admin-danger">{submitError}</p> : null}
        </div>

        <div className="flex flex-col-reverse gap-2 border-t border-admin-border-subtle px-4 py-4 sm:flex-row sm:justify-end sm:px-6">
          <AdminFilterButton type="button" variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Hủy
          </AdminFilterButton>
          <AdminFilterButton
            type="button"
            variant="primary"
            disabled={isSubmitting || !trimmedReason || !action}
            onClick={() => onSubmit?.({ action, reason: trimmedReason })}
          >
            {isSubmitting ? "Đang xử lý…" : "Xác nhận"}
          </AdminFilterButton>
        </div>
      </div>
    </div>
  );
}
