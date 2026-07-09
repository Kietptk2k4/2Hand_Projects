import { useCallback, useEffect, useState } from "react";
import { getOrderSupportDetail } from "../api/orderSupportApi.js";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { formatVndPrice } from "../../../../social/utils/formatPrice.js";
import { ORDER_SUPPORT_PERMISSIONS } from "../constants/orderSupportPermissions.js";
import { useOrderSupportPermissions } from "../hooks/useOrderSupportPermissions.js";
import { handleSupportLoadError } from "../utils/orderSupportTabErrors.js";
import {
  navigateToPaymentDetail,
  navigateToShipmentDetail,
} from "../utils/supportNavigation.js";
import { OrderSupportDetailPanelView } from "./OrderSupportDetailPanelView.jsx";
import { SupportForbiddenState } from "./SupportForbiddenState.jsx";
import { SupportListSkeleton } from "./ui/SupportListSkeleton.jsx";
import { SupportRetryPanel } from "./ui/SupportRetryPanel.jsx";
import { SupportUnavailableState } from "./SupportUnavailableState.jsx";

export function OrderSupportDetailPanel({ orderId, onNavigate }) {
  const { showSessionExpired } = useAuthSession();
  const { canReadOrder } = useOrderSupportPermissions();
  const [detail, setDetail] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchDetail = useCallback(async () => {
    if (!orderId) return;

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getOrderSupportDetail(orderId);
      setDetail(data);
      setStatus("ready");
    } catch (error) {
      handleSupportLoadError(error, {
        showSessionExpired,
        setStatus,
        setErrorMessage,
        permissionCode: ORDER_SUPPORT_PERMISSIONS.READ_ORDER,
        actionLabel: "xem chi tiết đơn hàng",
        fallbackMessage: "Không tải được chi tiết đơn hàng.",
        notFoundMessage: "Không tìm thấy đơn hàng.",
      });
    }
  }, [orderId, showSessionExpired]);

  useEffect(() => {
    if (!orderId) {
      setDetail(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }
    fetchDetail();
  }, [orderId, fetchDetail]);

  if (!orderId) return null;

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
    <OrderSupportDetailPanelView
      detail={detail}
      canReadOrder={canReadOrder}
      formatDateTime={formatDateTime}
      formatVndPrice={formatVndPrice}
      onNavigateToPayment={() => {
        if (!detail?.payment?.payment_id) return;
        onNavigate?.(navigateToPaymentDetail(detail.payment.payment_id, null, detail.order_id));
      }}
      onNavigateToShipment={(shipmentId) =>
        onNavigate?.(navigateToShipmentDetail(shipmentId, null, detail.order_id))
      }
    />
  );
}
