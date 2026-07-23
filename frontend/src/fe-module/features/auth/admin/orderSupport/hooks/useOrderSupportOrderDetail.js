import { useCallback, useEffect, useState } from "react";
import { getOrderSupportDetail } from "../api/orderSupportApi.js";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { ORDER_SUPPORT_PERMISSIONS } from "../constants/orderSupportPermissions.js";
import { handleSupportLoadError } from "../utils/orderSupportTabErrors.js";

export function useOrderSupportOrderDetail(orderId, { enabled = true } = {}) {
  const { showSessionExpired } = useAuthSession();
  const [detail, setDetail] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const refetch = useCallback(async () => {
    if (!enabled || !orderId) {
      setDetail(null);
      setStatus("idle");
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getOrderSupportDetail(orderId);
      setDetail(data);
      setStatus("ready");
    } catch (error) {
      setDetail(null);
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
  }, [enabled, orderId, showSessionExpired]);

  useEffect(() => {
    refetch();
  }, [refetch]);

  return { detail, status, errorMessage, refetch };
}
