import { useCallback, useEffect, useState } from "react";
import { listOrdersForSupport } from "../../api/orderSupportApi.js";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../../security/utils/formatDateTime.js";
import { formatVndPrice } from "../../../../../social/utils/formatPrice.js";
import { ORDER_LIST_PAGE_SIZE } from "../../constants/orderListConstants.js";
import { ORDER_SUPPORT_PERMISSIONS } from "../../constants/orderSupportPermissions.js";
import { useOrderSupportPermissions } from "../../hooks/useOrderSupportPermissions.js";
import { handleSupportLoadError } from "../../utils/orderSupportTabErrors.js";
import { OrderSupportDetailTabView } from "./OrderSupportDetailTabView.jsx";

export function OrderSupportDetailTab({
  orderId,
  orderListFilters,
  onNavigate,
  onOrderListFiltersChange,
  onOrderSelect,
}) {
  const { showSessionExpired } = useAuthSession();
  const { canReadOrder } = useOrderSupportPermissions();
  const [listResult, setListResult] = useState(null);
  const [listStatus, setListStatus] = useState("idle");
  const [listErrorMessage, setListErrorMessage] = useState("");

  const [draftFilters, setDraftFilters] = useState({
    status: orderListFilters?.status || "",
    payment_method: orderListFilters?.payment_method || "",
    from: orderListFilters?.from || "",
    to: orderListFilters?.to || "",
    sort: orderListFilters?.sort || "created_at",
  });

  useEffect(() => {
    setDraftFilters({
      status: orderListFilters?.status || "",
      payment_method: orderListFilters?.payment_method || "",
      from: orderListFilters?.from || "",
      to: orderListFilters?.to || "",
      sort: orderListFilters?.sort || "created_at",
    });
  }, [orderListFilters]);

  const fetchOrders = useCallback(async () => {
    setListStatus("loading");
    setListErrorMessage("");

    try {
      const data = await listOrdersForSupport({
        status: orderListFilters?.status || undefined,
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
  }, [orderListFilters, showSessionExpired]);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  const handleApplyFilters = (event) => {
    event.preventDefault();
    onOrderListFiltersChange?.({
      ...draftFilters,
      page: 1,
      size: ORDER_LIST_PAGE_SIZE,
    });
  };

  const handleClearFilters = () => {
    const cleared = {
      status: "",
      payment_method: "",
      from: "",
      to: "",
      sort: "created_at",
      page: 1,
      size: ORDER_LIST_PAGE_SIZE,
    };
    setDraftFilters(cleared);
    onOrderListFiltersChange?.(cleared);
  };

  const currentPage = Number(orderListFilters?.page) || 1;
  const totalPages = listResult?.total_pages || 1;
  const activeSort = orderListFilters?.sort || "created_at";

  const handlePageChange = (nextPage) => {
    onOrderListFiltersChange?.({
      ...orderListFilters,
      page: nextPage,
      size: ORDER_LIST_PAGE_SIZE,
    });
  };

  return (
    <OrderSupportDetailTabView
      canReadOrder={canReadOrder}
      listStatus={listStatus}
      listErrorMessage={listErrorMessage}
      listResult={listResult}
      draftFilters={draftFilters}
      onDraftFiltersChange={setDraftFilters}
      onApplyFilters={handleApplyFilters}
      onClearFilters={handleClearFilters}
      onRetryList={fetchOrders}
      orderId={orderId}
      currentPage={currentPage}
      totalPages={totalPages}
      activeSort={activeSort}
      onPageChange={handlePageChange}
      onOrderSelect={onOrderSelect}
      onNavigate={onNavigate}
      formatDateTime={formatDateTime}
      formatVndPrice={formatVndPrice}
    />
  );
}
