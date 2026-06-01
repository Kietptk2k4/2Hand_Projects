import { useCallback, useState } from "react";
import { Link } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { buildModerateReviewSuccessToast } from "../constants/adminReviewModerationConstants";
import { useAdminReviewList } from "../hooks/useAdminReviewList";
import { useModerateReview } from "../hooks/useModerateReview";
import { AdminReviewModerateDialog } from "./AdminReviewModerateDialog";
import { AdminReviewModerationFilters } from "./AdminReviewModerationFilters";
import { AdminReviewModerationPagination } from "./AdminReviewModerationPagination";
import { AdminReviewModerationTable } from "./AdminReviewModerationTable";

export function AdminReviewModerationTab() {
  const [toastMessage, setToastMessage] = useState("");
  const [moderateReview, setModerateReview] = useState(null);

  const {
    items,
    activeStatusTabId,
    changeStatusTab,
    ratingFilter,
    changeRatingFilter,
    page,
    pagination,
    totalItems,
    totalPages,
    rangeStart,
    rangeEnd,
    isLoading,
    errorMessage,
    forbidden,
    isEmpty,
    goToPage,
    retry,
    refresh,
    searchInput,
    setSearchInput,
    applySearch,
  } = useAdminReviewList();

  const handleModerateSuccess = useCallback(
    (result, action) => {
      setModerateReview(null);
      setToastMessage(buildModerateReviewSuccessToast(action, result));
      refresh();
    },
    [refresh],
  );

  const { isSubmitting, submitError, submit, clearError } = useModerateReview({
    onSuccess: handleModerateSuccess,
  });

  const handleCloseDialog = useCallback(() => {
    if (isSubmitting) return;
    setModerateReview(null);
    clearError();
  }, [clearError, isSubmitting]);

  const handleSubmitModerate = useCallback(
    async ({ action, reason }) => {
      if (!moderateReview?.reviewId) return;
      await submit(moderateReview.reviewId, { action, reason });
    },
    [moderateReview, submit],
  );

  const disabled = isLoading || isSubmitting;

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-headline-sm font-semibold text-on-surface">Kiểm duyệt đánh giá</h2>
        <p className="mt-1 text-body-sm text-on-surface-variant">
          Ẩn hoặc khôi phục đánh giá sản phẩm trên marketplace. Rating shop chỉ tính từ review
          công khai (VISIBLE).
        </p>
      </div>

      {forbidden ? (
        <div className="rounded-xl border border-error/30 bg-error-container/40 p-8 text-center">
          <p className="text-body-md text-on-error-container">
            Bạn không có quyền truy cập. Đăng nhập bằng tài khoản admin (
            <span className="font-mono">admin@2hands.vn</span>).
          </p>
          <Link to={APP_ROUTES.login} className="mt-4 inline-block text-primary hover:underline">
            Đăng nhập
          </Link>
        </div>
      ) : (
        <>
          <AdminReviewModerationFilters
            activeStatusTabId={activeStatusTabId}
            onStatusChange={changeStatusTab}
            ratingFilter={ratingFilter}
            onRatingChange={changeRatingFilter}
            searchInput={searchInput}
            onSearchInputChange={setSearchInput}
            onSearchSubmit={applySearch}
            disabled={disabled}
          />

          <div className="overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-sm">
            {isLoading ? (
              <div className="space-y-0 divide-y divide-outline-variant">
                {[1, 2, 3, 4, 5].map((key) => (
                  <div key={key} className="h-20 animate-pulse bg-surface-container-low/60" />
                ))}
              </div>
            ) : null}

            {!isLoading && errorMessage ? (
              <div className="p-8 text-center">
                <p className="text-body-md text-on-error-container">{errorMessage}</p>
                <button
                  type="button"
                  onClick={retry}
                  className="mt-4 rounded-lg bg-primary px-4 py-2 text-label-md text-on-primary"
                >
                  Thử lại
                </button>
              </div>
            ) : null}

            {!isLoading && !errorMessage && isEmpty ? (
              <div className="p-10 text-center">
                <span className="material-symbols-outlined mb-2 text-4xl text-outline">
                  rate_review
                </span>
                <p className="text-body-md text-on-surface-variant">
                  Không có đánh giá phù hợp bộ lọc.
                </p>
              </div>
            ) : null}

            {!isLoading && !errorMessage && items.length > 0 ? (
              <AdminReviewModerationTable
                items={items}
                disabled={disabled}
                onModerate={setModerateReview}
              />
            ) : null}

            {!isLoading && !errorMessage && pagination ? (
              <AdminReviewModerationPagination
                page={page}
                totalPages={totalPages}
                rangeStart={rangeStart}
                rangeEnd={rangeEnd}
                totalItems={totalItems}
                disabled={disabled}
                onPrev={() => goToPage(page - 1)}
                onNext={() => goToPage(page + 1)}
                onGoToPage={goToPage}
              />
            ) : null}
          </div>
        </>
      )}

      <AdminReviewModerateDialog
        open={Boolean(moderateReview)}
        review={moderateReview}
        isSubmitting={isSubmitting}
        submitError={submitError}
        onClose={handleCloseDialog}
        onSubmit={handleSubmitModerate}
      />

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </div>
  );
}
