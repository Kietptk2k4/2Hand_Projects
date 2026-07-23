import { useCallback, useEffect, useState } from "react";
import { FeedToast } from "../../../../social/components/FeedToast";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { formatVndPrice } from "../../../../social/utils/formatPrice.js";
import { listOrdersForSupport } from "../api/orderSupportApi.js";
import {
  ORDER_LIST_PAGE_SIZE,
  ORDER_SUPPORT_VIEW_MODES,
} from "../constants/orderSupportListConstants.js";
import { ORDER_SUPPORT_PERMISSIONS } from "../constants/orderSupportPermissions.js";
import { useOrderSupportOrderDetail } from "../hooks/useOrderSupportOrderDetail.js";
import { useOrderSupportOrderStats } from "../hooks/useOrderSupportOrderStats.js";
import { useOrderSupportPermissions } from "../hooks/useOrderSupportPermissions.js";
import { handleSupportLoadError } from "../utils/orderSupportTabErrors.js";
import {
  buildOrderSupportQuickFilter,
  removeOrderSupportFilterChip,
} from "../utils/orderSupportFilterHelpers.js";
import { isValidUuid } from "../utils/supportNavigation.js";
import { OrderSupportDrawer } from "./OrderSupportDrawer.jsx";
import { OrderSupportListView } from "./OrderSupportListView.jsx";

export function OrderSupportListPanel({
  orderListFilters,
  onFiltersChange,
  orderId,
  orderView,
  onOrderSelectionChange,
  onNavigate,
}) {
  const { showSessionExpired } = useAuthSession();
  const { canReadOrder } = useOrderSupportPermissions();
  const [listResult, setListResult] = useState(null);
  const [listStatus, setListStatus] = useState("idle");
  const [listErrorMessage, setListErrorMessage] = useState("");
  const [toastMessage, setToastMessage] = useState("");
  const [lookupValue, setLookupValue] = useState(orderId || "");
  const [lookupError, setLookupError] = useState("");

  const filterStatus = orderListFilters?.status || "";
  const filterPaymentStatus = orderListFilters?.payment_status || "";
  const filterPaymentMethod = orderListFilters?.payment_method || "";
  const filterQ = orderListFilters?.q || "";
  const filterFrom = orderListFilters?.from || "";
  const filterTo = orderListFilters?.to || "";
  const filterSort = orderListFilters?.sort || "created_at";
  const filterPage = Number(orderListFilters?.page) || 1;
  const filterSize = Number(orderListFilters?.size) || ORDER_LIST_PAGE_SIZE;

  const [draftFilters, setDraftFilters] = useState({
    q: filterQ,
    status: filterStatus,
    payment_status: filterPaymentStatus,
    payment_method: filterPaymentMethod,
    from: filterFrom,
    to: filterTo,
    sort: filterSort,
  });

  const { stats, status: statsStatus, refetch: refetchStats } = useOrderSupportOrderStats({
    enabled: canReadOrder,
  });

  const {
    detail,
    status: detailStatus,
    errorMessage: detailErrorMessage,
    refetch: refetchDetail,
  } = useOrderSupportOrderDetail(orderId, { enabled: canReadOrder && Boolean(orderId) });

  useEffect(() => {
    setDraftFilters({
      q: filterQ,
      status: filterStatus,
      payment_status: filterPaymentStatus,
      payment_method: filterPaymentMethod,
      from: filterFrom,
      to: filterTo,
      sort: filterSort,
    });
  }, [
    filterFrom,
    filterPaymentMethod,
    filterPaymentStatus,
    filterQ,
    filterSort,
    filterStatus,
    filterTo,
  ]);

  useEffect(() => {
    setLookupValue(orderId || "");
  }, [orderId]);

  const fetchList = useCallback(async () => {
    if (!canReadOrder) return;

    setListStatus("loading");
    setListErrorMessage("");

    try {
      const data = await listOrdersForSupport({
        q: orderListFilters?.q || undefined,
        status: orderListFilters?.status || undefined,
        payment_status: orderListFilters?.payment_status || undefined,
        payment_method: orderListFilters?.payment_method || undefined,
        from: orderListFilters?.from || undefined,
        to: orderListFilters?.to || undefined,
        sort: orderListFilters?.sort || "created_at",
        page: Number(orderListFilters?.page) || 1,
        size: Number(orderListFilters?.size) || ORDER_LIST_PAGE_SIZE,
      });
      setListResult(data);
      setListStatus("ready");
    } catch (error) {
      handleSupportLoadError(error, {
        showSessionExpired,
        setStatus: setListStatus,
        setErrorMessage: setListErrorMessage,
        permissionCode: ORDER_SUPPORT_PERMISSIONS.READ_ORDER,
        actionLabel: "xem danh sách đơn hàng",
        fallbackMessage: "Không tải được danh sách đơn hàng.",
      });
    }
  }, [canReadOrder, orderListFilters, showSessionExpired]);

  const refreshAll = useCallback(() => {
    fetchList();
    refetchStats();
    if (orderId) refetchDetail();
  }, [fetchList, orderId, refetchDetail, refetchStats]);

  useEffect(() => {
    fetchList();
  }, [fetchList]);

  const applyFiltersPatch = useCallback(
    (patch) => {
      onFiltersChange?.({
        ...orderListFilters,
        ...patch,
      });
    },
    [onFiltersChange, orderListFilters],
  );

  const handleApplyFilters = (event) => {
    event.preventDefault();
    applyFiltersPatch({
      ...draftFilters,
      page: "1",
      size: String(filterSize),
    });
  };

  const handleClearFilters = () => {
    const cleared = {
      q: "",
      status: "",
      payment_status: "",
      payment_method: "",
      from: "",
      to: "",
      sort: "created_at",
      page: "1",
      size: String(ORDER_LIST_PAGE_SIZE),
    };
    setDraftFilters(cleared);
    onFiltersChange?.(cleared);
  };

  const handleQuickFilter = (preset) => {
    const next = buildOrderSupportQuickFilter(preset);
    setDraftFilters({
      q: next.q,
      status: next.status,
      payment_status: next.payment_status,
      payment_method: next.payment_method,
      from: next.from,
      to: next.to,
      sort: next.sort,
    });
    applyFiltersPatch({
      ...next,
      size: String(filterSize),
    });
  };

  const handleRemoveFilterChip = (chipKey) => {
    const next = removeOrderSupportFilterChip(orderListFilters, chipKey);
    setDraftFilters({
      q: next.q || "",
      status: next.status || "",
      payment_status: next.payment_status || "",
      payment_method: next.payment_method || "",
      from: next.from || "",
      to: next.to || "",
      sort: next.sort || "created_at",
    });
    applyFiltersPatch({
      ...next,
      size: String(filterSize),
    });
  };

  const handleOrderSelect = (nextOrderId) => {
    if (!nextOrderId) return;
    if (nextOrderId === orderId) {
      onOrderSelectionChange?.({ orderId: null, orderView: null });
      return;
    }
    onOrderSelectionChange?.({
      orderId: nextOrderId,
      orderView: ORDER_SUPPORT_VIEW_MODES.SUMMARY,
    });
  };

  const handleLookupSubmit = (event) => {
    event.preventDefault();
    const trimmed = lookupValue.trim();
    if (!trimmed) {
      onOrderSelectionChange?.({ orderId: null, orderView: null });
      setLookupError("");
      return;
    }
    if (!isValidUuid(trimmed)) {
      setLookupError("UUID không hợp lệ.");
      return;
    }
    setLookupError("");
    onOrderSelectionChange?.({
      orderId: trimmed,
      orderView: ORDER_SUPPORT_VIEW_MODES.SUMMARY,
    });
  };

  const currentPage = filterPage || listResult?.page || 1;
  const totalPages = listResult?.total_pages || 1;

  return (
    <>
      <OrderSupportListView
        canReadOrder={canReadOrder}
        listStatus={listStatus}
        listErrorMessage={listErrorMessage}
        listResult={listResult}
        appliedFilters={orderListFilters}
        draftFilters={draftFilters}
        onDraftFiltersChange={setDraftFilters}
        onApplyFilters={handleApplyFilters}
        onClearFilters={handleClearFilters}
        onQuickFilter={handleQuickFilter}
        onRemoveFilterChip={handleRemoveFilterChip}
        onRetryList={refreshAll}
        stats={stats}
        statsStatus={statsStatus}
        lookupValue={lookupValue}
        onLookupChange={setLookupValue}
        onLookupSubmit={handleLookupSubmit}
        lookupError={lookupError}
        currentPage={currentPage}
        totalPages={totalPages}
        pageSize={String(filterSize)}
        activeSort={filterSort}
        selectedOrderId={orderId}
        onOrderSelect={handleOrderSelect}
        onPageChange={(nextPage) =>
          applyFiltersPatch({
            page: String(nextPage),
            size: String(filterSize),
          })
        }
        onPageSizeChange={(nextSize) =>
          applyFiltersPatch({
            page: "1",
            size: String(nextSize),
          })
        }
        onCopied={() => setToastMessage("Đã sao chép UUID.")}
        formatVndPrice={formatVndPrice}
        drawer={
          orderId ? (
            <OrderSupportDrawer
              orderId={orderId}
              detail={detail}
              loading={detailStatus === "loading"}
              errorMessage={detailErrorMessage}
              status={detailStatus}
              orderView={orderView || ORDER_SUPPORT_VIEW_MODES.SUMMARY}
              canReadOrder={canReadOrder}
              formatDateTime={formatDateTime}
              formatVndPrice={formatVndPrice}
              onClose={() => onOrderSelectionChange?.({ orderId: null, orderView: null })}
              onViewChange={(nextView) =>
                onOrderSelectionChange?.({ orderId, orderView: nextView })
              }
              onNavigate={onNavigate}
              onRetry={refetchDetail}
            />
          ) : null
        }
      />

      <FeedToast message={toastMessage} onClose={() => setToastMessage("")} />
    </>
  );
}
