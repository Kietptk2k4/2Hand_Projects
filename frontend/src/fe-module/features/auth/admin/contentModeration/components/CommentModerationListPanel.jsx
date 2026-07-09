import { useCallback, useEffect, useState } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { getCommentsForModeration } from "../api/socialModerationListApi.js";
import { COMMENT_MODERATION_LIST_PAGE_SIZE } from "../constants/commentModerationListConstants.js";
import { CommentModerationListView } from "./CommentModerationListView.jsx";

export function CommentModerationListPanel({
  listFilters,
  onFiltersChange,
  selectedCommentId,
  onCommentSelect,
}) {
  const { showSessionExpired } = useAuthSession();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const filterStatus = listFilters.status || "";
  const filterPostId = listFilters.post_id || "";
  const filterQ = listFilters.q || "";
  const filterSort = listFilters.sort || "created_at";
  const filterPage = Number(listFilters.page) || 1;
  const filterSize = Number(listFilters.size) || COMMENT_MODERATION_LIST_PAGE_SIZE;

  const [draftFilters, setDraftFilters] = useState({
    status: filterStatus,
    post_id: filterPostId,
    q: filterQ,
    sort: filterSort,
  });

  useEffect(() => {
    setDraftFilters({
      status: filterStatus,
      post_id: filterPostId,
      q: filterQ,
      sort: filterSort,
    });
  }, [filterStatus, filterPostId, filterQ, filterSort]);

  const fetchList = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getCommentsForModeration({
        status: filterStatus || undefined,
        post_id: filterPostId || undefined,
        q: filterQ || undefined,
        sort: filterSort,
        page: filterPage,
        size: filterSize,
      });
      setResult(data);
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
  }, [filterStatus, filterPostId, filterQ, filterSort, filterPage, filterSize, showSessionExpired]);

  useEffect(() => {
    fetchList();
  }, [fetchList]);

  const handleApplyFilters = (event) => {
    event.preventDefault();
    onFiltersChange?.({
      ...draftFilters,
      page: 1,
      size: COMMENT_MODERATION_LIST_PAGE_SIZE,
    });
  };

  const handleClearFilters = () => {
    const cleared = {
      status: "",
      post_id: "",
      q: "",
      sort: "created_at",
      page: 1,
      size: COMMENT_MODERATION_LIST_PAGE_SIZE,
    };
    setDraftFilters(cleared);
    onFiltersChange?.(cleared);
  };

  const pagination = result?.pagination;
  const currentPage = filterPage || pagination?.page || 1;
  const totalPages = pagination?.total_pages || 1;

  return (
    <CommentModerationListView
      status={status}
      errorMessage={errorMessage}
      draftFilters={draftFilters}
      onDraftFiltersChange={setDraftFilters}
      onApplyFilters={handleApplyFilters}
      onClearFilters={handleClearFilters}
      onRetry={fetchList}
      items={result?.items || []}
      pagination={pagination}
      currentPage={currentPage}
      totalPages={totalPages}
      activeSort={filterSort}
      selectedCommentId={selectedCommentId}
      onPageChange={(nextPage) =>
        onFiltersChange?.({
          ...listFilters,
          page: nextPage,
          size: COMMENT_MODERATION_LIST_PAGE_SIZE,
        })
      }
      onRowSelect={(row) => onCommentSelect?.(row.id)}
    />
  );
}
