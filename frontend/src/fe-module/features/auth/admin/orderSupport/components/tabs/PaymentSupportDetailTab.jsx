import { useCallback, useEffect, useState } from "react";
import { getPaymentsForSupport } from "../../api/orderSupportApi.js";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../../security/utils/formatDateTime.js";
import { formatVndPrice } from "../../../../../social/utils/formatPrice.js";
import { ORDER_SUPPORT_PERMISSIONS } from "../../constants/orderSupportPermissions.js";
import { useOrderSupportPermissions } from "../../hooks/useOrderSupportPermissions.js";
import { handleSupportLoadError } from "../../utils/orderSupportTabErrors.js";
import { navigateToPaymentDetail } from "../../utils/supportNavigation.js";
import { PaymentSupportDetailTabView } from "./PaymentSupportDetailTabView.jsx";

const PAGE_SIZE = 20;

export function PaymentSupportDetailTab({
  paymentId,
  orderId,
  paymentFilters,
  onNavigate,
  onPaymentFiltersChange,
}) {
  const { showSessionExpired } = useAuthSession();
  const { canReadPayment } = useOrderSupportPermissions();
  const [listResult, setListResult] = useState(null);
  const [listStatus, setListStatus] = useState("idle");
  const [listErrorMessage, setListErrorMessage] = useState("");

  const [draftFilters, setDraftFilters] = useState({
    status: paymentFilters?.status || "",
    payment_method: paymentFilters?.payment_method || "",
    order_id: paymentFilters?.order_id || "",
    from: paymentFilters?.from || "",
    to: paymentFilters?.to || "",
  });

  useEffect(() => {
    setDraftFilters({
      status: paymentFilters?.status || "",
      payment_method: paymentFilters?.payment_method || "",
      order_id: paymentFilters?.order_id || "",
      from: paymentFilters?.from || "",
      to: paymentFilters?.to || "",
    });
  }, [paymentFilters]);

  const fetchPayments = useCallback(async () => {
    setListStatus("loading");
    setListErrorMessage("");

    try {
      const data = await getPaymentsForSupport({
        status: paymentFilters?.status || undefined,
        payment_method: paymentFilters?.payment_method || undefined,
        order_id: paymentFilters?.order_id || undefined,
        from: paymentFilters?.from || undefined,
        to: paymentFilters?.to || undefined,
        page: Number(paymentFilters?.page) || 1,
        size: Number(paymentFilters?.size) || PAGE_SIZE,
      });
      setListResult(data);
      setListStatus("ready");
    } catch (error) {
      handleSupportLoadError(error, {
        showSessionExpired,
        setStatus: setListStatus,
        setErrorMessage: setListErrorMessage,
        permissionCode: ORDER_SUPPORT_PERMISSIONS.READ_PAYMENT,
        actionLabel: "xem danh sách thanh toán",
        fallbackMessage: "Không tải được danh sách thanh toán.",
      });
    }
  }, [paymentFilters, showSessionExpired]);

  useEffect(() => {
    fetchPayments();
  }, [fetchPayments]);

  const handleApplyFilters = (event) => {
    event.preventDefault();
    onPaymentFiltersChange?.({
      ...draftFilters,
      page: 1,
      size: PAGE_SIZE,
    });
  };

  const handleClearFilters = () => {
    const cleared = {
      status: "",
      payment_method: "",
      order_id: "",
      from: "",
      to: "",
      page: 1,
      size: PAGE_SIZE,
    };
    setDraftFilters(cleared);
    onPaymentFiltersChange?.(cleared);
  };

  const currentPage = Number(paymentFilters?.page) || 1;
  const totalPages = listResult?.total_pages || 1;

  const handlePageChange = (nextPage) => {
    onPaymentFiltersChange?.({
      ...paymentFilters,
      page: nextPage,
      size: PAGE_SIZE,
    });
  };

  const handleSelectPayment = (selectedPaymentId) => {
    onNavigate?.(navigateToPaymentDetail(selectedPaymentId, null, orderId));
  };

  return (
    <PaymentSupportDetailTabView
      canReadPayment={canReadPayment}
      listStatus={listStatus}
      listErrorMessage={listErrorMessage}
      listResult={listResult}
      draftFilters={draftFilters}
      onDraftFiltersChange={setDraftFilters}
      onApplyFilters={handleApplyFilters}
      onClearFilters={handleClearFilters}
      onRetryList={fetchPayments}
      paymentId={paymentId}
      orderId={orderId}
      currentPage={currentPage}
      totalPages={totalPages}
      onPageChange={handlePageChange}
      onPaymentSelect={handleSelectPayment}
      onNavigate={onNavigate}
      formatDateTime={formatDateTime}
      formatVndPrice={formatVndPrice}
    />
  );
}
