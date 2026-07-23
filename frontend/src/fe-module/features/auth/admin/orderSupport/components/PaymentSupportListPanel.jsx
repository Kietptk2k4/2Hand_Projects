import { useCallback, useEffect, useState } from "react";
import { FeedToast } from "../../../../social/components/FeedToast";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { formatVndPrice } from "../../../../social/utils/formatPrice.js";
import { getPaymentsForSupport } from "../api/orderSupportApi.js";
import {
  PAYMENT_LIST_PAGE_SIZE,
  PAYMENT_SUPPORT_VIEW_MODES,
} from "../constants/paymentSupportListConstants.js";
import { ORDER_SUPPORT_PERMISSIONS } from "../constants/orderSupportPermissions.js";
import { usePaymentSupportPaymentDetail } from "../hooks/usePaymentSupportPaymentDetail.js";
import { usePaymentSupportPaymentStats } from "../hooks/usePaymentSupportPaymentStats.js";
import { useOrderSupportPermissions } from "../hooks/useOrderSupportPermissions.js";
import { handleSupportLoadError } from "../utils/orderSupportTabErrors.js";
import {
  buildPaymentSupportQuickFilter,
  removePaymentSupportFilterChip,
} from "../utils/paymentSupportFilterHelpers.js";
import { isValidUuid } from "../utils/supportNavigation.js";
import { PaymentSupportDrawer } from "./PaymentSupportDrawer.jsx";
import { PaymentSupportListView } from "./PaymentSupportListView.jsx";

export function PaymentSupportListPanel({
  paymentFilters,
  onFiltersChange,
  paymentId,
  paymentView,
  orderId,
  onPaymentSelectionChange,
  onNavigate,
}) {
  const { showSessionExpired } = useAuthSession();
  const { canReadPayment } = useOrderSupportPermissions();
  const [listResult, setListResult] = useState(null);
  const [listStatus, setListStatus] = useState("idle");
  const [listErrorMessage, setListErrorMessage] = useState("");
  const [toastMessage, setToastMessage] = useState("");
  const [lookupValue, setLookupValue] = useState(paymentId || "");
  const [lookupError, setLookupError] = useState("");

  const filterPage = Number(paymentFilters?.page) || 1;
  const filterSize = Number(paymentFilters?.size) || PAYMENT_LIST_PAGE_SIZE;

  const [draftFilters, setDraftFilters] = useState({
    q: paymentFilters?.q || "",
    status: paymentFilters?.status || "",
    reconciliation_status: paymentFilters?.reconciliation_status || "",
    payment_method: paymentFilters?.payment_method || "",
    order_id: paymentFilters?.order_id || "",
    from: paymentFilters?.from || "",
    to: paymentFilters?.to || "",
  });

  const { stats, status: statsStatus, refetch: refetchStats } = usePaymentSupportPaymentStats({
    enabled: canReadPayment,
  });

  const {
    detail,
    status: detailStatus,
    errorMessage: detailErrorMessage,
    refetch: refetchDetail,
  } = usePaymentSupportPaymentDetail(paymentId, { enabled: canReadPayment && Boolean(paymentId) });

  useEffect(() => {
    setDraftFilters({
      q: paymentFilters?.q || "",
      status: paymentFilters?.status || "",
      reconciliation_status: paymentFilters?.reconciliation_status || "",
      payment_method: paymentFilters?.payment_method || "",
      order_id: paymentFilters?.order_id || "",
      from: paymentFilters?.from || "",
      to: paymentFilters?.to || "",
    });
  }, [paymentFilters]);

  useEffect(() => {
    setLookupValue(paymentId || "");
  }, [paymentId]);

  const fetchList = useCallback(async () => {
    if (!canReadPayment) return;

    setListStatus("loading");
    setListErrorMessage("");

    try {
      const data = await getPaymentsForSupport({
        q: paymentFilters?.q || undefined,
        status: paymentFilters?.status || undefined,
        reconciliation_status: paymentFilters?.reconciliation_status || undefined,
        payment_method: paymentFilters?.payment_method || undefined,
        order_id: paymentFilters?.order_id || undefined,
        from: paymentFilters?.from || undefined,
        to: paymentFilters?.to || undefined,
        page: Number(paymentFilters?.page) || 1,
        size: Number(paymentFilters?.size) || PAYMENT_LIST_PAGE_SIZE,
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
  }, [canReadPayment, paymentFilters, showSessionExpired]);

  const refreshAll = useCallback(() => {
    fetchList();
    refetchStats();
    if (paymentId) refetchDetail();
  }, [fetchList, paymentId, refetchDetail, refetchStats]);

  useEffect(() => {
    fetchList();
  }, [fetchList]);

  const applyFiltersPatch = useCallback(
    (patch) => {
      onFiltersChange?.({
        ...paymentFilters,
        ...patch,
      });
    },
    [onFiltersChange, paymentFilters],
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
      reconciliation_status: "",
      payment_method: "",
      order_id: "",
      from: "",
      to: "",
      page: "1",
      size: String(PAYMENT_LIST_PAGE_SIZE),
    };
    setDraftFilters(cleared);
    onFiltersChange?.(cleared);
  };

  const handleQuickFilter = (preset) => {
    const next = buildPaymentSupportQuickFilter(preset);
    setDraftFilters({
      q: next.q,
      status: next.status,
      reconciliation_status: next.reconciliation_status,
      payment_method: next.payment_method,
      order_id: next.order_id,
      from: next.from,
      to: next.to,
    });
    applyFiltersPatch({
      ...next,
      size: String(filterSize),
    });
  };

  const handleRemoveFilterChip = (chipKey) => {
    const next = removePaymentSupportFilterChip(paymentFilters, chipKey);
    setDraftFilters({
      q: next.q || "",
      status: next.status || "",
      reconciliation_status: next.reconciliation_status || "",
      payment_method: next.payment_method || "",
      order_id: next.order_id || "",
      from: next.from || "",
      to: next.to || "",
    });
    applyFiltersPatch({
      ...next,
      size: String(filterSize),
    });
  };

  const handlePaymentSelect = (nextPaymentId) => {
    if (!nextPaymentId) return;
    if (nextPaymentId === paymentId) {
      onPaymentSelectionChange?.({ paymentId: null, paymentView: null });
      return;
    }
    onPaymentSelectionChange?.({
      paymentId: nextPaymentId,
      paymentView: PAYMENT_SUPPORT_VIEW_MODES.SUMMARY,
    });
  };

  const handleLookupSubmit = (event) => {
    event.preventDefault();
    const trimmed = lookupValue.trim();
    if (!trimmed) {
      onPaymentSelectionChange?.({ paymentId: null, paymentView: null });
      setLookupError("");
      return;
    }
    if (!isValidUuid(trimmed)) {
      setLookupError("UUID không hợp lệ.");
      return;
    }
    setLookupError("");
    onPaymentSelectionChange?.({
      paymentId: trimmed,
      paymentView: PAYMENT_SUPPORT_VIEW_MODES.SUMMARY,
    });
  };

  const currentPage = filterPage || listResult?.page || 1;
  const totalPages = listResult?.total_pages || 1;

  return (
    <div className="w-full">
      <PaymentSupportListView
        canReadPayment={canReadPayment}
        listStatus={listStatus}
        listErrorMessage={listErrorMessage}
        listResult={listResult}
        appliedFilters={paymentFilters}
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
        selectedPaymentId={paymentId}
        onPaymentSelect={handlePaymentSelect}
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
          paymentId ? (
            <PaymentSupportDrawer
              paymentId={paymentId}
              detail={detail}
              loading={detailStatus === "loading"}
              errorMessage={detailErrorMessage}
              status={detailStatus}
              paymentView={paymentView || PAYMENT_SUPPORT_VIEW_MODES.SUMMARY}
              orderId={orderId}
              canReadPayment={canReadPayment}
              formatDateTime={formatDateTime}
              formatVndPrice={formatVndPrice}
              onClose={() => onPaymentSelectionChange?.({ paymentId: null, paymentView: null })}
              onViewChange={(nextView) =>
                onPaymentSelectionChange?.({ paymentId, paymentView: nextView })
              }
              onNavigate={onNavigate}
              onRetry={refetchDetail}
            />
          ) : null
        }
      />

      <FeedToast message={toastMessage} onClose={() => setToastMessage("")} />
    </div>
  );
}
