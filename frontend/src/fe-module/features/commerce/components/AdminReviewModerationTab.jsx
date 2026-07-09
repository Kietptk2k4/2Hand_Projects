import { useCallback, useState } from "react";
import { FeedToast } from "../../social/components/FeedToast";
import { buildModerateReviewSuccessToast } from "../constants/adminReviewModerationConstants";
import { useAdminReviewList } from "../hooks/useAdminReviewList";
import { useModerateReview } from "../hooks/useModerateReview";
import { AdminReviewModerateDialog } from "./AdminReviewModerateDialog";
import { AdminReviewModerationTabView } from "./AdminReviewModerationTabView";

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
    <>
      <AdminReviewModerationTabView
        forbidden={forbidden}
        isLoading={isLoading}
        errorMessage={errorMessage}
        isEmpty={isEmpty}
        items={items}
        pagination={pagination}
        page={page}
        totalPages={totalPages}
        totalItems={totalItems}
        rangeStart={rangeStart}
        rangeEnd={rangeEnd}
        disabled={disabled}
        activeStatusTabId={activeStatusTabId}
        onStatusChange={changeStatusTab}
        ratingFilter={ratingFilter}
        onRatingChange={changeRatingFilter}
        searchInput={searchInput}
        onSearchInputChange={setSearchInput}
        onSearchSubmit={applySearch}
        onModerate={setModerateReview}
        onRetry={retry}
        onPrevPage={() => goToPage(page - 1)}
        onNextPage={() => goToPage(page + 1)}
        onGoToPage={goToPage}
      />

      <AdminReviewModerateDialog
        open={Boolean(moderateReview)}
        review={moderateReview}
        isSubmitting={isSubmitting}
        submitError={submitError}
        onClose={handleCloseDialog}
        onSubmit={handleSubmitModerate}
      />

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </>
  );
}
