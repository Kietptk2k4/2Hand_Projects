import { useEffect, useState } from "react";
import { AdminFilterButton } from "../../components/ui";
import { REASON_MAX_LENGTH, RESTORE_WARNING } from "../constants/socialModerationConstants.js";

export function SocialRestoreDialog({
  open,
  targetLabel,
  targetId,
  isSubmitting,
  submitError,
  onClose,
  onSubmit,
}) {
  const [reason, setReason] = useState("");

  useEffect(() => {
    if (!open) return;
    setReason("");
  }, [open, targetId]);

  if (!open || !targetId) return null;

  const trimmedReason = reason.trim();

  return (
    <div
      className="fixed inset-0 z-50 flex items-end justify-center bg-admin-text/40 p-0 sm:items-center sm:p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="social-restore-dialog-title"
    >
      <div className="flex max-h-[90dvh] w-full max-w-lg flex-col overflow-hidden rounded-t-2xl border border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)] sm:rounded-xl">
        <div className="border-b border-admin-border-subtle px-4 py-4 sm:px-6">
          <h2 id="social-restore-dialog-title" className="text-lg font-semibold text-admin-text">
            Khôi phục {targetLabel}
          </h2>
          <p className="mt-1 break-all font-mono text-xs text-admin-text-muted">{targetId}</p>
        </div>

        <div className="flex-1 space-y-4 overflow-y-auto px-4 py-4 sm:px-6">
          <p className="rounded-lg border border-admin-warning/30 bg-admin-warning-soft px-3 py-2 text-sm text-admin-text">
            {RESTORE_WARNING}
          </p>

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
            onClick={() => onSubmit?.({ reason: trimmedReason })}
          >
            {isSubmitting ? "Đang xử lý…" : "Khôi phục"}
          </AdminFilterButton>
        </div>
      </div>
    </div>
  );
}
