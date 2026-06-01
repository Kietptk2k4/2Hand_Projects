import { ProductReviewCard } from "./ProductReviewCard";
import { StarRating } from "./StarRating";

export function ProductDetailReviewsPreview({
  ratingSummary,
  reviews,
  isLoading,
  isEmpty,
  hasMoreReviews,
  errorMessage,
  onViewAll,
  onRetry,
  onComingSoon,
}) {
  const count = ratingSummary?.ratingCount ?? 0;
  const avg = ratingSummary?.ratingAvg ?? 0;

  return (
    <section
      className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm"
      aria-labelledby="product-reviews-heading"
    >
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h2
            id="product-reviews-heading"
            className="text-headline-sm font-semibold text-on-surface"
          >
            Đánh giá sản phẩm
          </h2>
          {!isLoading && count > 0 ? (
            <div className="mt-2 flex flex-wrap items-center gap-2">
              <span className="text-headline-md font-bold text-on-surface">{avg.toFixed(1)}</span>
              <StarRating rating={avg} />
              <span className="text-sm text-on-surface-variant">({count} đánh giá)</span>
            </div>
          ) : null}
          {!isLoading && count === 0 ? (
            <p className="mt-2 text-sm text-on-surface-variant">Chưa có đánh giá</p>
          ) : null}
        </div>

        {!isLoading ? (
          <button
            type="button"
            onClick={onViewAll}
            className="text-sm font-medium text-primary hover:underline"
          >
            {count > 0 && hasMoreReviews ? `Xem tất cả (${count})` : "Xem trang đánh giá"}
          </button>
        ) : null}
      </div>

      {isLoading ? (
        <div className="mt-6 space-y-4">
          {[1, 2, 3].map((key) => (
            <div key={key} className="animate-pulse rounded-lg border border-outline-variant p-4">
              <div className="flex gap-3">
                <div className="h-10 w-10 rounded-full bg-surface-container" />
                <div className="flex-1 space-y-2">
                  <div className="h-4 w-24 rounded bg-surface-container" />
                  <div className="h-4 w-full rounded bg-surface-container" />
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : null}

      {!isLoading && errorMessage ? (
        <div className="mt-6 rounded-lg border border-error/20 bg-error-container/30 p-4 text-center">
          <p className="text-sm text-on-error-container">{errorMessage}</p>
          <button
            type="button"
            onClick={onRetry}
            className="mt-3 text-sm font-medium text-primary hover:underline"
          >
            Thử lại
          </button>
        </div>
      ) : null}

      {!isLoading && !errorMessage && isEmpty ? (
        <p className="mt-6 text-sm text-on-surface-variant">
          Chưa có đánh giá nào cho sản phẩm này.
        </p>
      ) : null}

      {!isLoading && !errorMessage && reviews.length > 0 ? (
        <div className="mt-6 flex flex-col gap-4">
          {reviews.map((review) => (
            <ProductReviewCard
              key={review.reviewId}
              review={review}
              onComingSoon={onComingSoon}
            />
          ))}
        </div>
      ) : null}

      {!isLoading && !errorMessage ? (
        <button
          type="button"
          onClick={onViewAll}
          className="mt-6 w-full rounded-lg border-2 border-primary py-2.5 text-sm font-medium text-primary transition-colors hover:bg-surface-container-low"
        >
          {count > 0 && hasMoreReviews
            ? `Xem tất cả ${count} đánh giá`
            : "Xem trang đánh giá"}
        </button>
      ) : null}
    </section>
  );
}
