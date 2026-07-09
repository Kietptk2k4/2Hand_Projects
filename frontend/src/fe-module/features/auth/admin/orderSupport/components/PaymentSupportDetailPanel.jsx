import { useCallback, useEffect, useState } from "react";
import { getPaymentSupportDetail } from "../api/orderSupportApi.js";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { formatVndPrice } from "../../../../social/utils/formatPrice.js";
import { ORDER_SUPPORT_PERMISSIONS } from "../constants/orderSupportPermissions.js";
import { useOrderSupportPermissions } from "../hooks/useOrderSupportPermissions.js";
import { handleSupportLoadError } from "../utils/orderSupportTabErrors.js";
import {
  navigateToOrderDetail,
  navigateToWebhookLogs,
} from "../utils/supportNavigation.js";
import { PaymentSupportDetailPanelView } from "./PaymentSupportDetailPanelView.jsx";
import { SupportForbiddenState } from "./SupportForbiddenState.jsx";
import { SupportListSkeleton } from "./ui/SupportListSkeleton.jsx";
import { SupportRetryPanel } from "./ui/SupportRetryPanel.jsx";
import { SupportUnavailableState } from "./SupportUnavailableState.jsx";

export function PaymentSupportDetailPanel({ paymentId, orderId, onNavigate }) {
  const { showSessionExpired } = useAuthSession();
  const { canReadPayment } = useOrderSupportPermissions();
  const [detail, setDetail] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchDetail = useCallback(async () => {
    if (!paymentId) return;

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getPaymentSupportDetail(paymentId);
      setDetail(data);
      setStatus("ready");
    } catch (error) {
      handleSupportLoadError(error, {
        showSessionExpired,
        setStatus,
        setErrorMessage,
        permissionCode: ORDER_SUPPORT_PERMISSIONS.READ_PAYMENT,
        actionLabel: "xem chi tiết thanh toán",
        fallbackMessage: "Không tải được chi tiết thanh toán.",
        notFoundMessage: "Không tìm thấy thanh toán.",
      });
    }
  }, [paymentId, showSessionExpired]);

  useEffect(() => {
    if (!paymentId) {
      setDetail(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }
    fetchDetail();
  }, [paymentId, fetchDetail]);

  if (!paymentId) return null;

  if (status === "loading" || status === "idle") {
    return <SupportListSkeleton rows={3} />;
  }

  if (status === "forbidden") {
    return <SupportForbiddenState message={errorMessage} />;
  }

  if (status === "unavailable") {
    return <SupportUnavailableState message={errorMessage} />;
  }

  if (status === "error") {
    return <SupportRetryPanel message={errorMessage} onRetry={fetchDetail} />;
  }

  return (
    <PaymentSupportDetailPanelView
      detail={detail}
      orderId={orderId}
      canReadPayment={canReadPayment}
      formatDateTime={formatDateTime}
      formatVndPrice={formatVndPrice}
      onNavigateToOrder={() => onNavigate?.(navigateToOrderDetail(detail.order_id))}
      onNavigateToWebhookLogs={() =>
        onNavigate?.(
          navigateToWebhookLogs({
            provider: "PAYOS",
            reference_id: detail.provider_order_code,
          }),
        )
      }
    />
  );
}
