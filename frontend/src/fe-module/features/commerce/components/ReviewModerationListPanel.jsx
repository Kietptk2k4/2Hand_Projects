import { useCallback, useEffect, useState } from "react";
import { FeedToast } from "../../social/components/FeedToast";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { useContentModerationPermissions } from "../../auth/admin/contentModeration/hooks/useContentModerationPermissions.js";
import { fetchAdminReviewList } from "../api/adminReviewModerationApi";
import { mapAdminReviewModerationApiError } from "../constants/adminReviewModerationConstants";
import { REVIEW_MODERATION_LIST_PAGE_SIZE } from "../constants/reviewModerationListConstants.js";
import { useBulkReviewModeration } from "../hooks/useBulkReviewModeration.js";
import { useReviewModerationStats } from "../hooks/useReviewModerationStats.js";
import {
  buildReviewModerationQuickFilter,
  removeReviewModerationFilterChip,
} from "../utils/reviewModerationFilterHelpers.js";
import { mapAdminReviewListResponse } from "../utils/adminReviewModerationMapper";
import { ReviewModerationBulkDialog } from "./ReviewModerationBulkDialog.jsx";
import { ReviewModerationDrawer } from "./ReviewModerationDrawer.jsx";
import { ReviewModerationListView } from "./ReviewModerationListView.jsx";

export function ReviewModerationListPanel({
  listFilters,
  onFiltersChange,
  selectedReviewId,
  onReviewSelect,
  onReviewClear,
}) {
  const { showSessionExpired } = useAuthSession();
  const { canHideReview, canRestoreReview } = useContentModerationPermissions();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [selectedReviewIds, setSelectedReviewIds] = useState([]);
  const [bulkDialogMode, setBulkDialogMode] = useState(null);
  const [toastMessage, setToastMessage] = useState("");

  const filterStatus = listFilters.status || "";
  const filterRating = listFilters.rating || "";
  const filterQ = listFilters.q || "";
  const filterSort = listFilters.sort || "NEWEST";
  const filterPage = Number(listFilters.page) || 1;
  const filterSize = Number(listFilters.size) || REVIEW_MODERATION_LIST_PAGE_SIZE;

  const [draftFilters, setDraftFilters] = useState({
    status: filterStatus,
    rating: filterRating,
    q: filterQ,
    sort: filterSort,
  });

  const selectionEnabled = canHideReview || canRestoreReview;
  const { stats, status: statsStatus, refetch: refetchStats } = useReviewModerationStats();
  const {
    isSubmitting: isBulkSubmitting,
    submitError: bulkSubmitError,
    execute: executeBulk,
    clearError: clearBulkError,
  } = useBulkReviewModeration();

  useEffect(() => {
    setDraftFilters({
      status: filterStatus,
      rating: filterRating,
      q: filterQ,
      sort: filterSort,
    });
  }, [filterStatus, filterRating, filterQ, filterSort]);

  useEffect(() => {
    setSelectedReviewIds([]);
  }, [filterStatus, filterRating, filterQ, filterSort, filterPage, filterSize]);

  const fetchList = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchAdminReviewList({
        page: filterPage,
        limit: filterSize,
        status: filterStatus || undefined,
        rating: filterRating || undefined,
        q: filterQ || undefined,
        sort: filterSort,
      });
      setResult(mapAdminReviewListResponse(data));
      setStatus("ready");
    } catch (error) {
      const code = String(error?.code ?? "");
      if (code === "401" || code.includes("401") || code.includes("COMMERCE-401")) {
        showSessionExpired(error?.message);
        return;
      }
      if (code === "COMMERCE-403") {
        setStatus("forbidden");
        setErrorMessage(error?.message || "Bạn không có quyền truy cập.");
        return;
      }
      setStatus("error");
      setErrorMessage(mapAdminReviewModerationApiError(error));
    }
  }, [
    filterPage,
    filterQ,
    filterRating,
    filterSize,
    filterSort,
    filterStatus,
    showSessionExpired,
  ]);

  useEffect(() => {
    fetchList();
  }, [fetchList]);

  const refreshAll = useCallback(() => {
    fetchList();
    refetchStats();
  }, [fetchList, refetchStats]);

  const applyFiltersPatch = useCallback(
    (patch) => {
      onFiltersChange?.({
        ...listFilters,
        ...patch,
      });
    },
    [listFilters, onFiltersChange],
  );

  const handleApplyFilters = (event) => {
    event.preventDefault();
    applyFiltersPatch({
      ...draftFilters,
      page: "1",
      size: String(filterSize),
    });
  };

  const handleClearFilters = () => {
    const cleared = {
      status: "",
      rating: "",
      q: "",
      sort: "NEWEST",
      page: "1",
      size: String(REVIEW_MODERATION_LIST_PAGE_SIZE),
    };
    setDraftFilters(cleared);
    onFiltersChange?.(cleared);
  };

  const handleQuickFilter = (preset) => {
    const next = buildReviewModerationQuickFilter(preset);
    setDraftFilters({
      status: next.status,
      rating: next.rating,
      q: next.q,
      sort: next.sort,
    });
    applyFiltersPatch({
      ...next,
      size: String(filterSize),
    });
  };

  const handleRemoveFilterChip = (chipKey) => {
    const next = removeReviewModerationFilterChip(listFilters, chipKey);
    setDraftFilters({
      status: next.status,
      rating: next.rating,
      q: next.q,
      sort: next.sort,
    });
    applyFiltersPatch({
      ...next,
      size: String(filterSize),
    });
  };

  const handleToggleReview = useCallback((reviewId) => {
    setSelectedReviewIds((current) =>
      current.includes(reviewId) ? current.filter((id) => id !== reviewId) : [...current, reviewId],
    );
  }, []);

  const handleToggleAll = useCallback((pageItems) => {
    const pageIds = pageItems.map((item) => item.reviewId);
    setSelectedReviewIds((current) => {
      const allSelected = pageIds.every((id) => current.includes(id));
      if (allSelected) {
        return current.filter((id) => !pageIds.includes(id));
      }
      return [...new Set([...current, ...pageIds])];
    });
  }, []);

  const handleBulkSubmit = useCallback(
    async ({ mode, reason, note }) => {
      const { succeeded, failed } = await executeBulk({
        reviewIds: selectedReviewIds,
        mode,
        reason,
        note,
      });

      if (succeeded.length) {
        setToastMessage(
          `Đã xử lý ${succeeded.length} đánh giá${failed.length ? `, ${failed.length} thất bại` : ""}.`,
        );
        setSelectedReviewIds(failed.map((item) => item.reviewId));
        setBulkDialogMode(null);
        clearBulkError();
        refreshAll();
      }
    },
    [clearBulkError, executeBulk, refreshAll, selectedReviewIds],
  );

  const pagination = result?.pagination;
  const currentPage = filterPage || pagination?.page || 1;
  const totalPages = pagination?.totalPages || pagination?.total_pages || 1;
  const items = result?.items || [];
  const selectedReview = items.find((item) => item.reviewId === selectedReviewId) || null;

  return (
    <>
      <ReviewModerationListView
        status={status}
        errorMessage={errorMessage}
        appliedFilters={listFilters}
        draftFilters={draftFilters}
        onDraftFiltersChange={setDraftFilters}
        onApplyFilters={handleApplyFilters}
        onClearFilters={handleClearFilters}
        onQuickFilter={handleQuickFilter}
        onRemoveFilterChip={handleRemoveFilterChip}
        onRetry={fetchList}
        stats={stats}
        statsStatus={statsStatus}
        onStatPresetClick={handleQuickFilter}
        items={items}
        pagination={pagination}
        currentPage={currentPage}
        totalPages={totalPages}
        pageSize={String(filterSize)}
        activeSort={filterSort}
        selectedReviewId={selectedReviewId}
        selectedReviewIds={selectedReviewIds}
        selectionEnabled={selectionEnabled}
        canHideReview={canHideReview}
        canRestoreReview={canRestoreReview}
        bulkSubmitting={isBulkSubmitting}
        onToggleReview={handleToggleReview}
        onToggleAll={handleToggleAll}
        onOpenBulkHide={() => setBulkDialogMode("hide")}
        onOpenBulkRestore={() => setBulkDialogMode("restore")}
        onClearBulkSelection={() => setSelectedReviewIds([])}
        onPageChange={(nextPage) =>
          applyFiltersPatch({
            page: String(nextPage),
            size: String(filterSize),
          })
        }
        onPageSizeChange={(nextSize) =>
          applyFiltersPatch({
            page: "1",
            size: String(nextSize),
          })
        }
        onRowSelect={(review) => onReviewSelect?.(review.reviewId)}
        drawer={
          selectedReviewId ? (
            <ReviewModerationDrawer
              reviewId={selectedReviewId}
              review={selectedReview}
              onClose={onReviewClear}
              onRefresh={refreshAll}
            />
          ) : null
        }
      />

      <ReviewModerationBulkDialog
        open={Boolean(bulkDialogMode)}
        mode={bulkDialogMode}
        selectedCount={selectedReviewIds.length}
        isSubmitting={isBulkSubmitting}
        submitError={bulkSubmitError}
        onClose={() => {
          if (isBulkSubmitting) return;
          setBulkDialogMode(null);
          clearBulkError();
        }}
        onSubmit={handleBulkSubmit}
      />

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </>
  );
}
