import { useEffect, useState } from "react";
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
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-inverse-surface/40 p-4" role="dialog" aria-modal="true">
      <div className="flex max-h-[90vh] w-full max-w-lg flex-col overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-lg">
        <div className="border-b border-outline-variant px-6 py-4">
          <h2 className="text-headline-sm font-semibold text-on-surface">Kiem duyet {targetLabel}</h2>
          <p className="mt-1 break-all font-mono text-xs text-on-surface-variant">{targetId}</p>
        </div>

        <div className="flex-1 space-y-4 overflow-y-auto px-6 py-4">
          <fieldset className="space-y-2">
            <legend className="text-label-sm font-medium text-on-surface">Hanh dong</legend>
            {[MODERATION_ACTIONS.HIDE, MODERATION_ACTIONS.REMOVE].map((value) => (
              <label key={value} className="flex cursor-pointer items-center gap-2 text-body-sm">
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
            <p className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-body-sm text-amber-950">{HIDE_WARNING}</p>
          ) : (
            <p className="rounded-lg border border-error/20 bg-error-container/30 px-3 py-2 text-body-sm text-on-error-container">{REMOVE_WARNING}</p>
          )}

          <label className="block">
            <span className="text-label-sm font-medium text-on-surface">Ly do <span className="text-error">*</span></span>
            <textarea
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              rows={4}
              maxLength={REASON_MAX_LENGTH}
              disabled={isSubmitting}
              className="mt-1 w-full resize-y rounded-lg border border-outline-variant px-3 py-2 text-body-sm disabled:opacity-50"
            />
          </label>
          <p className="text-right text-label-sm text-on-surface-variant">{trimmedReason.length}/{REASON_MAX_LENGTH}</p>
          {submitError ? <p className="text-sm text-error">{submitError}</p> : null}
        </div>

        <div className="flex justify-end gap-3 border-t border-outline-variant px-6 py-4">
          <button type="button" onClick={onClose} disabled={isSubmitting} className="rounded-lg px-4 py-2 text-label-md text-on-surface-variant hover:bg-surface-container-low disabled:opacity-50">
            Huy
          </button>
          <button
            type="button"
            disabled={isSubmitting || !trimmedReason}
            onClick={() => onSubmit?.({ action, reason: trimmedReason })}
            className="rounded-lg bg-primary px-4 py-2 text-label-md font-medium text-on-primary disabled:opacity-50"
          >
            {isSubmitting ? "Dang xu ly..." : "Xac nhan"}
          </button>
        </div>
      </div>
    </div>
  );
}