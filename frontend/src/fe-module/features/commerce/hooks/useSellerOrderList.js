import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { fetchSellerOrderList } from "../api/sellerOrderApi";
import {
  PAGE_SIZE,
  STATUS_TABS,
  VALID_ITEM_STATUSES,
  VALID_SHIPMENT_STATUSES,
  mapSellerOrderApiError,
} from "../constants/sellerOrderConstants";
import { mapSellerOrderListResponse } from "../utils/sellerOrderMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

function parseStatusParam(value) {
  if (!value) return null;
  if (!VALID_ITEM_STATUSES.includes(value)) return "__invalid__";
  return value;
}

function parseShipmentParam(value) {
  if (!value) return null;
  if (!VALID_SHIPMENT_STATUSES.includes(value)) return "__invalid__";
  return value;
}

/**
 * API ViewSellerOrders không có param `q`.
 * Client search chỉ lọc trên items của trang hiện tại (theo order_id / product_name_snapshot).
 */
export function useSellerOrderList() {
  const { showSessionExpired } = useAuthSession();
  const [searchParams, setSearchParams] = useSearchParams();

  const statusFilter = parseStatusParam(searchParams.get("status"));
  const shipmentFilterRaw = parseShipmentParam(searchParams.get("shipment_status"));
  const shipmentStatusFilter = shipmentFilterRaw === "__invalid__" ? null : shipmentFilterRaw;

  const [page, setPage] = useState(1);
  const [items, setItems] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [clientSearch, setClientSearch] = useState("");
  const [pendingCount, setPendingCount] = useState(0);
  const requestIdRef = useRef(0);

  const loadPage = useCallback(
    async (targetPage, { statusValue = statusFilter, shipmentValue = shipmentFilterRaw } = {}) => {
      if (statusValue === "__invalid__" || shipmentValue === "__invalid__") {
        setItems([]);
        setPagination(null);
        setErrorMessage("Bộ lọc không hợp lệ.");
        setIsLoading(false);
        return;
      }

      const requestId = ++requestIdRef.current;
      setIsLoading(true);
      setErrorMessage("");

      try {
        const raw = await fetchSellerOrderList({
          page: targetPage,
          limit: PAGE_SIZE,
          status: statusValue ?? undefined,
          shipmentStatus: shipmentValue ?? undefined,
        });

        if (requestId !== requestIdRef.current) return;

        const data = mapSellerOrderListResponse(raw);
        setItems(data.items);
        setPagination(data.pagination);
        setPendingCount(data.pendingCount ?? 0);
        setPage(targetPage);
      } catch (error) {
        if (requestId !== requestIdRef.current) return;

        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          return;
        }

        setErrorMessage(mapSellerOrderApiError(error));
      } finally {
        if (requestId === requestIdRef.current) {
          setIsLoading(false);
        }
      }
    },
    [shipmentFilterRaw, showSessionExpired, statusFilter],
  );

  useEffect(() => {
    setPage(1);
    loadPage(1);
  }, [statusFilter, shipmentFilterRaw, loadPage]);

  const filteredItems = useMemo(() => {
    const needle = clientSearch.trim().toLowerCase();
    if (!needle) return items;

    return items.filter((item) => {
      const orderId = (item.orderId || "").toLowerCase();
      const name = (item.productNameSnapshot || "").toLowerCase();
      const shortId = orderId.replace(/-/g, "").slice(-6);
      return orderId.includes(needle) || name.includes(needle) || shortId.includes(needle);
    });
  }, [clientSearch, items]);

  const changeStatusFilter = useCallback(
    (nextStatus) => {
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev);
        if (!nextStatus) next.delete("status");
        else next.set("status", nextStatus);
        return next;
      });
    },
    [setSearchParams],
  );

  const changeShipmentFilter = useCallback(
    (nextShipment) => {
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev);
        if (!nextShipment) next.delete("shipment_status");
        else next.set("shipment_status", nextShipment);
        return next;
      });
    },
    [setSearchParams],
  );

  const goToPage = useCallback(
    (nextPage) => {
      if (!pagination) return;
      if (nextPage < 1 || nextPage > pagination.totalPages) return;
      loadPage(nextPage);
    },
    [loadPage, pagination],
  );

  const retry = useCallback(() => {
    loadPage(page);
  }, [loadPage, page]);

  const activeTabId =
    STATUS_TABS.find((tab) => tab.status === statusFilter)?.id || "all";

  const totalItems = pagination?.totalItems ?? 0;
  const totalPages = pagination?.totalPages ?? 1;
  const rangeStart = totalItems === 0 ? 0 : (page - 1) * PAGE_SIZE + 1;
  const rangeEnd = Math.min(page * PAGE_SIZE, totalItems);

  const hasServerFilter = Boolean(
    (statusFilter && statusFilter !== "__invalid__") || shipmentStatusFilter,
  );

  const isEmpty =
    !isLoading && !errorMessage && items.length === 0 && !clientSearch.trim();

  const isFilterEmpty =
    !isLoading &&
    !errorMessage &&
    items.length === 0 &&
    hasServerFilter &&
    !clientSearch.trim();

  const isSearchEmpty =
    !isLoading &&
    !errorMessage &&
    items.length > 0 &&
    filteredItems.length === 0 &&
    Boolean(clientSearch.trim());

  return {
    items,
    filteredItems,
    activeTabId,
    changeStatusFilter,
    shipmentStatusFilter,
    changeShipmentFilter,
    page,
    pagination,
    totalItems,
    totalPages,
    rangeStart,
    rangeEnd,
    isLoading,
    errorMessage,
    isEmpty,
    isFilterEmpty,
    isSearchEmpty,
    goToPage,
    retry,
    clientSearch,
    setClientSearch,
    pendingCount,
  };
}
