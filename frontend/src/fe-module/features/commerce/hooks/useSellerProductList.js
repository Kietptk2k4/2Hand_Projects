import { useCallback, useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { fetchSellerProductList } from "../api/sellerProductApi";
import {
  mapSellerProductApiError,
  PAGE_SIZE,
  PRODUCT_STATUSES,
  STATUS_TABS,
} from "../constants/sellerProductConstants";
import { mapSellerProductListResponse } from "../utils/sellerProductMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

function isNoShopError(error) {
  const code = String(error?.code ?? "");
  return code.includes("409-SELLER-SHOP") || code.includes("404-SHOP");
}

function parseStatusParam(value) {
  if (!value) return null;
  if (!PRODUCT_STATUSES.includes(value)) return "__invalid__";
  return value;
}

export function useSellerProductList() {
  const { showSessionExpired } = useAuthSession();
  const [searchParams, setSearchParams] = useSearchParams();
  const statusFilter = parseStatusParam(searchParams.get("status"));
  const qParam = searchParams.get("q") || "";

  const [page, setPage] = useState(1);
  const [searchInput, setSearchInput] = useState(qParam);
  const [debouncedQ, setDebouncedQ] = useState(qParam);
  const [items, setItems] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [summary, setSummary] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [noShop, setNoShop] = useState(false);
  const requestIdRef = useRef(0);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedQ(searchInput.trim());
    }, 300);
    return () => clearTimeout(timer);
  }, [searchInput]);

  useEffect(() => {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      if (debouncedQ) next.set("q", debouncedQ);
      else next.delete("q");
      return next;
    });
    setPage(1);
  }, [debouncedQ, setSearchParams]);

  const load = useCallback(
    async (targetPage = page) => {
      if (statusFilter === "__invalid__") {
        setItems([]);
        setStatus("error");
        setErrorMessage("Bộ lọc trạng thái không hợp lệ.");
        return;
      }

      const requestId = ++requestIdRef.current;
      setStatus("loading");
      setErrorMessage("");
      setNoShop(false);

      try {
        const raw = await fetchSellerProductList({
          page: targetPage,
          limit: PAGE_SIZE,
          status: statusFilter ?? undefined,
          q: debouncedQ || undefined,
        });
        if (requestId !== requestIdRef.current) return;

        const data = mapSellerProductListResponse(raw);
        setItems(data.items);
        setPagination(data.pagination);
        setSummary(data.summary);
        setPage(targetPage);
        setStatus("ready");
      } catch (error) {
        if (requestId !== requestIdRef.current) return;

        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          return;
        }

        if (isNoShopError(error)) {
          setNoShop(true);
          setItems([]);
          setStatus("ready");
          setErrorMessage(mapSellerProductApiError(error));
          return;
        }

        setStatus("error");
        setErrorMessage(mapSellerProductApiError(error));
      }
    },
    [debouncedQ, page, showSessionExpired, statusFilter],
  );

  useEffect(() => {
    load(1);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [statusFilter, debouncedQ]);

  const changeStatusFilter = useCallback(
    (nextStatus) => {
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev);
        if (!nextStatus) next.delete("status");
        else next.set("status", nextStatus);
        return next;
      });
      setPage(1);
    },
    [setSearchParams],
  );

  const goToPage = useCallback(
    (nextPage) => {
      if (nextPage < 1 || (pagination && nextPage > pagination.totalPages)) return;
      load(nextPage);
    },
    [load, pagination],
  );

  const reload = useCallback(() => {
    load(page);
  }, [load, page]);

  const activeTabId =
    STATUS_TABS.find((tab) => tab.status === statusFilter)?.id || "all";

  const total = pagination?.total ?? 0;
  const rangeStart = total === 0 ? 0 : (page - 1) * PAGE_SIZE + 1;
  const rangeEnd = Math.min(page * PAGE_SIZE, total);

  return {
    items,
    summary,
    pagination,
    page,
    statusFilter: statusFilter === "__invalid__" ? null : statusFilter,
    activeTabId,
    changeStatusFilter,
    goToPage,
    searchInput,
    setSearchInput,
    status,
    errorMessage,
    noShop,
    isLoading: status === "loading",
    isEmpty: status === "ready" && items.length === 0 && !noShop,
    rangeStart,
    rangeEnd,
    total,
    reload,
  };
}
