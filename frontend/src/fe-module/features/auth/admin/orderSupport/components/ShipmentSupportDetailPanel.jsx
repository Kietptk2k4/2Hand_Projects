import { useCallback, useEffect, useState } from "react";
import { getShipmentSupportDetail } from "../api/orderSupportApi.js";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { formatVndPrice } from "../../../../social/utils/formatPrice.js";
import { ORDER_SUPPORT_PERMISSIONS } from "../constants/orderSupportPermissions.js";
import { useOrderSupportPermissions } from "../hooks/useOrderSupportPermissions.js";
import { handleSupportLoadError } from "../utils/orderSupportTabErrors.js";
import { navigateToOrderDetail, navigateToWebhookLogs } from "../utils/supportNavigation.js";
import { ShipmentSupportDetailPanelView } from "./ShipmentSupportDetailPanelView.jsx";
import { SupportForbiddenState } from "./SupportForbiddenState.jsx";
import { SupportListSkeleton } from "./ui/SupportListSkeleton.jsx";
import { SupportRetryPanel } from "./ui/SupportRetryPanel.jsx";
import { SupportUnavailableState } from "./SupportUnavailableState.jsx";

export function ShipmentSupportDetailPanel({ shipmentId, onNavigate, onNotify }) {
  const { showSessionExpired } = useAuthSession();
  const { canReadShipment, canWriteShipment, canForceWriteShipment } = useOrderSupportPermissions();
  const [detail, setDetail] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchDetail = useCallback(async () => {
    if (!shipmentId) return;

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getShipmentSupportDetail(shipmentId);
      setDetail(data);
      setStatus("ready");
    } catch (error) {
      handleSupportLoadError(error, {
        showSessionExpired,
        setStatus,
        setErrorMessage,
        permissionCode: ORDER_SUPPORT_PERMISSIONS.READ_SHIPMENT,
        actionLabel: "xem chi tiết vận chuyển",
        fallbackMessage: "Không tải được chi tiết vận chuyển.",
        notFoundMessage: "Không tìm thấy vận đơn.",
      });
    }
  }, [shipmentId, showSessionExpired]);

  useEffect(() => {
    if (!shipmentId) {
      setDetail(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }
    fetchDetail();
  }, [shipmentId, fetchDetail]);

  if (!shipmentId) return null;

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
    <ShipmentSupportDetailPanelView
      detail={detail}
      shipmentId={shipmentId}
      canReadShipment={canReadShipment}
      canWriteShipment={canWriteShipment}
      canForceWriteShipment={canForceWriteShipment}
      formatDateTime={formatDateTime}
      formatVndPrice={formatVndPrice}
      onNavigateToOrder={() => onNavigate?.(navigateToOrderDetail(detail.order_id))}
      onNavigateToWebhook={() =>
        onNavigate?.(
          navigateToWebhookLogs({
            provider: "GHN",
            reference_id: detail.ghn_order_code,
          }),
        )
      }
      onOverrideSuccess={() => fetchDetail()}
      onNotify={onNotify}
    />
  );
}
