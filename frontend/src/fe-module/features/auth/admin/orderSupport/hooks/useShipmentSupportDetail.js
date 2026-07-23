import { useCallback, useEffect, useState } from "react";
import { getShipmentSupportDetail } from "../api/orderSupportApi.js";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { ORDER_SUPPORT_PERMISSIONS } from "../constants/orderSupportPermissions.js";
import { handleSupportLoadError } from "../utils/orderSupportTabErrors.js";

export function useShipmentSupportDetail(shipmentId, { enabled = true } = {}) {
  const { showSessionExpired } = useAuthSession();
  const [detail, setDetail] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const refetch = useCallback(async () => {
    if (!enabled || !shipmentId) {
      setDetail(null);
      setStatus("idle");
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getShipmentSupportDetail(shipmentId);
      setDetail(data);
      setStatus("ready");
    } catch (error) {
      setDetail(null);
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
  }, [enabled, shipmentId, showSessionExpired]);

  useEffect(() => {
    refetch();
  }, [refetch]);

  return { detail, status, errorMessage, refetch };
}
