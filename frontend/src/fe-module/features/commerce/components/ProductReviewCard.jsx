import { formatReviewDate } from "../utils/formatReviewDate";
import { ProductReviewMediaStrip } from "./ProductReviewMediaStrip";
import { ProductReviewSellerReply } from "./ProductReviewSellerReply";
import { ReviewAuthorLink } from "./ReviewAuthorLink";
import { StarRating } from "./StarRating";

export function ProductReviewCard({ review, productName, shop, onComingSoon }) {
  if (!review) return null;

  return (
    <article className="rounded-xl border border-outline-variant bg-surface-container-lowest p-5 shadow-sm">
      <div className="min-w-0">
        <div className="flex flex-wrap items-start justify-between gap-2">
          <ReviewAuthorLink
            buyerId={review.buyerId}
            displayName={review.buyerDisplayName}
            avatarUrl={review.buyerAvatarUrl}
          />
          <time className="text-xs text-on-surface-variant" dateTime={review.createdAt}>
            {formatReviewDate(review.createdAt)}
          </time>
        </div>
        {productName ? (
          <p className="mt-1 line-clamp-1 text-xs text-on-surface-variant">
            Sản phẩm: {productName}
          </p>
        ) : null}
        <div className="mt-1">
          <StarRating rating={review.rating} />
        </div>
      </div>

      {review.comment ? (
        <p className="mt-4 text-sm leading-relaxed text-on-surface">{review.comment}</p>
      ) : null}

      <ProductReviewMediaStrip media={review.media} />
      <ProductReviewSellerReply sellerReply={review.sellerReply} shop={shop} />

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
