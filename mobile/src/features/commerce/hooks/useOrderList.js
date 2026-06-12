import { useCallback, useEffect, useRef, useState } from "react";
import { fetchOrderList } from "../api/orderListApi";
import { ORDER_STATUS_FILTERS, PAGE_SIZE, VALID_ORDER_STATUSES } from "../constants/orderListConstants";
import { mapOrderListResponse } from "../utils/orderListMapper";

function parseStatusFilter(value) {
  if (!value) return null;
  if (!VALID_ORDER_STATUSES.includes(value)) return "__invalid__";
  return value;
}

export function useOrderList() {
  const [statusFilter, setStatusFilter] = useState(null);
  const parsedStatusFilter = parseStatusFilter(statusFilter);

  const [page, setPage] = useState(1);
  const [orders, setOrders] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const requestIdRef = useRef(0);

  const loadPage = useCallback(
    async (targetPage, { append = false, statusValue = parsedStatusFilter } = {}) => {
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

        setStatus("error");
        setErrorMessage(error?.message || "Không tải được danh sách đơn hàng. Vui lòng thử lại.");
      }
    },
    [parsedStatusFilter],
  );

  const resetAndFetch = useCallback(
    (statusValue = parsedStatusFilter) => {
      setPage(1);
      setOrders([]);
      setPagination(null);
      setErrorMessage("");
      requestIdRef.current += 1;
      loadPage(1, { append: false, statusValue });
    },
    [loadPage, parsedStatusFilter],
  );

  useEffect(() => {
    resetAndFetch(parsedStatusFilter);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [parsedStatusFilter]);

  const changeStatusFilter = useCallback((nextStatus) => {
    setStatusFilter(nextStatus);
  }, []);

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
    ORDER_STATUS_FILTERS.find((filter) => filter.status === parsedStatusFilter)?.id || "all";

  return {
    orders,
    statusFilter: parsedStatusFilter === "__invalid__" ? null : parsedStatusFilter,
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
