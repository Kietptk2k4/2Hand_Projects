import { useCallback, useEffect, useState } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { fetchAdminRefundApprovalDetail } from "../api/adminRefundApprovalApi.js";
import {
  mapRefundApprovalItem,
  REFUND_REQUESTED_BY_LABELS,
  REFUND_STATUS_LABELS,
} from "../utils/adminRefundApprovalMapper.js";
import { AdminRefundApprovalDetailDrawerView } from "./AdminRefundApprovalDetailDrawerView.jsx";

function formatDateTime(value) {
  if (!value) return "—";
  return new Date(value).toLocaleString("vi-VN");
}

export function AdminRefundApprovalDetailDrawer({ refundRequestId, onClose, onNotify }) {
  const { showSessionExpired } = useAuthSession();
  const [item, setItem] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchDetail = useCallback(async () => {
    if (!refundRequestId) return;
    setStatus("loading");
    setErrorMessage("");
    try {
      const data = await fetchAdminRefundApprovalDetail(refundRequestId);
      setItem(mapRefundApprovalItem(data));
      setStatus("ready");
    } catch (error) {
      if (String(error?.code ?? "").includes("401")) {
        showSessionExpired(error?.message);
        return;
      }
      setErrorMessage(error?.message || "Không tải được chi tiết yêu cầu hoàn tiền.");
      setStatus("error");
      if (String(error?.code ?? "").includes("404")) {
        onNotify?.({ type: "error", message: error?.message || "Yêu cầu hoàn tiền không tồn tại." });
        onClose?.();
      }
    }
  }, [onClose, onNotify, refundRequestId, showSessionExpired]);

  useEffect(() => {
    if (!refundRequestId) {
      setItem(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }
    fetchDetail();
  }, [fetchDetail, refundRequestId]);

  if (!refundRequestId) return null;

  return (
    <AdminRefundApprovalDetailDrawerView
      refundRequestId={refundRequestId}
      status={status}
      errorMessage={errorMessage}
      item={item}
      statusLabels={REFUND_STATUS_LABELS}
      requestedByLabels={REFUND_REQUESTED_BY_LABELS}
      formatDateTime={formatDateTime}
      onClose={onClose}
      onRetry={fetchDetail}
    />
  );
}
