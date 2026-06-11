import { useCallback, useEffect, useRef, useState } from "react";
import { router } from "expo-router";
import { fetchFollowingFeed, fetchGlobalFeed } from "../api/feedApi";
import { FEED_TABS, FEED_PAGE_SIZE } from "../constants/feedTabs";
import { clearSessionTokens } from "../../../services/auth/tokenStorage";
import { ROUTES } from "../../../shared/constants/routes";

const FETCH_BY_TAB = {
  [FEED_TABS.GLOBAL]: fetchGlobalFeed,
  [FEED_TABS.FOLLOWING]: fetchFollowingFeed,
};

async function handleSessionExpired() {
  await clearSessionTokens();
  router.replace(ROUTES.login);
}

export function useFeed(activeTab) {
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
          await handleSessionExpired();
          return;
        }

        setStatus("error");
        setErrorMessage(error?.message || "Không tải được feed. Vui lòng thử lại.");
      }
    },
    [activeTab]
  );

  useEffect(() => {
    resetFeed();
    loadPage(0, { append: false });
  }, [activeTab, resetFeed, loadPage]);

  const loadMore = useCallback(() => {
    if (status === "loadingMore" || status === "loading" || !meta?.hasNext) return;
    loadPage(page + 1, { append: true });
  }, [loadPage, meta?.hasNext, page, status]);

  const retry = useCallback(() => {
    if (items.length > 0) {
      loadPage(page, { append: false });
      return;
    }
    loadPage(0, { append: false });
  }, [items.length, loadPage, page]);

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
  };
}
