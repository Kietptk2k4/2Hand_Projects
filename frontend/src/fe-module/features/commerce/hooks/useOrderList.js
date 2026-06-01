import { useCallback, useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { fetchOrderList } from "../api/orderListApi";
import { ORDER_STATUS_FILTERS, PAGE_SIZE, VALID_ORDER_STATUSES } from "../constants/orderListConstants";
import { mapOrderListResponse } from "../utils/orderListMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

function parseStatusParam(value) {
  if (!value) return null;
  if (!VALID_ORDER_STATUSES.includes(value)) return "__invalid__";
  return value;
}

export function useOrderList() {
  const { showSessionExpired } = useAuthSession();
  const [searchParams, setSearchParams] = useSearchParams();
  const statusFilter = parseStatusParam(searchParams.get("status"));

  const [page, setPage] = useState(1);
  const [orders, setOrders] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const requestIdRef = useRef(0);

  const loadPage = useCallback(
    async (targetPage, { append = false, statusValue = statusFilter } = {}) => {
      if (statusValue === "__invalid__") {
        setOrders([]);
        setPagination(null);
        setStatus("error");
        setErrorMessage("Bộ lọc trạng thái không hợp lệ.");
        return;
      }

      const requestId = ++requestIdRef.current;
      setStatus(append ? "loadingMore" : "loading");
      setErrorMessage("");

      try {
        const raw = await fetchOrderList({
          page: targetPage,
          limit: PAGE_SIZE,
          status: statusValue ?? undefined,
        });
        if (requestId !== requestIdRef.current) return;

        const data = mapOrderListResponse(raw);
        setOrders((prev) => (append ? [...prev, ...data.orders] : data.orders));
        setPagination(data.pagination);
        setPage(targetPage);
        setStatus("ready");
      } catch (error) {
        if (requestId !== requestIdRef.current) return;

        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          return;
        }

        setStatus("error");
        setErrorMessage(error?.message || "Không tải được danh sách đơn hàng. Vui lòng thử lại.");
      }
    },
    [showSessionExpired, statusFilter]
  );

  const resetAndFetch = useCallback(
    (statusValue = statusFilter) => {
      setPage(1);
      setOrders([]);
      setPagination(null);
      setErrorMessage("");
      requestIdRef.current += 1;
      loadPage(1, { append: false, statusValue });
    },
    [loadPage, statusFilter]
  );

  useEffect(() => {
    resetAndFetch(statusFilter);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [statusFilter]);

  const changeStatusFilter = useCallback(
    (nextStatus) => {
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev);
        if (!nextStatus) {
          next.delete("status");
        } else {
          next.set("status", nextStatus);
        }
        return next;
      });
    },
    [setSearchParams]
  );

  const loadMore = useCallback(() => {
    if (status === "loadingMore" || !pagination?.hasNext) return;
    loadPage(page + 1, { append: true });
  }, [loadPage, page, pagination?.hasNext, status]);

  const retry = useCallback(() => {
    if (orders.length > 0) {
      loadPage(page, { append: false });
      return;
    }
    resetAndFetch();
  }, [loadPage, orders.length, page, resetAndFetch]);

  const activeFilterId =
    ORDER_STATUS_FILTERS.find((filter) => filter.status === statusFilter)?.id || "all";

  return {
    orders,
    statusFilter: statusFilter === "__invalid__" ? null : statusFilter,
    activeFilterId,
    changeStatusFilter,
    pagination,
    status,
    errorMessage,
    isInitialLoading: status === "loading" && orders.length === 0,
    isLoadingMore: status === "loadingMore",
    hasNext: Boolean(pagination?.hasNext),
    totalItems: pagination?.totalItems ?? 0,
    isEmpty: status === "ready" && orders.length === 0,
    loadMore,
    retry,
  };
}
