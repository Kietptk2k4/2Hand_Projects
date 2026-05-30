import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { fetchUserRelations } from "../api/relationsApi";
import { DEFAULT_USER_DISPLAY_NAME } from "../constants/socialUiStrings";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

const RELATIONS_PAGE_SIZE = 20;

export function useUserRelations(targetUserId, relationType, { enabled = true } = {}) {
  const { showSessionExpired } = useAuthSession();
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

  const loadPage = useCallback(
    async (targetPage, { append = false } = {}) => {
      if (!targetUserId || !relationType || !enabled) return;

      const requestId = ++requestIdRef.current;
      setStatus(append ? "loadingMore" : "loading");
      setErrorMessage("");
      setErrorCode(null);

      try {
        const data = await fetchUserRelations(targetUserId, {
          type: relationType,
          page: targetPage,
          size: RELATIONS_PAGE_SIZE,
        });
        if (requestId !== requestIdRef.current) return;

        const nextItems = data?.items || [];
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
          showSessionExpired(error?.message);
          return;
        }

        setStatus("error");
        setErrorCode(error?.code || 500);
        setErrorMessage(error?.message || "Không tải được danh sách.");
      }
    },
    [enabled, relationType, showSessionExpired, targetUserId]
  );

  useEffect(() => {
    reset();
    if (enabled && targetUserId && relationType) {
      loadPage(0, { append: false });
    }
  }, [enabled, targetUserId, relationType, reset, loadPage]);

  const loadMore = useCallback(() => {
    if (status === "loadingMore" || !meta?.hasNext) return;
    loadPage(page + 1, { append: true });
  }, [loadPage, meta?.hasNext, page, status]);

  const retry = useCallback(() => {
    loadPage(items.length > 0 ? page : 0, { append: false });
  }, [items.length, loadPage, page]);

  const filteredItems = useMemo(() => {
    const query = searchQuery.trim().toLowerCase();
    if (!query) return items;
    return items.filter((item) =>
      (item.displayName || DEFAULT_USER_DISPLAY_NAME).toLowerCase().includes(query)
    );
  }, [items, searchQuery]);

  return {
    items: filteredItems,
    rawCount: items.length,
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
