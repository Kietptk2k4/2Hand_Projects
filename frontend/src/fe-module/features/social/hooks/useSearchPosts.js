import { useCallback, useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { searchPosts } from "../api/searchPostsApi";
import { SEARCH_POSTS_PAGE_SIZE } from "../constants/searchPostsConstants";
import { addSearchHistory } from "../utils/searchHistoryStorage";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

export function useSearchPosts() {
  const [searchParams] = useSearchParams();
  const rawQ = searchParams.get("q") ?? "";
  const q = rawQ.trim();

  const { showSessionExpired } = useAuthSession();
  const [page, setPage] = useState(0);
  const [items, setItems] = useState([]);
  const [keyword, setKeyword] = useState("");
  const [meta, setMeta] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const requestIdRef = useRef(0);

  const loadPage = useCallback(
    async (targetPage, { append = false, searchQ = q } = {}) => {
      const trimmed = searchQ.trim();
      if (!trimmed) {
        setItems([]);
        setMeta(null);
        setKeyword("");
        setStatus("idle");
        setErrorMessage("");
        return;
      }

      const requestId = ++requestIdRef.current;
      setStatus(append ? "loadingMore" : "loading");
      setErrorMessage("");

      try {
        const data = await searchPosts({
          q: trimmed,
          page: targetPage,
          size: SEARCH_POSTS_PAGE_SIZE,
        });
        if (requestId !== requestIdRef.current) return;

        const nextItems = data?.items || [];
        setItems((prev) => (append ? [...prev, ...nextItems] : nextItems));
        setMeta(data?.meta || null);
        setKeyword(data?.keyword || trimmed);
        setPage(targetPage);
        setStatus("ready");
        if (!append) {
          addSearchHistory(data?.keyword || trimmed);
        }
      } catch (error) {
        if (requestId !== requestIdRef.current) return;

        if (error?.code === 401) {
          showSessionExpired(error?.message);
          return;
        }

        setStatus("error");
        setErrorMessage(error?.message || "Không tìm được bài viết.");
      }
    },
    [q, showSessionExpired]
  );

  useEffect(() => {
    if (!q) {
      setItems([]);
      setMeta(null);
      setKeyword("");
      setStatus("idle");
      setErrorMessage("");
      return;
    }
    loadPage(0, { append: false, searchQ: q });
  }, [q, loadPage]);

  const loadMore = useCallback(() => {
    if (status === "loadingMore" || !meta?.hasNext || !q) return;
    loadPage(page + 1, { append: true });
  }, [loadPage, meta?.hasNext, page, q, status]);

  const retry = useCallback(() => {
    loadPage(items.length > 0 ? page : 0, { append: false });
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

  const patchLiked = useCallback((targetPostId, liked, likeCount) => {
    setItems((prev) =>
      prev.map((item) =>
        item.postId === targetPostId ? { ...item, likedByMe: liked, likeCount } : item
      )
    );
  }, []);

  return {
    q,
    keyword,
    items,
    meta,
    status,
    errorMessage,
    isInitialLoading: status === "loading" && items.length === 0,
    isLoadingMore: status === "loadingMore",
    hasNext: Boolean(meta?.hasNext),
    totalElements: meta?.totalElements ?? 0,
    loadMore,
    retry,
    refetch,
    removeItem,
    patchSaved,
    patchLiked,
  };
}
