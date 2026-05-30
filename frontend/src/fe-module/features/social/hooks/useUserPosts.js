import { useCallback, useEffect, useRef, useState } from "react";
import { fetchUserPosts } from "../api/userPostsApi";
import { PROFILE_POSTS_PAGE_SIZE } from "../constants/socialProfileConstants";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

export function useUserPosts(userId, { enabled = true, statusFilter = "published" } = {}) {
  const { showSessionExpired } = useAuthSession();
  const [page, setPage] = useState(0);
  const [items, setItems] = useState([]);
  const [meta, setMeta] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [errorCode, setErrorCode] = useState(null);
  const requestIdRef = useRef(0);

  const reset = useCallback(() => {
    setPage(0);
    setItems([]);
    setMeta(null);
    setErrorMessage("");
    setErrorCode(null);
    setStatus("idle");
  }, []);

  const loadPage = useCallback(
    async (targetPage, { append = false } = {}) => {
      if (!userId || !enabled) return;

      const requestId = ++requestIdRef.current;
      setStatus(append ? "loadingMore" : "loading");
      setErrorMessage("");
      setErrorCode(null);

      try {
        const data = await fetchUserPosts(userId, {
          page: targetPage,
          size: PROFILE_POSTS_PAGE_SIZE,
          statusFilter,
        });
        if (requestId !== requestIdRef.current) return;

        const nextItems = data?.items || [];
        setItems((prev) => (append ? [...prev, ...nextItems] : nextItems));
        setMeta(data?.meta || null);
        setPage(targetPage);
        setStatus("ready");
      } catch (error) {
        if (requestId !== requestIdRef.current) return;

        if (error?.code === 401) {
          showSessionExpired(error?.message);
          return;
        }

        setStatus("error");
        setErrorCode(error?.code || 500);
        setErrorMessage(error?.message || "Không tải được bài viết.");
      }
    },
    [enabled, showSessionExpired, statusFilter, userId]
  );

  useEffect(() => {
    reset();
    if (enabled && userId) {
      loadPage(0, { append: false });
    }
  }, [enabled, userId, statusFilter, reset, loadPage]);

  const loadMore = useCallback(() => {
    if (status === "loadingMore" || !meta?.hasNext) return;
    loadPage(page + 1, { append: true });
  }, [loadPage, meta?.hasNext, page, status]);

  const retry = useCallback(() => {
    if (items.length > 0) {
      loadPage(page, { append: false });
      return;
    }
    loadPage(0, { append: false });
  }, [items.length, loadPage, page]);

  const refetch = useCallback(() => {
    loadPage(0, { append: false });
  }, [loadPage]);

  const removeItem = useCallback((targetPostId) => {
    setItems((prev) => prev.filter((item) => item.postId !== targetPostId));
    setMeta((prev) =>
      prev
        ? {
            ...prev,
            totalElements: Math.max(0, (prev.totalElements || 0) - 1),
          }
        : prev
    );
  }, []);

  const patchSaved = useCallback((targetPostId, saved) => {
    setItems((prev) =>
      prev.map((item) =>
        item.postId === targetPostId ? { ...item, savedByMe: saved } : item
      )
    );
  }, []);

  return {
    items,
    meta,
    status,
    errorMessage,
    errorCode,
    isInitialLoading: status === "loading" && items.length === 0,
    isLoadingMore: status === "loadingMore",
    hasNext: Boolean(meta?.hasNext),
    loadMore,
    retry,
    refetch,
    removeItem,
    patchSaved,
  };
}
