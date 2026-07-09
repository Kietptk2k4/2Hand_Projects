import { useCallback, useEffect, useState } from "react";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import { formatVndPrice } from "../../../../../social/utils/formatPrice.js";
import {
  confirmAdminRefundApproval,
  fetchAdminRefundApprovals,
  rejectAdminRefundApproval,
} from "../../api/adminRefundApprovalApi.js";
import {
  mapRefundApprovalQueueResponse,
  REFUND_REQUESTED_BY_LABELS,
  REFUND_STATUS_LABELS,
} from "../../utils/adminRefundApprovalMapper.js";
import { AdminRefundApprovalDetailDrawer } from "../AdminRefundApprovalDetailDrawer.jsx";
import { AdminRefundApprovalsView } from "../AdminRefundApprovalsView.jsx";

const STATUS_OPTIONS = ["", "REQUESTED", "CONFIRMED", "REJECTED"];

function formatRefundDateTime(value) {
  if (!value) return "—";
  return new Date(value).toLocaleString("vi-VN");
}

export function AdminRefundApprovalsTab({ onNotify }) {
  const { showSessionExpired } = useAuthSession();
  const [statusFilter, setStatusFilter] = useState("REQUESTED");
  const [queue, setQueue] = useState({ items: [], pagination: { page: 1, totalItems: 0 } });
  const [loadStatus, setLoadStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [actionId, setActionId] = useState("");
  const [selectedRefundId, setSelectedRefundId] = useState("");

  const load = useCallback(async () => {
    setLoadStatus("loading");
    setErrorMessage("");
    try {
      const raw = await fetchAdminRefundApprovals({ status: statusFilter || undefined, page: 1, limit: 20 });
      setQueue(mapRefundApprovalQueueResponse(raw));
      setLoadStatus("ready");
    } catch (error) {
      if (String(error?.code ?? "").includes("401")) {
        showSessionExpired(error?.message);
        return;
      }
      setErrorMessage(error?.message || "Không tải được danh sách duyệt hoàn tiền.");
      setLoadStatus("error");
    }
  }, [showSessionExpired, statusFilter]);

  useEffect(() => {
    load();
  }, [load]);

  const runAction = useCallback(
    async (refundRequestId, action) => {
      setActionId(refundRequestId);
      try {
        if (action === "confirm") {
          const confirmed = window.confirm(
            "Xác nhận bạn đã hoàn tiền cho khách trên VNPay? Hành động này sẽ hủy đơn và giải phóng tồn kho.",
          );
          if (!confirmed) return;
          await confirmAdminRefundApproval(refundRequestId);
          onNotify?.({ type: "success", message: "Đã xác nhận hoàn tiền." });
        }
        if (action === "reject") {
          const note = window.prompt("Lý do từ chối (tuỳ chọn)") || "";
          await rejectAdminRefundApproval(refundRequestId, note);
          onNotify?.({ type: "success", message: "Đã từ chối yêu cầu hoàn tiền." });
        }
        await load();
        if (selectedRefundId === refundRequestId) {
          setSelectedRefundId("");
        }
      } catch (error) {
        onNotify?.({ type: "error", message: error?.message || "Thao tác thất bại." });
      } finally {
        setActionId("");
      }
    },
    [load, onNotify, selectedRefundId],
  );

  return (
    <>
      <AdminRefundApprovalsView
        statusFilter={statusFilter}
        statusOptions={STATUS_OPTIONS}
        statusLabels={REFUND_STATUS_LABELS}
        requestedByLabels={REFUND_REQUESTED_BY_LABELS}
        loadStatus={loadStatus}
        errorMessage={errorMessage}
        queue={queue}
        actionId={actionId}
        onStatusFilterChange={setStatusFilter}
        onRefresh={load}
        onSelectDetail={setSelectedRefundId}
        onConfirm={(id) => runAction(id, "confirm")}
        onReject={(id) => runAction(id, "reject")}
        formatVndPrice={formatVndPrice}
        formatDateTime={formatRefundDateTime}
      />

      <AdminRefundApprovalDetailDrawer
        refundRequestId={selectedRefundId}
        onClose={() => setSelectedRefundId("")}
        onNotify={onNotify}
      />
    </>
  );
}
