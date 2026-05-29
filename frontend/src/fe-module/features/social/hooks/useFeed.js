import { useCallback, useEffect, useRef, useState } from "react";
import { fetchFollowingFeed, fetchGlobalFeed } from "../api/feedApi";
import { FEED_TABS, FEED_PAGE_SIZE } from "../constants/feedTabs";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

const FETCH_BY_TAB = {
  [FEED_TABS.GLOBAL]: fetchGlobalFeed,
  [FEED_TABS.FOLLOWING]: fetchFollowingFeed,
};

export function useFeed(activeTab) {
  const { showSessionExpired } = useAuthSession();
  const [page, setPage] = useState(0);
  const [items, setItems] = useState([]);
  const [meta, setMeta] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const requestIdRef = useRef(0);

  const resetFeed = useCallback(() => {
    setPage(0);
    setItems([]);
    setMeta(null);
    setErrorMessage("");
    setStatus("idle");
  }, []);

  const loadPage = useCallback(
    async (targetPage, { append = false } = {}) => {
      const fetchFn = FETCH_BY_TAB[activeTab];
      if (!fetchFn) return;

      const requestId = ++requestIdRef.current;
      setStatus(append ? "loadingMore" : "loading");
      setErrorMessage("");

      try {
        const data = await fetchFn({ page: targetPage, size: FEED_PAGE_SIZE });
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
        setErrorMessage(error?.message || "Không tải được feed. Vui lòng thử lại.");
      }
    },
    [activeTab, showSessionExpired]
  );

  useEffect(() => {
    resetFeed();
    loadPage(0, { append: false });
  }, [activeTab, resetFeed, loadPage]);

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
  };
}
