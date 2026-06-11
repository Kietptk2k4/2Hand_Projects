import { useCallback, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceShell } from "../components/CommerceShell";
import { ProductReviewCard } from "../components/ProductReviewCard";
import { ProductReviewsFilters } from "../components/ProductReviewsFilters";
import { ProductReviewsSkeleton } from "../components/ProductReviewsSkeleton";
import { ProductReviewsSummary } from "../components/ProductReviewsSummary";
import { useShopReviews } from "../hooks/useShopReviews";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { buildCommerceShopPath } from "../utils/commerceRoutes";

const COMING_SOON_MESSAGE = "Tính năng đang được phát triển.";

export function CommerceShopReviewsPage() {
  const { shopId } = useParams();
  const [toastMessage, setToastMessage] = useState("");

  const {
    shopName,
    reviews,
    ratingSummary,
    sort,
    ratingFilter,
    changeSort,
    changeRatingFilter,
    isInitialLoading,
    isLoadingMore,
    isNotFound,
    isEmpty,
    hasNext,
    errorMessage,
    loadMore,
    retry,
  } = useShopReviews(shopId);

  const showComingSoon = useCallback(() => {
    setToastMessage(COMING_SOON_MESSAGE);
  }, []);

  const dismissToast = useCallback(() => {
    setToastMessage("");
  }, []);

  const shopTitle = shopName || shopId;
  const shopPath = buildCommerceShopPath(shopId);

  return (
    <CommerceShell onComingSoon={showComingSoon}>
      <div className="mx-auto w-full max-w-[1280px]">
        {isInitialLoading ? <ProductReviewsSkeleton /> : null}

        {isNotFound ? (
          <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center">
            <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
              rate_review
            </span>
            <p className="text-sm text-on-surface-variant">
              {errorMessage || "Shop không tồn tại."}
            </p>
            <div className="mt-4 flex flex-wrap justify-center gap-3">
              <Link
                to={APP_ROUTES.commerceHome}
                className="rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
              >
                Về trang Commerce
              </Link>
              <Link
                to={shopPath}
                className="rounded-lg border border-primary px-4 py-2 text-sm font-medium text-primary hover:bg-surface-container-low"
              >
                Xem shop
              </Link>
            </div>
          </div>
        ) : null}

        {!isInitialLoading && !isNotFound && errorMessage && reviews.length === 0 ? (
          <div className="rounded-xl border border-error/30 bg-error-container/40 p-6 text-center">
            <p className="text-sm text-on-error-container">{errorMessage}</p>
            <button
              type="button"
              onClick={retry}
              className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
            >
              Thử lại
            </button>
          </div>
        ) : null}

        {!isInitialLoading && !isNotFound && !errorMessage ? (
          <>
            <nav
              className="mb-6 flex flex-wrap items-center text-body-sm text-on-surface-variant"
              aria-label="Breadcrumb"
            >
              <Link to={APP_ROUTES.commerceHome} className="transition-colors hover:text-primary">
                Trang chủ
              </Link>
              <span className="material-symbols-outlined mx-1 text-[16px]" aria-hidden="true">
                chevron_right
              </span>
              <Link to={shopPath} className="line-clamp-1 transition-colors hover:text-primary">
                Shop
              </Link>
              <span className="material-symbols-outlined mx-1 text-[16px]" aria-hidden="true">
                chevron_right
              </span>
              <span className="font-medium text-on-surface">Đánh giá</span>
            </nav>

            <header className="mb-8">
              <h1 className="text-headline-lg-mobile font-bold text-on-surface md:text-headline-lg">
                Đánh giá shop
              </h1>
              <p className="mt-1 line-clamp-2 text-body-md text-on-surface-variant">{shopTitle}</p>
            </header>

            <div className="grid grid-cols-1 items-start gap-8 lg:grid-cols-12">
              <aside className="space-y-0 lg:sticky lg:top-24 lg:col-span-4">
                <ProductReviewsSummary ratingSummary={ratingSummary} />
                <ProductReviewsFilters
                  sort={sort}
                  ratingFilter={ratingFilter}
                  onSortChange={changeSort}
                  onRatingFilterChange={changeRatingFilter}
                  onComingSoon={showComingSoon}
                  disabled={isInitialLoading}
                />
              </aside>

              <section className="lg:col-span-8">
                {isEmpty ? (
                  <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-10 text-center">
                    <span
                      className="material-symbols-outlined mb-2 text-4xl text-outline"
                      aria-hidden="true"
                    >
                      rate_review
                    </span>
                    <p className="text-sm text-on-surface-variant">
                      Chưa có đánh giá nào cho shop này.
                    </p>
                    <Link
                      to={shopPath}
                      className="mt-4 inline-block text-sm font-medium text-primary hover:underline"
                    >
                      Quay lại shop
                    </Link>
                  </div>
                ) : (
                  <div className="flex flex-col gap-4">
                    {reviews.map((review) => (
                      <ProductReviewCard
                        key={review.reviewId}
                        review={review}
                        productName={review.productName}
                        onComingSoon={showComingSoon}
                      />
                    ))}
                  </div>
                )}

                {hasNext ? (
                  <div className="mt-8 flex justify-center">
                    {isLoadingMore ? (
                      <div
                        className="h-8 w-8 animate-spin rounded-full border-4 border-[#d8e3fb] border-t-primary"
                        aria-label="Đang tải thêm"
                      />
                    ) : (
                      <button
                        type="button"
                        onClick={loadMore}
                        className="rounded-md border-2 border-primary px-8 py-3 text-label-md font-bold text-primary transition-colors hover:bg-primary hover:text-on-primary"
                      >
                        Tải thêm đánh giá
                      </button>
                    )}
                  </div>
                ) : null}
              </section>
            </div>
          </>
        ) : null}
      </div>

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}
