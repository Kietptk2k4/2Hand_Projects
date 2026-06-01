import { formatReviewDate } from "../utils/formatReviewDate";
import { ProductReviewMediaStrip } from "./ProductReviewMediaStrip";
import { ProductReviewSellerReply } from "./ProductReviewSellerReply";
import { StarRating } from "./StarRating";

export function ProductReviewCard({ review, onComingSoon }) {
  if (!review) return null;

  return (
    <article className="rounded-xl border border-outline-variant bg-surface-container-lowest p-5 shadow-sm">
      <div className="flex items-start gap-3">
        <div
          className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-surface-container-high text-on-surface-variant"
          aria-hidden="true"
        >
          <span className="material-symbols-outlined">person</span>
        </div>
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <p className="text-sm font-semibold text-on-surface">Người mua</p>
            <time className="text-xs text-on-surface-variant" dateTime={review.createdAt}>
              {formatReviewDate(review.createdAt)}
            </time>
          </div>
          <div className="mt-1">
            <StarRating rating={review.rating} />
          </div>
        </div>
      </div>

      {review.comment ? (
        <p className="mt-4 text-sm leading-relaxed text-on-surface">{review.comment}</p>
      ) : null}

      <ProductReviewMediaStrip media={review.media} />
      <ProductReviewSellerReply sellerReply={review.sellerReply} />

      <div className="mt-4 flex gap-4 border-t border-outline-variant pt-3">
        <button
          type="button"
          onClick={onComingSoon}
          className="text-xs text-on-surface-variant transition-colors hover:text-primary"
        >
          Hữu ích
        </button>
        <button
          type="button"
          onClick={onComingSoon}
          className="text-xs text-on-surface-variant transition-colors hover:text-primary"
        >
          Báo cáo
        </button>
      </div>
    </article>
  );
}
