import { Link } from "react-router-dom";
import { StarRating } from "./StarRating";
import { APP_ROUTES } from "../../../shared/constants/routes";

const COMMENT_PREVIEW_MAX = 160;

function truncateComment(text) {
  if (!text || text.length <= COMMENT_PREVIEW_MAX) return text;
  return `${text.slice(0, COMMENT_PREVIEW_MAX).trimEnd()}…`;
}

function MyProductReviewStripSkeleton() {
  return (
    <div
      className="mb-6 animate-pulse rounded-xl border border-outline-variant bg-surface-container-low p-4"
      aria-hidden="true"
    >
      <div className="h-4 w-40 rounded bg-surface-container-high" />
      <div className="mt-3 h-4 w-full rounded bg-surface-container-high" />
      <div className="mt-2 h-4 w-3/4 rounded bg-surface-container-high" />
    </div>
  );
}

export function MyProductReviewStrip({
  myReview,
  isLoading,
  isError,
  errorMessage,
  productId,
}) {
  if (isLoading) {
    return <MyProductReviewStripSkeleton />;
  }

  if (isError) {
    return (
      <div className="mb-6 rounded-xl border border-outline-variant bg-surface-container-low p-4">
        <p className="text-body-sm text-on-surface-variant">{errorMessage}</p>
      </div>
    );
  }

  if (!myReview) return null;

  if (myReview.hasReview) {
    const editPath = APP_ROUTES.commerceReviewEdit.replace(":reviewId", myReview.reviewId);
    const reviewsReturnPath = APP_ROUTES.commerceProductReviews.replace(":productId", productId);

    return (
      <section
        className="mb-6 rounded-xl border border-primary/30 bg-surface-container-low p-4 md:p-5"
        aria-labelledby="my-product-review-heading"
      >
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div className="min-w-0 flex-1">
            <h2
              id="my-product-review-heading"
              className="text-headline-sm font-semibold text-on-surface"
            >
              Đánh giá của bạn
            </h2>
            <div className="mt-2">
              <StarRating rating={myReview.rating} />
            </div>
            {myReview.comment ? (
              <p className="mt-3 text-body-sm leading-relaxed text-on-surface">
                {truncateComment(myReview.comment)}
              </p>
            ) : (
              <p className="mt-3 text-body-sm text-on-surface-variant">Bạn chưa viết nhận xét.</p>
            )}
          </div>

          {myReview.canEdit && myReview.reviewId ? (
            <Link
              to={editPath}
              state={{ returnTo: reviewsReturnPath, productId }}
              className="shrink-0 rounded-lg border border-primary px-4 py-2 text-label-md font-medium text-primary hover:bg-surface-container-lowest"
            >
              Sửa đánh giá
            </Link>
          ) : null}
        </div>
      </section>
    );
  }

  return (
    <section
      className="mb-6 rounded-xl border border-outline-variant bg-surface-container-low p-4 md:p-5"
      aria-labelledby="my-product-review-empty-heading"
    >
      <h2
        id="my-product-review-empty-heading"
        className="text-headline-sm font-semibold text-on-surface"
      >
        Đánh giá của bạn
      </h2>
      <p className="mt-2 text-body-sm text-on-surface-variant">
        Bạn chưa đánh giá sản phẩm này.
      </p>
      <p className="mt-1 text-body-sm text-on-surface-variant">
        Hoàn tất đơn hàng và viết đánh giá từ trang chi tiết đơn.
      </p>
      <Link
        to={APP_ROUTES.commerceOrders}
        className="mt-3 inline-flex items-center gap-1 text-label-md font-medium text-primary hover:underline"
      >
        Đơn hàng của tôi
        <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
          arrow_forward
        </span>
      </Link>
    </section>
  );
}
