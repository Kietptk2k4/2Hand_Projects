import { useCallback, useEffect, useState } from "react";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import {
  approveAdminPayoutRequest,
  fetchAdminPayoutQueue,
  markAdminPayoutRequestPaid,
  rejectAdminPayoutRequest,
} from "../../api/adminFinancePayoutApi.js";
import {
  applyPayoutQueueAfterAction,
  mapPayoutQueueResponse,
} from "../../utils/adminFinancePayoutMapper.js";
import { AdminFinancePayoutQueueView } from "../AdminFinancePayoutQueueView.jsx";

export function AdminFinancePayoutQueueTab({ onNotify }) {
  const { showSessionExpired } = useAuthSession();
  const [statusFilter, setStatusFilter] = useState("REQUESTED");
  const [queue, setQueue] = useState({ items: [], pagination: { page: 1, totalItems: 0 } });
  const [loadStatus, setLoadStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [actionId, setActionId] = useState("");

  const load = useCallback(async () => {
    setLoadStatus("loading");
    setErrorMessage("");
    try {
      const raw = await fetchAdminPayoutQueue({ status: statusFilter || undefined, page: 1, limit: 20 });
      setQueue(mapPayoutQueueResponse(raw));
      setLoadStatus("ready");
    } catch (error) {
      if (String(error?.code ?? "").includes("401")) {
        showSessionExpired(error?.message);
        setLoadStatus("error");
        return;
      }
      setErrorMessage(error?.message || "Không tải được hàng đợi rút tiền.");
      setLoadStatus("error");
    }
  }, [showSessionExpired, statusFilter]);

  useEffect(() => {
    load();
  }, [load]);

  const runAction = useCallback(
    async (payoutRequestId, action) => {
      setActionId(payoutRequestId);
      try {
        let updatedItem = null;

        if (action === "approve") {
          updatedItem = await approveAdminPayoutRequest(payoutRequestId);
          onNotify?.({ variant: "success", message: "Đã duyệt yêu cầu rút tiền." });
        }

        if (action === "reject") {
          const note = window.prompt("Lý do từ chối (bắt buộc)");
          if (note === null) return;
          const trimmedNote = note.trim();
          if (!trimmedNote) {
            onNotify?.({ variant: "error", message: "Vui lòng nhập lý do từ chối." });
            return;
          }
          updatedItem = await rejectAdminPayoutRequest(payoutRequestId, trimmedNote);
          onNotify?.({ variant: "success", message: "Đã từ chối yêu cầu rút tiền." });
        }

        if (action === "mark-paid") {
          const ref = window.prompt("Mã tham chiếu chuyển khoản");
          if (!ref?.trim()) return;
          updatedItem = await markAdminPayoutRequestPaid(payoutRequestId, ref.trim());
          onNotify?.({ variant: "success", message: "Đã ghi nhận chuyển khoản." });
        }

        if (updatedItem) {
          setQueue((prev) => applyPayoutQueueAfterAction(prev, updatedItem, statusFilter));
        }
        await load();
      } catch (error) {
        onNotify?.({ variant: "error", message: error?.message || "Thao tác thất bại." });
      } finally {
        setActionId("");
      }
    },
    [load, onNotify, statusFilter],
  );

  return (
    <AdminFinancePayoutQueueView
      title="Hàng đợi rút tiền"
      subtitle="Duyệt, từ chối hoặc ghi nhận chuyển khoản cho seller."
      statusFilter={statusFilter}
      loadStatus={loadStatus}
      errorMessage={errorMessage}
      items={queue.items}
      totalItems={queue.pagination.totalItems}
      actionId={actionId}
      onStatusChange={setStatusFilter}
      onRefresh={load}
      onAction={runAction}
      onRetry={load}
    />
  );
}
