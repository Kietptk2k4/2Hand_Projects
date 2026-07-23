import { useCallback, useEffect, useState } from "react";
import { getPaymentSupportDetail } from "../api/orderSupportApi.js";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { ORDER_SUPPORT_PERMISSIONS } from "../constants/orderSupportPermissions.js";
import { handleSupportLoadError } from "../utils/orderSupportTabErrors.js";

export function usePaymentSupportPaymentDetail(paymentId, { enabled = true } = {}) {
  const { showSessionExpired } = useAuthSession();
  const [detail, setDetail] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const refetch = useCallback(async () => {
    if (!enabled || !paymentId) {
      setDetail(null);
      setStatus("idle");
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getPaymentSupportDetail(paymentId);
      setDetail(data);
      setStatus("ready");
    } catch (error) {
      setDetail(null);
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
  }, [enabled, paymentId, showSessionExpired]);

  useEffect(() => {
    refetch();
  }, [refetch]);

  return { detail, status, errorMessage, refetch };
}
