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
import { CommerceFooterTrustSection } from "../components/CommerceFooterTrustSection";
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
        {/* Header & Overview Stats */}
        <div className="mb-6 flex flex-col gap-4 md:flex-row md:items-center md:justify-between border-b border-outline-variant/60 pb-4">
          <div>
            <h1 className="text-xl font-black text-on-surface sm:text-2xl">
              ĐÁNH GIÁ CỬA HÀNG
            </h1>
            <p className="mt-1 text-xs font-semibold text-on-surface-variant">
              Theo dõi và tương tác phản hồi ý kiến của khách hàng
            </p>
          </div>
          <SellerShopReviewsStats
            ratingAvg={ratingSummary.ratingAvg}
            ratingCount={ratingSummary.ratingCount}
          />
        </div>

        {/* Reply Tabs */}
        <div className="mb-4">
          <SellerShopReviewsReplyTabs
            activeTabId={activeReplyTab}
            tabCounts={replyTabCounts}
            onChange={changeReplyTab}
            disabled={disabled}
          />
        </div>

        {/* Search & Filter Bar */}
        <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="relative min-w-[260px] flex-1">
            <span
              className="material-symbols-outlined pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 text-lg"
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
              className="w-full rounded-2xl border border-outline-variant bg-surface-container-lowest py-3 pl-10 pr-4 text-xs text-on-surface shadow-xs transition-all focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20 sm:text-sm disabled:opacity-50"
            />
          </div>

          <div className="flex flex-wrap items-center gap-2">
            <select
              value={ratingFilter ?? ""}
              onChange={(e) =>
                changeRatingFilter(e.target.value ? Number(e.target.value) : null)
              }
              disabled={disabled}
              className="rounded-2xl border border-outline-variant bg-surface-container-lowest px-4 py-3 text-xs font-bold text-on-surface shadow-xs transition-all focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20 disabled:opacity-50 cursor-pointer"
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
              className="rounded-2xl border border-outline-variant bg-surface-container-lowest px-4 py-3 text-xs font-bold text-on-surface shadow-xs transition-all focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20 disabled:opacity-50 cursor-pointer"
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

        {/* Skeleton loader */}
        {isLoading ? <SellerShopReviewsListSkeleton /> : null}

        {/* Error Container */}
        {!isLoading && errorMessage ? (
          <div className="rounded-2xl border border-error/30 bg-error-container/40 p-8 text-center shadow-xs">
            <p className="text-sm font-semibold text-on-error-container">{errorMessage}</p>
            <button
              type="button"
              onClick={retry}
              className="mt-4 rounded-xl bg-primary px-6 py-2.5 text-xs font-bold text-on-primary hover:bg-[#0050cb]"
            >
              Thử lại ngay
            </button>
          </div>
        ) : null}

        {/* Empty State */}
        {showEmptyState ? (
          <div className="rounded-2xl border border-outline-variant bg-surface-container-lowest p-12 text-center shadow-xs">
            <span className="material-symbols-outlined mb-2 text-5xl text-slate-300" aria-hidden="true">
              rate_review
            </span>
            <p className="text-sm font-semibold text-on-surface-variant">
              {isSearchEmpty
                ? "Không tìm thấy đánh giá phù hợp."
                : isFilterEmpty
                  ? "Không có đánh giá với bộ lọc hiện tại."
                  : "Chưa có đánh giá nào."}
            </p>
          </div>
        ) : null}

        {/* Review Cards List */}
        {showList ? (
          <div className="space-y-4">
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

        {/* Pagination Toolbar */}
        {showList && pagination ? (
          <div className="mt-4 flex flex-wrap items-center justify-between gap-4 text-xs font-semibold text-on-surface-variant">
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

        {/* E-Commerce Trust Badges Footer */}
        <CommerceFooterTrustSection />
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
