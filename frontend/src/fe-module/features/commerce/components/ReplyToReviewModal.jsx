import { useEffect, useState } from "react";
import { REPLY_CONTENT_MAX } from "../constants/sellerShopReviewsConstants";
import { StarRating } from "./StarRating";

export function ReplyToReviewModal({
  open,
  review,
  isSubmitting,
  submitError,
  onClose,
  onRequestConfirm,
}) {
  const [content, setContent] = useState("");

  useEffect(() => {
    if (open) setContent("");
  }, [open, review?.reviewId]);

  if (!open || !review) return null;

  const trimmed = content.trim();
  const tooLong = trimmed.length > REPLY_CONTENT_MAX;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-inverse-surface/40 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="reply-review-title"
    >
      <div className="flex max-h-[90vh] w-full max-w-lg flex-col overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-lg">
        <div className="border-b border-outline-variant px-6 py-4">
          <h2 id="reply-review-title" className="text-headline-sm font-semibold text-on-surface">
            Phản hồi đánh giá
          </h2>
          <p className="mt-1 line-clamp-1 text-body-sm text-on-surface-variant">
            {review.productNameSnapshot}
          </p>
          <div className="mt-2">
            <StarRating rating={review.rating} />
          </div>
          {review.comment ? (
            <p className="mt-2 line-clamp-3 rounded-lg bg-surface-container-low p-3 text-body-sm text-on-surface">
              {review.comment}
            </p>
          ) : null}
        </div>

        <div className="flex-1 overflow-y-auto px-6 py-4">
          <label className="block">
            <span className="text-label-sm font-medium text-on-surface">Nội dung phản hồi</span>
            <textarea
              value={content}
              onChange={(e) => setContent(e.target.value)}
              rows={5}
              maxLength={REPLY_CONTENT_MAX}
              placeholder="Cảm ơn bạn đã mua hàng..."
              className="mt-2 w-full resize-y rounded-lg border border-outline-variant px-3 py-2 text-body-sm"
            />
          </label>
          <p className="mt-1 text-right text-label-sm text-on-surface-variant">
            {trimmed.length}/{REPLY_CONTENT_MAX}
          </p>
          {submitError ? <p className="mt-2 text-sm text-error">{submitError}</p> : null}
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
            disabled={isSubmitting || !trimmed || tooLong}
            onClick={() => onRequestConfirm?.(trimmed)}
            className="rounded-lg bg-primary px-4 py-2 text-label-md font-medium text-on-primary disabled:opacity-50"
          >
            Tiếp tục
          </button>
        </div>
      </div>
    </div>
  );
}
