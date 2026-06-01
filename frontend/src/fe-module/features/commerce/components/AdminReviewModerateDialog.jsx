import { useEffect, useMemo, useState } from "react";
import {
  ACTION_DESCRIPTIONS,
  ACTION_LABELS,
  HIDE_WARNING,
  MODERATION_ACTIONS,
  REASON_MAX_LENGTH,
  RESTORE_WARNING,
  getAllowedActionsForReviewStatus,
  getDefaultActionForReview,
} from "../constants/adminReviewModerationConstants";
import { formatShortOrderId } from "../utils/formatOrderDate";
import { AdminReviewStatusBadge } from "./AdminReviewStatusBadge";
import { StarRating } from "./StarRating";

export function AdminReviewModerateDialog({
  open,
  review,
  isSubmitting,
  submitError,
  onClose,
  onSubmit,
}) {
  const allowedActions = useMemo(
    () => (review ? getAllowedActionsForReviewStatus(review.status) : []),
    [review],
  );

  const [action, setAction] = useState("");
  const [reason, setReason] = useState("");

  useEffect(() => {
    if (!open || !review) return;
    setAction(getDefaultActionForReview(review.status));
    setReason("");
  }, [open, review?.reviewId, review?.status]);

  if (!open || !review) return null;

  const trimmedReason = reason.trim();
  const showHideWarning = action === MODERATION_ACTIONS.HIDE;
  const showRestoreWarning = action === MODERATION_ACTIONS.RESTORE;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-inverse-surface/40 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="moderate-review-title"
    >
      <div className="flex max-h-[90vh] w-full max-w-lg flex-col overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-lg">
        <div className="border-b border-outline-variant px-6 py-4">
          <h2 id="moderate-review-title" className="text-headline-sm font-semibold text-on-surface">
            Kiểm duyệt đánh giá
          </h2>
          <p className="mt-1 text-body-sm text-on-surface-variant">{review.productTitle}</p>
          <div className="mt-2 flex flex-wrap items-center gap-2 text-label-sm text-on-surface-variant">
            <span>Người mua: {review.buyerDisplayName || "—"}</span>
            <AdminReviewStatusBadge status={review.status} />
          </div>
        </div>

        <div className="flex-1 space-y-4 overflow-y-auto px-6 py-4">
          <div className="rounded-lg border border-outline-variant bg-surface-container-low/50 p-3">
            <StarRating rating={review.rating ?? 0} />
            <p className="mt-2 text-body-sm text-on-surface">{review.comment?.trim() || "—"}</p>
            <dl className="mt-3 grid gap-1 text-label-sm text-on-surface-variant">
              <div className="flex flex-wrap gap-2">
                <dt className="font-medium">review_id:</dt>
                <dd className="font-mono break-all">{review.reviewId}</dd>
              </div>
              <div className="flex flex-wrap gap-2">
                <dt className="font-medium">order_item_id:</dt>
                <dd className="font-mono">{formatShortOrderId(review.orderItemId)}</dd>
              </div>
            </dl>
          </div>

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

          {showHideWarning ? (
            <p className="rounded-lg border border-error/20 bg-error-container/30 px-3 py-2 text-body-sm text-on-error-container">
              {HIDE_WARNING}
            </p>
          ) : null}

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
              placeholder="Mô tả lý do kiểm duyệt..."
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
