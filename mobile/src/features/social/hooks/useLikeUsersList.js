import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { fetchCommentLikers, fetchPostLikers } from "../api/likesApi";
import { DEFAULT_USER_DISPLAY_NAME } from "../constants/socialUiStrings";
import { handleSocialQueryError } from "../utils/handleSocialQueryError";

const LIKES_PAGE_SIZE = 20;

function mapLikerItem(item) {
  return {
    userId: item?.user_id ?? item?.userId ?? "",
    displayName: item?.display_name ?? item?.displayName ?? DEFAULT_USER_DISPLAY_NAME,
    avatarUrl: item?.avatar_url ?? item?.avatarUrl ?? "",
    likedAt: item?.liked_at ?? item?.likedAt ?? null,
  };
}

export function useLikeUsersList(targetType, targetId, { enabled = true } = {}) {
  const [searchQuery, setSearchQuery] = useState("");
  const [items, setItems] = useState([]);
  const [meta, setMeta] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [errorCode, setErrorCode] = useState(null);
  const [page, setPage] = useState(0);
  const requestIdRef = useRef(0);

  const reset = useCallback(() => {
    setSearchQuery("");
    setItems([]);
    setMeta(null);
    setPage(0);
    setErrorMessage("");
    setErrorCode(null);
    setStatus("idle");
  }, []);

  const fetchPage = useCallback(
    async (targetPage, { append = false } = {}) => {
      if (!targetType || !targetId || !enabled) return;

      const requestId = ++requestIdRef.current;
      setStatus(append ? "loadingMore" : "loading");
      setErrorMessage("");
      setErrorCode(null);

      const fetcher = targetType === "comment" ? fetchCommentLikers : fetchPostLikers;

      try {
        const data = await fetcher(targetId, { page: targetPage, size: LIKES_PAGE_SIZE });
        if (requestId !== requestIdRef.current) return;

        const nextItems = (data?.items || []).map(mapLikerItem);
        setItems((prev) => {
          if (!append) return nextItems;
          const seen = new Set(prev.map((item) => item.userId));
          const merged = [...prev];
          nextItems.forEach((item) => {
            if (!seen.has(item.userId)) {
              seen.add(item.userId);
              merged.push(item);
            }
          });
          return merged;
        });
        setMeta(data?.meta || null);
        setPage(targetPage);
        setStatus("ready");
      } catch (error) {
        if (requestId !== requestIdRef.current) return;

        if (error?.code === 401) {
          await handleSocialQueryError(error);
          return;
        }

        setStatus("error");
        setErrorCode(error?.code || 500);
        setErrorMessage(error?.message || "Không tải được danh sách.");
      }
    },
    [enabled, targetId, targetType]
  );

  useEffect(() => {
    reset();
    if (enabled && targetType && targetId) {
      fetchPage(0, { append: false });
    }
  }, [enabled, targetType, targetId, reset, fetchPage]);

  const loadMore = useCallback(() => {
    if (status === "loadingMore" || !meta?.hasNext) return;
    fetchPage(page + 1, { append: true });
  }, [fetchPage, meta?.hasNext, page, status]);

  const retry = useCallback(() => {
    fetchPage(items.length > 0 ? page : 0, { append: false });
  }, [fetchPage, items.length, page]);

  const filteredItems = useMemo(() => {
    const query = searchQuery.trim().toLowerCase();
    if (!query) return items;
    return items.filter((item) =>
      (item.displayName || DEFAULT_USER_DISPLAY_NAME).toLowerCase().includes(query)
    );
  }, [items, searchQuery]);

  return {
    items: filteredItems,
    meta,
    status,
    errorMessage,
    errorCode,
    searchQuery,
    setSearchQuery,
    isInitialLoading: status === "loading" && items.length === 0,
    isLoadingMore: status === "loadingMore",
    hasNext: Boolean(meta?.hasNext),
    loadMore,
    retry,
  };
}
