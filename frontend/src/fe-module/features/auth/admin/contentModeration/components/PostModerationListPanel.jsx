import { useCallback, useEffect, useState } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { getPostsForModeration } from "../api/socialModerationListApi.js";
import { POST_MODERATION_LIST_PAGE_SIZE } from "../constants/postModerationListConstants.js";
import { PostModerationListView } from "./PostModerationListView.jsx";

export function PostModerationListPanel({
  listFilters,
  onFiltersChange,
  selectedPostId,
  onPostSelect,
}) {
  const { showSessionExpired } = useAuthSession();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

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

  useEffect(() => {
    setDraftFilters({
      status: filterStatus,
      moderation_status: filterModerationStatus,
      q: filterQ,
      sort: filterSort,
    });
  }, [filterStatus, filterModerationStatus, filterQ, filterSort]);

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

  const handleApplyFilters = (event) => {
    event.preventDefault();
    onFiltersChange?.({
      ...draftFilters,
      page: 1,
      size: POST_MODERATION_LIST_PAGE_SIZE,
    });
  };

  const handleClearFilters = () => {
    const cleared = {
      status: "",
      moderation_status: "",
      q: "",
      sort: "created_at",
      page: 1,
      size: POST_MODERATION_LIST_PAGE_SIZE,
    };
    setDraftFilters(cleared);
    onFiltersChange?.(cleared);
  };

  const pagination = result?.pagination;
  const currentPage = filterPage || pagination?.page || 1;
  const totalPages = pagination?.total_pages || 1;

  return (
    <PostModerationListView
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
      selectedPostId={selectedPostId}
      onPageChange={(nextPage) =>
        onFiltersChange?.({
          ...listFilters,
          page: nextPage,
          size: POST_MODERATION_LIST_PAGE_SIZE,
        })
      }
      onRowSelect={(row) => onPostSelect?.(row.id)}
    />
  );
}
