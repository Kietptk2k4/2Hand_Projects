import { useCallback, useEffect, useMemo, useState } from "react";
import { StarRatingPicker } from "./StarRatingPicker";
import { ReviewMediaPicker } from "./ReviewMediaPicker";
import { MAX_REVIEW_MEDIA } from "../constants/reviewMediaConstants";
import {
  MAX_COMMENT_LENGTH,
  RATING_MAX,
  RATING_MIN,
} from "../constants/productReviewFormConstants";

const textareaClass =
  "w-full rounded-lg border border-outline-variant bg-surface-container-lowest px-3 py-2 text-body-md text-on-surface focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary";

export function ProductReviewForm({
  mode = "create",
  initialRating = 0,
  initialComment = "",
  existingMediaCount = 0,
  existingMedia = [],
  maxMedia = MAX_REVIEW_MEDIA,
  reviewStatus = "VISIBLE",
  onSubmit,
  onCancel,
  isSubmitting = false,
  isUploading = false,
  apiError = "",
}) {
  const [rating, setRating] = useState(initialRating);
  const [comment, setComment] = useState(initialComment);
  const [pendingFiles, setPendingFiles] = useState([]);
  const [ratingError, setRatingError] = useState("");
  const [noChangesMessage, setNoChangesMessage] = useState("");

  useEffect(() => {
    setRating(initialRating);
    setComment(initialComment);
  }, [initialRating, initialComment]);

  const isEdit = mode === "edit";
  const isBusy = isSubmitting || isUploading;
  const canUploadMedia = reviewStatus === "VISIBLE";

  const hasTextChanges = useMemo(() => {
    if (!isEdit) return true;
    return rating !== initialRating || comment !== initialComment;
  }, [comment, initialComment, initialRating, isEdit, rating]);

  const hasPendingMedia = pendingFiles.length > 0;
  const hasChanges = hasTextChanges || hasPendingMedia;

  const validate = useCallback(() => {
    if (!rating || rating < RATING_MIN || rating > RATING_MAX) {
      setRatingError("Vui lòng chọn điểm đánh giá từ 1 đến 5 sao.");
      return false;
    }
    setRatingError("");
    return true;
  }, [rating]);

  const handleSubmit = useCallback(
    async (event) => {
      event.preventDefault();
      setNoChangesMessage("");

      if (!validate()) return;

      if (isEdit && !hasChanges) {
        setNoChangesMessage("Không có thay đổi để lưu.");
        return;
      }

      const payload = isEdit
        ? {
            ...(rating !== initialRating ? { rating } : {}),
            ...(comment !== initialComment ? { comment } : {}),
          }
        : { rating, comment: comment.trim() || undefined };

      await onSubmit?.({ payload, pendingFiles });
    },
    [
      comment,
      hasChanges,
      initialComment,
      initialRating,
      isEdit,
      onSubmit,
      pendingFiles,
      rating,
      validate,
    ],
  );

  const submitLabel = isEdit ? "Lưu thay đổi" : "Gửi đánh giá";
  const submitDisabled = isBusy || (isEdit && !hasChanges);

  return (
    <section className="rounded-xl border border-outline-variant bg-surface-container-lowest p-4 shadow-sm md:p-6 lg:p-8">
      <div className="mb-6">
        <h1 className="text-headline-md font-bold text-on-surface">
          {isEdit ? "Sửa đánh giá" : "Viết đánh giá"}
        </h1>
        <p className="mt-1 text-body-sm text-on-surface-variant">
          {isEdit
            ? "Cập nhật đánh giá của bạn để giúp người mua khác."
            : "Chia sẻ trải nghiệm để giúp người mua khác quyết định tốt hơn."}
        </p>
      </div>

      <form onSubmit={handleSubmit} className="flex flex-col gap-6">
        <div>
          <label className="mb-2 block text-label-md font-medium text-on-surface">
            Đánh giá tổng thể <span className="text-error">*</span>
          </label>
          <StarRatingPicker value={rating} onChange={setRating} disabled={isBusy} />
          {ratingError ? <p className="mt-1 text-xs text-error">{ratingError}</p> : null}
        </div>

        <div>
          <label htmlFor="review-comment" className="mb-2 block text-label-md font-medium text-on-surface">
            Nhận xét chi tiết
          </label>
          <textarea
            id="review-comment"
            rows={5}
            value={comment}
            onChange={(e) => setComment(e.target.value.slice(0, MAX_COMMENT_LENGTH))}
            disabled={isBusy}
            placeholder="Bạn thích hoặc chưa hài lòng điều gì? Sản phẩm có đúng mô tả không?"
            className={textareaClass}
          />
          <div className="mt-1 flex justify-between text-body-sm text-on-surface-variant">
            <span>Gợi ý: mô tả càng cụ thể càng hữu ích (không bắt buộc).</span>
            <span>
              {comment.length} / {MAX_COMMENT_LENGTH}
            </span>
          </div>
        </div>

        <div>
          <label className="mb-2 block text-label-md font-medium text-on-surface">
            Ảnh / video đính kèm
          </label>
          <ReviewMediaPicker
            existingMedia={existingMedia}
            existingMediaCount={existingMediaCount}
            maxMedia={maxMedia}
            disabled={isBusy || !canUploadMedia}
            onFilesChange={setPendingFiles}
          />
          {!canUploadMedia ? (
            <p className="mt-2 text-body-sm text-error">
              Đánh giá đã bị ẩn — không thể thêm ảnh/video.
            </p>
          ) : null}
        </div>

        {apiError ? (
          <p className="rounded-lg border border-error/30 bg-error-container/40 p-3 text-sm text-on-error-container">
            {apiError}
          </p>
        ) : null}

        {noChangesMessage ? (
          <p className="rounded-lg border border-outline-variant bg-surface-container-low p-3 text-sm text-on-surface-variant">
            {noChangesMessage}
          </p>
        ) : null}

        <div className="flex flex-wrap justify-end gap-3 border-t border-outline-variant pt-4">
          <button
            type="button"
            onClick={onCancel}
            disabled={isBusy}
            className="rounded-lg px-4 py-2.5 text-label-md text-on-surface-variant hover:bg-surface-container-low disabled:opacity-50"
          >
            Hủy
          </button>
          <button
            type="submit"
            disabled={submitDisabled}
            className="flex min-w-[140px] items-center justify-center gap-2 rounded-lg bg-primary px-4 py-2.5 text-label-md font-medium text-on-primary hover:bg-[#0050cb] disabled:opacity-50"
          >
            {isBusy ? (
              <span className="h-5 w-5 animate-spin rounded-full border-2 border-on-primary border-t-transparent" />
            ) : (
              submitLabel
            )}
          </button>
        </div>
      </form>
    </section>
  );
}
