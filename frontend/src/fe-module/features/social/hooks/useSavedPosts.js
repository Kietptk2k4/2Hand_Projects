import { useCallback, useEffect, useRef, useState } from "react";
import { fetchSavedPosts } from "../api/savedPostsApi";
import { SAVED_POSTS_PAGE_SIZE } from "../constants/savedPostsConstants";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

export function useSavedPosts() {
  const { showSessionExpired } = useAuthSession();
  const [page, setPage] = useState(0);
  const [items, setItems] = useState([]);
  const [meta, setMeta] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const requestIdRef = useRef(0);

  const loadPage = useCallback(
    async (targetPage, { append = false } = {}) => {
      const requestId = ++requestIdRef.current;
      setStatus(append ? "loadingMore" : "loading");
      setErrorMessage("");

      try {
        const data = await fetchSavedPosts({
          page: targetPage,
          size: SAVED_POSTS_PAGE_SIZE,
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
        setErrorMessage(error?.message || "Không tải được danh sách đã lưu.");
      }
    },
    [showSessionExpired]
  );

  useEffect(() => {
    loadPage(0, { append: false });
  }, [loadPage]);

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

  const removeItem = useCallback((postId) => {
    setItems((prev) => prev.filter((item) => item.postId !== postId));
    setMeta((prev) =>
      prev
        ? {
            ...prev,
            totalElements: Math.max(0, (prev.totalElements || 0) - 1),
          }
        : prev
    );
  }, []);

  return {
    items,
    meta,
    status,
    errorMessage,
    isInitialLoading: status === "loading" && items.length === 0,
    isLoadingMore: status === "loadingMore",
    hasNext: Boolean(meta?.hasNext),
    loadMore,
    retry,
    refetch,
    removeItem,
  };
}
