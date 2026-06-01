import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { fetchSellerShipmentList } from "../api/sellerShipmentApi";
import {
  PAGE_SIZE,
  STATUS_TABS,
  mapSellerShipmentApiError,
} from "../constants/sellerShipmentConstants";
import { mapSellerShipmentListResponse } from "../utils/sellerShipmentMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

function isNoShopError(error) {
  return String(error?.code ?? "") === "COMMERCE-409-SELLER-SHOP";
}

function parseStatusTab(searchParams) {
  const tab = searchParams.get("tab") || "all";
  const found = STATUS_TABS.find((t) => t.id === tab);
  return found || STATUS_TABS[0];
}

export function useSellerShipmentList() {
  const { showSessionExpired } = useAuthSession();
  const [searchParams, setSearchParams] = useSearchParams();
  const activeTab = parseStatusTab(searchParams);
  const statusFilter = activeTab.status;

  const [page, setPage] = useState(1);
  const [items, setItems] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [statusCounts, setStatusCounts] = useState({});
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [noShop, setNoShop] = useState(false);
  const [clientSearch, setClientSearch] = useState("");
  const requestIdRef = useRef(0);

  const serverQuery = clientSearch.trim() || undefined;

  const loadPage = useCallback(
    async (targetPage, { statusValue = statusFilter, qValue = serverQuery } = {}) => {
      const requestId = ++requestIdRef.current;
      setIsLoading(true);
      setErrorMessage("");
      setNoShop(false);

      try {
        const raw = await fetchSellerShipmentList({
          page: targetPage,
          limit: PAGE_SIZE,
          status: statusValue ?? undefined,
          q: qValue,
        });

        if (requestId !== requestIdRef.current) return;

        const data = mapSellerShipmentListResponse(raw);
        setItems(data.items);
        setPagination(data.pagination);
        setStatusCounts(data.statusCounts);
        setPage(targetPage);
      } catch (error) {
        if (requestId !== requestIdRef.current) return;

        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          return;
        }

        if (isNoShopError(error)) {
          setNoShop(true);
          setItems([]);
          setPagination(null);
          return;
        }

        setErrorMessage(mapSellerShipmentApiError(error));
      } finally {
        if (requestId === requestIdRef.current) {
          setIsLoading(false);
        }
      }
    },
    [serverQuery, showSessionExpired, statusFilter],
  );

  useEffect(() => {
    setPage(1);
    loadPage(1);
  }, [statusFilter, serverQuery, loadPage]);

  const changeStatusTab = useCallback(
    (tabId) => {
      const next = new URLSearchParams(searchParams);
      if (tabId === "all") next.delete("tab");
      else next.set("tab", tabId);
      setSearchParams(next, { replace: true });
    },
    [searchParams, setSearchParams],
  );

  const goToPage = useCallback(
    (targetPage) => {
      if (!pagination) return;
      if (targetPage < 1 || targetPage > pagination.totalPages) return;
      loadPage(targetPage);
    },
    [loadPage, pagination],
  );

  const retry = useCallback(() => {
    loadPage(page);
  }, [loadPage, page]);

  const refresh = useCallback(() => {
    loadPage(page);
  }, [loadPage, page]);

  const totalItems = pagination?.totalItems ?? 0;
  const totalPages = pagination?.totalPages ?? 1;
  const rangeStart = totalItems === 0 ? 0 : (page - 1) * PAGE_SIZE + 1;
  const rangeEnd = Math.min(page * PAGE_SIZE, totalItems);

  const isEmpty = !isLoading && !errorMessage && !noShop && totalItems === 0 && !serverQuery;
  const isFilterEmpty =
    !isLoading && !errorMessage && !noShop && totalItems === 0 && Boolean(statusFilter);
  const isSearchEmpty =
    !isLoading && !errorMessage && !noShop && totalItems === 0 && Boolean(serverQuery);

  const tabCounts = useMemo(() => {
    const counts = { all: 0 };
    for (const tab of STATUS_TABS) {
      if (tab.status) {
        counts[tab.id] = statusCounts[tab.status] ?? 0;
        counts.all += counts[tab.id];
      }
    }
    return counts;
  }, [statusCounts]);

  return {
    items,
    activeTabId: activeTab.id,
    statusFilter,
    changeStatusTab,
    page,
    pagination,
    totalItems,
    totalPages,
    rangeStart,
    rangeEnd,
    isLoading,
    errorMessage,
    noShop,
    isEmpty,
    isFilterEmpty,
    isSearchEmpty,
    goToPage,
    retry,
    refresh,
    clientSearch,
    setClientSearch,
    tabCounts,
    hasNext: Boolean(pagination?.hasNext),
    loadMore: () => {
      if (pagination?.hasNext) goToPage(page + 1);
    },
  };
}
