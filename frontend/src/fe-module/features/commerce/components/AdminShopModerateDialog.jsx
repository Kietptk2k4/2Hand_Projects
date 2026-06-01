import { useEffect, useMemo, useState } from "react";
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
      className="fixed inset-0 z-50 flex items-center justify-center bg-inverse-surface/40 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="moderate-shop-title"
    >
      <div className="flex max-h-[90vh] w-full max-w-lg flex-col overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-lg">
        <div className="border-b border-outline-variant px-6 py-4">
          <h2 id="moderate-shop-title" className="text-headline-sm font-semibold text-on-surface">
            Moderate shop
          </h2>
          <p className="mt-1 text-body-sm text-on-surface-variant">{shop.shopName}</p>
          <div className="mt-2 flex flex-wrap items-center gap-2 text-label-sm text-on-surface-variant">
            <span className="font-mono">{formatShortShopId(shop.shopId)}</span>
            <AdminShopStatusBadge status={shop.status} />
          </div>
        </div>

        <div className="flex-1 space-y-4 overflow-y-auto px-6 py-4">
          <label className="block">
            <span className="text-label-sm font-medium text-on-surface">Hành động</span>
            <select
              value={action}
              onChange={(e) => setAction(e.target.value)}
              disabled={isSubmitting || allowedActions.length === 0}
              className="mt-1 w-full rounded-lg border border-outline-variant px-3 py-2 text-body-sm disabled:opacity-50"
            >
              {allowedActions.map((value) => (
                <option key={value} value={value}>
                  {ACTION_LABELS[value]}
                </option>
              ))}
            </select>
            {action ? (
              <p className="mt-1 text-body-sm text-on-surface-variant">
                {ACTION_DESCRIPTIONS[action]}
              </p>
            ) : null}
          </label>

          {showRestoreWarning ? (
            <p className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-body-sm text-amber-950">
              {RESTORE_WARNING}
            </p>
          ) : null}

          <label className="block">
            <span className="text-label-sm font-medium text-on-surface">
              Lý do <span className="text-error">*</span>
            </span>
            <textarea
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              rows={4}
              maxLength={REASON_MAX_LENGTH}
              disabled={isSubmitting}
              placeholder="Mô tả lý do moderation..."
              className="mt-1 w-full resize-y rounded-lg border border-outline-variant px-3 py-2 text-body-sm disabled:opacity-50"
            />
          </label>
          <p className="text-right text-label-sm text-on-surface-variant">
            {trimmedReason.length}/{REASON_MAX_LENGTH}
          </p>

          {submitError ? <p className="text-sm text-error">{submitError}</p> : null}
        </div>

        <div className="flex justify-end gap-3 border-t border-outline-variant px-6 py-4">
          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            className="rounded-lg px-4 py-2 text-label-md text-on-surface-variant hover:bg-surface-container-low disabled:opacity-50"
          >
            Hủy
          </button>
          <button
            type="button"
            disabled={isSubmitting || !trimmedReason || !action}
            onClick={() => onSubmit?.({ action, reason: trimmedReason })}
            className="rounded-lg bg-primary px-4 py-2 text-label-md font-medium text-on-primary disabled:opacity-50"
          >
            {isSubmitting ? "Đang xử lý..." : "Xác nhận"}
          </button>
        </div>
      </div>
    </div>
  );
}
