import { useEffect, useState } from "react";
import { AdminFilterButton } from "../../components/ui";
import {
  ACTION_LABELS,
  HIDE_WARNING,
  MODERATION_ACTIONS,
  REASON_MAX_LENGTH,
  REMOVE_WARNING,
} from "../constants/socialModerationConstants.js";

export function SocialModerateDialog({
  open,
  targetLabel,
  targetId,
  isSubmitting,
  submitError,
  onClose,
  onSubmit,
}) {
  const [action, setAction] = useState(MODERATION_ACTIONS.HIDE);
  const [reason, setReason] = useState("");

  useEffect(() => {
    if (!open) return;
    setAction(MODERATION_ACTIONS.HIDE);
    setReason("");
  }, [open, targetId]);

  if (!open || !targetId) return null;

  const trimmedReason = reason.trim();

  return (
    <div
      className="fixed inset-0 z-50 flex items-end justify-center bg-admin-text/40 p-0 sm:items-center sm:p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="social-moderate-dialog-title"
    >
      <div className="flex max-h-[90dvh] w-full max-w-lg flex-col overflow-hidden rounded-t-2xl border border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)] sm:rounded-xl">
        <div className="border-b border-admin-border-subtle px-4 py-4 sm:px-6">
          <h2 id="social-moderate-dialog-title" className="text-lg font-semibold text-admin-text">
            Kiểm duyệt {targetLabel}
          </h2>
          <p className="mt-1 break-all font-mono text-xs text-admin-text-muted">{targetId}</p>
        </div>

        <div className="flex-1 space-y-4 overflow-y-auto px-4 py-4 sm:px-6">
          <fieldset className="space-y-2">
            <legend className="text-sm font-medium text-admin-text">Hành động</legend>
            {[MODERATION_ACTIONS.HIDE, MODERATION_ACTIONS.REMOVE].map((value) => (
              <label key={value} className="flex min-h-11 cursor-pointer items-center gap-2 text-sm">
                <input
                  type="radio"
                  name="social-moderate-action"
                  value={value}
                  checked={action === value}
                  disabled={isSubmitting}
                  onChange={() => setAction(value)}
                />
                {ACTION_LABELS[value]}
              </label>
            ))}
          </fieldset>

          {action === MODERATION_ACTIONS.HIDE ? (
            <p className="rounded-lg border border-admin-warning/30 bg-admin-warning-soft px-3 py-2 text-sm text-admin-text">
              {HIDE_WARNING}
            </p>
          ) : (
            <p className="rounded-lg border border-admin-danger/30 bg-admin-danger-soft px-3 py-2 text-sm text-admin-danger">
              {REMOVE_WARNING}
            </p>
          )}

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
              className="mt-1 w-full min-h-[6rem] resize-y rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-sm text-admin-text outline-none focus:border-admin-accent-border focus:ring-2 focus:ring-admin-accent-soft disabled:opacity-50"
            />
          </label>
          <p className="text-right text-xs text-admin-text-muted">
            {trimmedReason.length}/{REASON_MAX_LENGTH}
          </p>
          {submitError ? <p className="text-sm text-admin-danger">{submitError}</p> : null}
        </div>

        <div className="flex flex-col-reverse gap-2 border-t border-admin-border-subtle px-4 py-4 sm:flex-row sm:justify-end sm:gap-3 sm:px-6">
          <AdminFilterButton type="button" variant="secondary" onClick={onClose} disabled={isSubmitting}>
            Hủy
          </AdminFilterButton>
          <AdminFilterButton
            type="button"
            variant="primary"
            disabled={isSubmitting || !trimmedReason}
            onClick={() => onSubmit?.({ action, reason: trimmedReason })}
          >
            {isSubmitting ? "Đang xử lý…" : "Xác nhận"}
          </AdminFilterButton>
        </div>
      </div>
    </div>
  );
}
