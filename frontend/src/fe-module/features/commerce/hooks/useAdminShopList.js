import { useCallback, useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { fetchAdminShopList } from "../api/adminShopModerationApi";
import {
  PAGE_SIZE,
  SHOP_STATUS_FILTER_TABS,
  mapAdminShopModerationApiError,
} from "../constants/adminShopModerationConstants";
import { mapAdminShopListResponse } from "../utils/adminShopModerationMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

function isForbiddenError(error) {
  return String(error?.code ?? "") === "COMMERCE-403";
}

function parseStatusTab(searchParams) {
  const tab = searchParams.get("status") || "all";
  const found = SHOP_STATUS_FILTER_TABS.find((t) => t.id === tab);
  return found || SHOP_STATUS_FILTER_TABS[0];
}

export function useAdminShopList() {
  const { showSessionExpired } = useAuthSession();
  const [searchParams, setSearchParams] = useSearchParams();

  const activeStatusTab = parseStatusTab(searchParams);
  const statusFilter = activeStatusTab.status;
  const sort = searchParams.get("sort") || "NEWEST";
  const serverSearch = searchParams.get("q") || "";

  const [page, setPage] = useState(1);
  const [items, setItems] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [forbidden, setForbidden] = useState(false);
  const [searchInput, setSearchInput] = useState(serverSearch);
  const requestIdRef = useRef(0);

  const loadPage = useCallback(
    async (
      targetPage,
      {
        statusValue = statusFilter,
        qValue = serverSearch,
        sortValue = sort,
      } = {},
    ) => {
      const requestId = ++requestIdRef.current;
      setIsLoading(true);
      setErrorMessage("");
      setForbidden(false);

      try {
        const raw = await fetchAdminShopList({
          page: targetPage,
          limit: PAGE_SIZE,
          status: statusValue ?? undefined,
          q: qValue || undefined,
          sort: sortValue,
        });

        if (requestId !== requestIdRef.current) return;

        const data = mapAdminShopListResponse(raw);
        setItems(data.items);
        setPagination(data.pagination);
        setPage(targetPage);
      } catch (error) {
        if (requestId !== requestIdRef.current) return;

        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          return;
        }

        if (isForbiddenError(error)) {
          setForbidden(true);
          setItems([]);
          setPagination(null);
          return;
        }

        setErrorMessage(mapAdminShopModerationApiError(error));
      } finally {
        if (requestId === requestIdRef.current) {
          setIsLoading(false);
        }
      }
    },
    [serverSearch, showSessionExpired, sort, statusFilter],
  );

  useEffect(() => {
    setPage(1);
    loadPage(1);
  }, [statusFilter, serverSearch, sort, loadPage]);

  const changeStatusTab = useCallback(
    (tabId) => {
      const next = new URLSearchParams(searchParams);
      if (tabId === "all") next.delete("status");
      else next.set("status", tabId);
      setSearchParams(next, { replace: true });
    },
    [searchParams, setSearchParams],
  );

  const changeSort = useCallback(
    (nextSort) => {
      const next = new URLSearchParams(searchParams);
      if (nextSort === "NEWEST") next.delete("sort");
      else next.set("sort", nextSort);
      setSearchParams(next, { replace: true });
    },
    [searchParams, setSearchParams],
  );

  const applySearch = useCallback(() => {
    const next = new URLSearchParams(searchParams);
    const trimmed = searchInput.trim();
    if (trimmed) next.set("q", trimmed);
    else next.delete("q");
    setSearchParams(next, { replace: true });
  }, [searchInput, searchParams, setSearchParams]);

  const goToPage = useCallback(
    (targetPage) => {
      if (!pagination) return;
      if (targetPage < 1 || targetPage > pagination.totalPages) return;
      loadPage(targetPage);
    },
    [loadPage, pagination],
  );

  const retry = useCallback(() => loadPage(page), [loadPage, page]);
  const refresh = useCallback(() => loadPage(page), [loadPage, page]);

  const totalItems = pagination?.totalItems ?? 0;
  const totalPages = pagination?.totalPages ?? 1;
  const rangeStart = totalItems === 0 ? 0 : (page - 1) * PAGE_SIZE + 1;
  const rangeEnd = Math.min(page * PAGE_SIZE, totalItems);

  return {
    items,
    activeStatusTabId: activeStatusTab.id,
    changeStatusTab,
    sort,
    changeSort,
    page,
    pagination,
    totalItems,
    totalPages,
    rangeStart,
    rangeEnd,
    isLoading,
    errorMessage,
    forbidden,
    isEmpty: !isLoading && !errorMessage && !forbidden && totalItems === 0,
    goToPage,
    retry,
    refresh,
    searchInput,
    setSearchInput,
    applySearch,
  };
}
