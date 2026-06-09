import { useCallback, useState } from "react";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceShell } from "../components/CommerceShell";
import { ReplyToReviewModal } from "../components/ReplyToReviewModal";
import { SellerOrderPagination } from "../components/SellerOrderPagination";
import { SellerShipmentUpdateConfirmDialog } from "../components/SellerShipmentUpdateConfirmDialog";
import { SellerShopReviewCard } from "../components/SellerShopReviewCard";
import { SellerShopReviewsListSkeleton } from "../components/SellerShopReviewsListSkeleton";
import { SellerShopReviewsReplyTabs } from "../components/SellerShopReviewsReplyTabs";
import { SellerShopReviewsStats } from "../components/SellerShopReviewsStats";
import {
  RATING_FILTER_OPTIONS,
  REVIEW_STATUS_OPTIONS,
} from "../constants/sellerShopReviewsConstants";
import { useReplyToReview } from "../hooks/useReplyToReview";
import { useSellerShopReviews } from "../hooks/useSellerShopReviews";

export function CommerceSellerShopReviewsPage() {
  const [toastMessage, setToastMessage] = useState("");
  const [replyReview, setReplyReview] = useState(null);
  const [pendingContent, setPendingContent] = useState("");

  const {
    reviews,
    ratingSummary,
    activeReplyTab,
    changeReplyTab,
    replyTabCounts,
    ratingFilter,
    changeRatingFilter,
    statusFilter,
    changeStatusFilter,
    page,
    pagination,
    totalItems,
    totalPages,
    rangeStart,
    rangeEnd,
    isLoading,
    errorMessage,
    isEmpty,
    isFilterEmpty,
    isSearchEmpty,
    goToPage,
    retry,
    refresh,
    clientSearch,
    setClientSearch,
  } = useSellerShopReviews();

  const handleReplySuccess = useCallback(() => {
    setReplyReview(null);
    setPendingContent("");
    setToastMessage("Phản hồi đánh giá thành công.");
    refresh();
  }, [refresh]);

  const handleAlreadyReplied = useCallback(() => {
    setReplyReview(null);
    setPendingContent("");
    setToastMessage("Đánh giá này đã có phản hồi. Đang tải lại danh sách.");
    refresh();
  }, [refresh]);

  const { isSubmitting, submitError, reply, clearError } = useReplyToReview({
    onSuccess: handleReplySuccess,
    onAlreadyReplied: handleAlreadyReplied,
  });

  const handleRequestConfirm = useCallback(
    (content) => {
      clearError();
      setPendingContent(content);
    },
    [clearError],
  );

  const handleConfirmReply = useCallback(async () => {
    if (!replyReview?.reviewId || !pendingContent) return;
    await reply(replyReview.reviewId, pendingContent);
  }, [pendingContent, reply, replyReview]);

  const handleCancelConfirm = useCallback(() => {
    if (isSubmitting) return;
    setPendingContent("");
    clearError();
  }, [clearError, isSubmitting]);

  const handleCloseModal = useCallback(() => {
    if (isSubmitting) return;
    setReplyReview(null);
    setPendingContent("");
    clearError();
  }, [clearError, isSubmitting]);

  const disabled = isLoading || isSubmitting;
  const showList = !isLoading && !errorMessage && reviews.length > 0;
  const showEmptyState = !isLoading && !errorMessage && reviews.length === 0;

  return (
    <CommerceShell>
      <div className="mx-auto w-full max-w-[1280px]">
        <div className="mb-6 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
          <div>
            <h1 className="text-headline-lg-mobile font-semibold text-on-surface md:text-headline-lg">
              Đánh giá cửa hàng
            </h1>
            <p className="mt-1 text-body-sm text-on-surface-variant">
              Theo dõi và phản hồi khách hàng
            </p>
          </div>
          <SellerShopReviewsStats
            ratingAvg={ratingSummary.ratingAvg}
            ratingCount={ratingSummary.ratingCount}
          />
        </div>

        <div className="overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-sm">
          <SellerShopReviewsReplyTabs
            activeTabId={activeReplyTab}
            tabCounts={replyTabCounts}
            onChange={changeReplyTab}
            disabled={disabled}
          />

          <div className="flex flex-col gap-3 border-b border-outline-variant bg-surface-bright p-4 sm:flex-row sm:items-center sm:justify-between">
            <div className="relative max-w-sm flex-1">
              <span
                className="material-symbols-outlined pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-[20px] text-outline"
                aria-hidden="true"
              >
                search
              </span>
              <input
                type="search"
                value={clientSearch}
                onChange={(e) => setClientSearch(e.target.value)}
                disabled={disabled}
                placeholder="Tìm sản phẩm hoặc mã mục đơn..."
                className="w-full rounded-lg border border-outline-variant bg-surface py-2 pl-10 pr-4 text-body-sm focus:border-primary focus:outline-none disabled:opacity-50"
              />
            </div>

            <div className="flex flex-wrap items-center gap-2">
              <select
                value={ratingFilter ?? ""}
                onChange={(e) =>
                  changeRatingFilter(e.target.value ? Number(e.target.value) : null)
                }
                disabled={disabled}
                className="rounded-lg border border-outline-variant bg-surface px-3 py-2 text-label-md text-on-surface disabled:opacity-50"
                aria-label="Lọc theo sao"
              >
                {RATING_FILTER_OPTIONS.map((opt) => (
                  <option key={opt.value || "all"} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>

              <select
                value={statusFilter}
                onChange={(e) => changeStatusFilter(e.target.value)}
                disabled={disabled}
                className="rounded-lg border border-outline-variant bg-surface px-3 py-2 text-label-md text-on-surface disabled:opacity-50"
                aria-label="Trạng thái đánh giá"
              >
                {REVIEW_STATUS_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {isLoading ? <SellerShopReviewsListSkeleton /> : null}

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

          {showEmptyState ? (
            <div className="p-10 text-center">
              <span className="material-symbols-outlined mb-2 text-4xl text-outline">rate_review</span>
              <p className="text-body-md text-on-surface-variant">
                {isSearchEmpty
                  ? "Không tìm thấy đánh giá phù hợp."
                  : isFilterEmpty
                    ? "Không có đánh giá với bộ lọc hiện tại."
                    : "Chưa có đánh giá nào."}
              </p>
            </div>
          ) : null}

          {showList ? (
            <div className="divide-y divide-outline-variant">
              {reviews.map((review) => (
                <SellerShopReviewCard
                  key={review.reviewId}
                  review={review}
                  disabled={disabled}
                  onReply={setReplyReview}
                />
              ))}
            </div>
          ) : null}

          {showList && pagination ? (
            <div className="border-t border-outline-variant bg-surface p-4">
              <SellerOrderPagination
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
            </div>
          ) : null}
        </div>
      </div>

      <ReplyToReviewModal
        open={Boolean(replyReview) && !pendingContent}
        review={replyReview}
        isSubmitting={isSubmitting}
        submitError={submitError}
        onClose={handleCloseModal}
        onRequestConfirm={handleRequestConfirm}
      />

      <SellerShipmentUpdateConfirmDialog
        open={Boolean(pendingContent && replyReview)}
        title="Xác nhận phản hồi"
        description="Gửi phản hồi này tới khách hàng? Bạn không thể sửa sau khi gửi."
        isProcessing={isSubmitting}
        errorMessage={submitError}
        confirmLabel="Gửi phản hồi"
        onCancel={handleCancelConfirm}
        onConfirm={handleConfirmReply}
      />

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </CommerceShell>
  );
}
