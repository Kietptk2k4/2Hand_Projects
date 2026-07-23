import { useCallback, useEffect, useState } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { fetchAdminRefundApprovalDetail } from "../api/adminRefundApprovalApi.js";
import { ORDER_SUPPORT_PERMISSIONS } from "../constants/orderSupportPermissions.js";
import { mapRefundApprovalItem } from "../utils/adminRefundApprovalMapper.js";
import { handleSupportLoadError } from "../utils/orderSupportTabErrors.js";

export function useRefundSupportDetail(refundRequestId, { enabled = true } = {}) {
  const { showSessionExpired } = useAuthSession();
  const [detail, setDetail] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const refetch = useCallback(async () => {
    if (!enabled || !refundRequestId) {
      setDetail(null);
      setStatus("idle");
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchAdminRefundApprovalDetail(refundRequestId);
      setDetail(mapRefundApprovalItem(data));
      setStatus("ready");
    } catch (error) {
      setDetail(null);
      handleSupportLoadError(error, {
        showSessionExpired,
        setStatus,
        setErrorMessage,
        permissionCode: ORDER_SUPPORT_PERMISSIONS.READ_REFUND,
        actionLabel: "xem chi tiết hoàn tiền",
        fallbackMessage: "Không tải được chi tiết yêu cầu hoàn tiền.",
        notFoundMessage: "Không tìm thấy yêu cầu hoàn tiền.",
      });
    }
  }, [enabled, refundRequestId, showSessionExpired]);

  useEffect(() => {
    refetch();
  }, [refetch]);

  return { detail, status, errorMessage, refetch };
}
