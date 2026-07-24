import { useCallback, useEffect, useState } from "react";
import { FeedToast } from "../../../../social/components/FeedToast.jsx";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { getCommentsForModeration } from "../api/socialModerationListApi.js";
import { COMMENT_MODERATION_LIST_PAGE_SIZE } from "../constants/commentModerationListConstants.js";
import { useBulkCommentModeration } from "../hooks/useBulkCommentModeration.js";
import { useContentModerationPermissions } from "../hooks/useContentModerationPermissions.js";
import { usePostAuthorSummaries } from "../hooks/usePostAuthorSummaries.js";
import { useCommentModerationStats } from "../hooks/useCommentModerationStats.js";
import { useSyncedDrawerId } from "../../hooks/useSyncedDrawerId.js";
import {
  buildCommentModerationQuickFilter,
  removeCommentModerationFilterChip,
} from "../utils/commentModerationFilterHelpers.js";
import { mapCommentModerationListResponse } from "../utils/commentModerationListMapper.js";
import { CommentModerationBulkDialog } from "./CommentModerationBulkDialog.jsx";
import { CommentModerationDrawer } from "./CommentModerationDrawer.jsx";
import { CommentModerationListView } from "./CommentModerationListView.jsx";

export function CommentModerationListPanel({
  listFilters,
  onFiltersChange,
  selectedCommentId,
  onCommentSelect,
  onCommentClear,
}) {
  const { showSessionExpired } = useAuthSession();
  const { canModerateComment, canRestoreComment } = useContentModerationPermissions();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [selectedCommentIds, setSelectedCommentIds] = useState([]);
  const [bulkDialogMode, setBulkDialogMode] = useState(null);
  const [toastMessage, setToastMessage] = useState("");

  const filterStatus = listFilters.status || "";
  const filterModerationStatus = listFilters.moderation_status || "";
  const filterPostId = listFilters.post_id || "";
  const filterQ = listFilters.q || "";
  const filterSort = listFilters.sort || "created_at";
  const filterPage = Number(listFilters.page) || 1;
  const filterSize = Number(listFilters.size) || COMMENT_MODERATION_LIST_PAGE_SIZE;

  const [draftFilters, setDraftFilters] = useState({
    status: filterStatus,
    moderation_status: filterModerationStatus,
    post_id: filterPostId,
    q: filterQ,
    sort: filterSort,
  });

  const selectionEnabled = canModerateComment || canRestoreComment;
  const { stats, status: statsStatus, refetch: refetchStats } = useCommentModerationStats();
  const {
    isSubmitting: isBulkSubmitting,
    submitError: bulkSubmitError,
    execute: executeBulk,
    clearError: clearBulkError,
  } = useBulkCommentModeration();

  useEffect(() => {
    setDraftFilters({
      status: filterStatus,
      moderation_status: filterModerationStatus,
      post_id: filterPostId,
      q: filterQ,
      sort: filterSort,
    });
  }, [filterStatus, filterModerationStatus, filterPostId, filterQ, filterSort]);

  useEffect(() => {
    setSelectedCommentIds([]);
  }, [
    filterStatus,
    filterModerationStatus,
    filterPostId,
    filterQ,
    filterSort,
    filterPage,
    filterSize,
  ]);

  const fetchList = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getCommentsForModeration({
        status: filterStatus || undefined,
        moderation_status: filterModerationStatus || undefined,
        post_id: filterPostId || undefined,
        q: filterQ || undefined,
        sort: filterSort,
        page: filterPage,
        size: filterSize,
      });
      setResult(mapCommentModerationListResponse(data));
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
      setErrorMessage(error?.message || "Không tải được danh sách bình luận.");
    }
  }, [
    filterStatus,
    filterModerationStatus,
    filterPostId,
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
      post_id: "",
      q: "",
      sort: "created_at",
      page: 1,
      size: String(COMMENT_MODERATION_LIST_PAGE_SIZE),
    };
    setDraftFilters(cleared);
    onFiltersChange?.(cleared);
  };

  const handleQuickFilter = (preset) => {
    const next = buildCommentModerationQuickFilter(preset);
    setDraftFilters({
      status: next.status,
      moderation_status: next.moderation_status,
      post_id: next.post_id,
      q: next.q,
      sort: next.sort,
    });
    applyFiltersPatch({
      ...next,
      size: String(filterSize),
    });
  };

  const handleRemoveFilterChip = (chipKey) => {
    const next = removeCommentModerationFilterChip(listFilters, chipKey);
    setDraftFilters({
      status: next.status,
      moderation_status: next.moderation_status,
      post_id: next.post_id,
      q: next.q,
      sort: next.sort,
    });
    applyFiltersPatch({
      ...next,
      size: String(filterSize),
    });
  };

  const handleToggleComment = useCallback((commentId) => {
    setSelectedCommentIds((current) =>
      current.includes(commentId) ? current.filter((id) => id !== commentId) : [...current, commentId],
    );
  }, []);

  const handleToggleAll = useCallback((pageItems) => {
    const pageIds = pageItems.map((item) => item.id);
    setSelectedCommentIds((current) => {
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
        commentIds: selectedCommentIds,
        mode,
        action,
        reason,
      });

      if (succeeded.length) {
        setToastMessage(
          `Đã xử lý ${succeeded.length} bình luận${failed.length ? `, ${failed.length} thất bại` : ""}.`,
        );
        setSelectedCommentIds(failed.map((item) => item.commentId));
        setBulkDialogMode(null);
        clearBulkError();
        refreshAll();
      }
    },
    [clearBulkError, executeBulk, refreshAll, selectedCommentIds],
  );

  const pagination = result?.pagination;
  const currentPage = filterPage || pagination?.page || 1;
  const totalPages = pagination?.total_pages || 1;
  const items = result?.items || [];
  const authorIdsNeedingFallback = items
    .filter((item) => item.author_id && !item.author_display_name)
    .map((item) => item.author_id);
  const authorSummaries = usePostAuthorSummaries(authorIdsNeedingFallback);
  const { openId: drawerCommentId, closeDrawer } = useSyncedDrawerId(
    selectedCommentId,
    onCommentClear,
  );
  const selectedComment = items.find((item) => item.id === drawerCommentId) || null;

  return (
    <>
      <CommentModerationListView
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
        selectedCommentId={drawerCommentId}
        selectedCommentIds={selectedCommentIds}
        selectionEnabled={selectionEnabled}
        canModerateComment={canModerateComment}
        canRestoreComment={canRestoreComment}
        bulkSubmitting={isBulkSubmitting}
        onToggleComment={handleToggleComment}
        onToggleAll={handleToggleAll}
        onOpenBulkModerate={() => setBulkDialogMode("moderate")}
        onOpenBulkRestore={() => setBulkDialogMode("restore")}
        onClearBulkSelection={() => setSelectedCommentIds([])}
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
          if (drawerCommentId === row.id) {
            closeDrawer();
            return;
          }
          onCommentSelect?.(row.id);
        }}
        drawer={
          drawerCommentId ? (
            <CommentModerationDrawer
              commentId={drawerCommentId}
              comment={selectedComment}
              onClose={closeDrawer}
              onRefresh={refreshAll}
            />
          ) : null
        }
      />

      <CommentModerationBulkDialog
        open={Boolean(bulkDialogMode)}
        mode={bulkDialogMode}
        selectedCount={selectedCommentIds.length}
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
