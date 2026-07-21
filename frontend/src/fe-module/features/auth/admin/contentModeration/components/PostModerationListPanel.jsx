import { useCallback, useEffect, useState } from "react";
import { FeedToast } from "../../../../social/components/FeedToast.jsx";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { getPostsForModeration } from "../api/socialModerationListApi.js";
import { POST_MODERATION_LIST_PAGE_SIZE } from "../constants/postModerationListConstants.js";
import { useBulkPostModeration } from "../hooks/useBulkPostModeration.js";
import { useContentModerationPermissions } from "../hooks/useContentModerationPermissions.js";
import { usePostAuthorSummaries } from "../hooks/usePostAuthorSummaries.js";
import { usePostModerationStats } from "../hooks/usePostModerationStats.js";
import {
  buildPostModerationQuickFilter,
  removePostModerationFilterChip,
} from "../utils/postModerationFilterHelpers.js";
import { mapPostModerationListResponse } from "../utils/postModerationListMapper.js";
import { PostModerationBulkDialog } from "./PostModerationBulkDialog.jsx";
import { PostModerationDrawer } from "./PostModerationDrawer.jsx";
import { PostModerationListView } from "./PostModerationListView.jsx";

export function PostModerationListPanel({
  listFilters,
  onFiltersChange,
  selectedPostId,
  onPostSelect,
  onPostClear,
}) {
  const { showSessionExpired } = useAuthSession();
  const { canModeratePost, canRestorePost } = useContentModerationPermissions();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [selectedPostIds, setSelectedPostIds] = useState([]);
  const [bulkDialogMode, setBulkDialogMode] = useState(null);
  const [toastMessage, setToastMessage] = useState("");

  const filterStatus = listFilters.status || "";
  const filterModerationStatus = listFilters.moderation_status || "";
  const filterQ = listFilters.q || "";
  const filterSort = listFilters.sort || "created_at";
  const filterPage = Number(listFilters.page) || 1;
  const filterSize = Number(listFilters.size) || POST_MODERATION_LIST_PAGE_SIZE;

  const [draftFilters, setDraftFilters] = useState({
    status: filterStatus,
    moderation_status: filterModerationStatus,
    q: filterQ,
    sort: filterSort,
  });

  const selectionEnabled = canModeratePost || canRestorePost;
  const { stats, status: statsStatus, refetch: refetchStats } = usePostModerationStats();
  const {
    isSubmitting: isBulkSubmitting,
    submitError: bulkSubmitError,
    execute: executeBulk,
    clearError: clearBulkError,
  } = useBulkPostModeration();

  useEffect(() => {
    setDraftFilters({
      status: filterStatus,
      moderation_status: filterModerationStatus,
      q: filterQ,
      sort: filterSort,
    });
  }, [filterStatus, filterModerationStatus, filterQ, filterSort]);

  useEffect(() => {
    setSelectedPostIds([]);
  }, [filterStatus, filterModerationStatus, filterQ, filterSort, filterPage, filterSize]);

  const fetchList = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getPostsForModeration({
        status: filterStatus || undefined,
        moderation_status: filterModerationStatus || undefined,
        q: filterQ || undefined,
        sort: filterSort,
        page: filterPage,
        size: filterSize,
      });
      setResult(mapPostModerationListResponse(data));
      setStatus("ready");
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      if (error?.code === 403) {
        setStatus("forbidden");
        setErrorMessage(error?.message || "Bạn không có quyền truy cập.");
        return;
      }
      setStatus("error");
      setErrorMessage(error?.message || "Không tải được danh sách bài viết.");
    }
  }, [
    filterStatus,
    filterModerationStatus,
    filterQ,
    filterSort,
    filterPage,
    filterSize,
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
      page: 1,
      size: String(filterSize),
    });
  };

  const handleClearFilters = () => {
    const cleared = {
      status: "",
      moderation_status: "",
      q: "",
      sort: "created_at",
      page: 1,
      size: String(POST_MODERATION_LIST_PAGE_SIZE),
    };
    setDraftFilters(cleared);
    onFiltersChange?.(cleared);
  };

  const handleQuickFilter = (preset) => {
    const next = buildPostModerationQuickFilter(preset);
    setDraftFilters({
      status: next.status,
      moderation_status: next.moderation_status,
      q: next.q,
      sort: next.sort,
    });
    applyFiltersPatch({
      ...next,
      size: String(filterSize),
    });
  };

  const handleRemoveFilterChip = (chipKey) => {
    const next = removePostModerationFilterChip(listFilters, chipKey);
    setDraftFilters({
      status: next.status,
      moderation_status: next.moderation_status,
      q: next.q,
      sort: next.sort,
    });
    applyFiltersPatch({
      ...next,
      size: String(filterSize),
    });
  };

  const handleTogglePost = useCallback((postId) => {
    setSelectedPostIds((current) =>
      current.includes(postId) ? current.filter((id) => id !== postId) : [...current, postId],
    );
  }, []);

  const handleToggleAll = useCallback((pageItems) => {
    const pageIds = pageItems.map((item) => item.id);
    setSelectedPostIds((current) => {
      const allSelected = pageIds.every((id) => current.includes(id));
      if (allSelected) {
        return current.filter((id) => !pageIds.includes(id));
      }
      return [...new Set([...current, ...pageIds])];
    });
  }, []);

  const handleBulkSubmit = useCallback(
    async ({ mode, action, reason }) => {
      const { succeeded, failed } = await executeBulk({
        postIds: selectedPostIds,
        mode,
        action,
        reason,
      });

      if (succeeded.length) {
        setToastMessage(
          `Đã xử lý ${succeeded.length} bài viết${failed.length ? `, ${failed.length} thất bại` : ""}.`,
        );
        setSelectedPostIds(failed.map((item) => item.postId));
        setBulkDialogMode(null);
        clearBulkError();
        refreshAll();
      }
    },
    [clearBulkError, executeBulk, refreshAll, selectedPostIds],
  );

  const pagination = result?.pagination;
  const currentPage = filterPage || pagination?.page || 1;
  const totalPages = pagination?.total_pages || 1;
  const items = result?.items || [];
  const authorIdsNeedingFallback = items
    .filter((item) => item.author_id && !item.author_display_name)
    .map((item) => item.author_id);
  const authorSummaries = usePostAuthorSummaries(authorIdsNeedingFallback);
  const selectedPost = items.find((item) => item.id === selectedPostId) || null;

  return (
    <>
      <PostModerationListView
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
        authorSummaries={authorSummaries}
        pagination={pagination}
        currentPage={currentPage}
        totalPages={totalPages}
        pageSize={String(filterSize)}
        activeSort={filterSort}
        selectedPostId={selectedPostId}
        selectedPostIds={selectedPostIds}
        selectionEnabled={selectionEnabled}
        canModeratePost={canModeratePost}
        canRestorePost={canRestorePost}
        bulkSubmitting={isBulkSubmitting}
        onTogglePost={handleTogglePost}
        onToggleAll={handleToggleAll}
        onOpenBulkModerate={() => setBulkDialogMode("moderate")}
        onOpenBulkRestore={() => setBulkDialogMode("restore")}
        onClearBulkSelection={() => setSelectedPostIds([])}
        onPageChange={(nextPage) =>
          applyFiltersPatch({
            page: String(nextPage),
            size: String(filterSize),
          })
        }
        onPageSizeChange={(nextSize) =>
          applyFiltersPatch({
            page: "1",
            size: nextSize,
          })
        }
        onRowSelect={(row) => {
          if (selectedPostId === row.id) {
            onPostClear?.();
            return;
          }
          onPostSelect?.(row.id);
        }}
        drawer={
          selectedPostId ? (
            <PostModerationDrawer
              postId={selectedPostId}
              post={selectedPost}
              onClose={() => onPostClear?.()}
              onRefresh={refreshAll}
            />
          ) : null
        }
      />

      <PostModerationBulkDialog
        open={Boolean(bulkDialogMode)}
        mode={bulkDialogMode}
        selectedCount={selectedPostIds.length}
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
