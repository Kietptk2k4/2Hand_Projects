import { REVIEW_STATUS_LABELS } from "../constants/sellerShopReviewsConstants";
import { formatShortOrderItemId } from "../utils/formatShortOrderItemId";
import { formatReviewDate } from "../utils/formatReviewDate";
import { ProductReviewMediaStrip } from "./ProductReviewMediaStrip";
import { ProductReviewSellerReply } from "./ProductReviewSellerReply";
import { StarRating } from "./StarRating";

function productPlaceholderSeed(name) {
  return encodeURIComponent(String(name || "product").slice(0, 24));
}

export function SellerShopReviewCard({ review, onReply, disabled }) {
  if (!review) return null;

  const isHidden = review.status === "HIDDEN";
  const hasReply = Boolean(review.sellerReply);
  const canReply = review.status === "VISIBLE" && !hasReply;

  const statusDotClass = isHidden ? "bg-outline" : "bg-emerald-500";
  const cardClass = isHidden
    ? "bg-error-container/10 opacity-90"
    : "hover:bg-surface-container-low/50";

  return (
    <article
      className={[
        "flex flex-col gap-6 border-b border-outline-variant p-6 transition-colors lg:flex-row",
        cardClass,
      ].join(" ")}
    >
      <div className="flex w-full min-w-[200px] gap-4 lg:w-1/4">
        <div className="flex h-16 w-16 shrink-0 items-center justify-center overflow-hidden rounded-md border border-outline-variant bg-surface-container-high">
          <img
            src={`https://picsum.photos/seed/${productPlaceholderSeed(review.productNameSnapshot)}/64/64`}
            alt=""
            className="h-full w-full object-cover"
          />
        </div>
        <div className="min-w-0">
          <h3 className="line-clamp-2 text-label-md font-medium text-on-surface">
            {review.productNameSnapshot}
          </h3>
          <p className="mt-1 font-mono text-body-sm text-on-surface-variant">
            {formatShortOrderItemId(review.orderItemId)}
          </p>
        </div>
      </div>

      <div className="flex w-full flex-col gap-2 lg:w-2/4">
        <div className="flex items-center justify-between gap-2">
          <StarRating rating={review.rating} />
          <time className="text-body-sm text-on-surface-variant" dateTime={review.createdAt}>
            {formatReviewDate(review.createdAt)}
          </time>
        </div>

        {review.comment ? (
          <p className="text-body-sm leading-relaxed text-on-surface">{review.comment}</p>
        ) : (
          <p className="text-body-sm italic text-on-surface-variant">Không có bình luận.</p>
        )}

        <ProductReviewMediaStrip media={review.media} />
        <ProductReviewSellerReply sellerReply={review.sellerReply} />
      </div>

      <div className="flex w-full flex-col items-start justify-between gap-4 border-t border-outline-variant pt-4 lg:w-1/4 lg:items-end lg:border-t-0 lg:pt-0">
        <div className="flex items-center gap-2">
          <span className={`h-2 w-2 rounded-full ${statusDotClass}`} aria-hidden="true" />
          <span className="text-label-sm uppercase text-on-surface-variant">
            {REVIEW_STATUS_LABELS[review.status] || review.status}
          </span>
        </div>

        {canReply ? (
          <button
            type="button"
            disabled={disabled}
            onClick={() => onReply?.(review)}
            className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-primary-container px-4 py-2 text-label-md font-medium text-on-primary-container transition-colors hover:bg-primary hover:text-on-primary disabled:opacity-50 lg:w-auto"
          >
            <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
              reply
            </span>
            Phản hồi
          </button>
        ) : hasReply ? (
          <p
            className="text-center text-label-sm text-on-surface-variant lg:text-right"
            title="Mỗi đánh giá chỉ phản hồi một lần"
          >
            Đã phản hồi
          </p>
        ) : (
          <p
            className="text-center text-label-sm text-on-surface-variant lg:text-right"
            title="Chỉ phản hồi đánh giá đang hiển thị"
          >
            {isHidden ? "Không thể phản hồi (đã ẩn)" : "—"}
          </p>
        )}
      </div>
    </article>
  );
}
