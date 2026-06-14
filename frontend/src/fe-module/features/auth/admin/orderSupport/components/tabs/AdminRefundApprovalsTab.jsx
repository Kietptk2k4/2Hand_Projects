import { useCallback, useEffect, useState } from "react";
import { formatVndPrice } from "../../../../../social/utils/formatPrice";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import { AccountCard, AccountSkeleton, TabPanelHeader } from "../../../../../../shared/ui/auth/authUi.jsx";
import { ErrorState } from "../../../../../../shared/ui/PageState.jsx";
import {
  confirmAdminRefundApproval,
  fetchAdminRefundApprovals,
  rejectAdminRefundApproval,
} from "../../api/adminRefundApprovalApi.js";

const STATUS_OPTIONS = ["", "REQUESTED", "CONFIRMED", "REJECTED"];

const STATUS_LABELS = {
  REQUESTED: "Chờ duyệt",
  CONFIRMED: "Đã hoàn tiền",
  REJECTED: "Từ chối",
};

const REQUESTED_BY_LABELS = {
  BUYER: "Người mua",
  SELLER: "Người bán",
};

function mapQueueResponse(raw) {
  const pagination = raw?.pagination ?? {};
  return {
    items: (raw?.items ?? []).map((item) => ({
      id: item.id,
      orderId: item.order_id ?? item.orderId,
      paymentId: item.payment_id ?? item.paymentId,
      buyerId: item.buyer_id ?? item.buyerId,
      requestedBy: item.requested_by ?? item.requestedBy,
      amount: Number(item.amount) || 0,
      status: item.status,
      reason: item.reason,
      adminNote: item.admin_note ?? item.adminNote,
      paymentMethod: item.payment_method ?? item.paymentMethod,
      requestedAt: item.requested_at ?? item.requestedAt,
    })),
    pagination: {
      page: Number(pagination.page) || 1,
      totalItems: Number(pagination.total_items ?? pagination.totalItems) || 0,
    },
  };
}

export function AdminRefundApprovalsTab({ onNotify }) {
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
      const raw = await fetchAdminRefundApprovals({ status: statusFilter || undefined, page: 1, limit: 20 });
      setQueue(mapQueueResponse(raw));
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
      } catch (error) {
        onNotify?.({ type: "error", message: error?.message || "Thao tác thất bại." });
      } finally {
        setActionId("");
      }
    },
    [load, onNotify],
  );

  return (
    <AccountCard>
      <TabPanelHeader
        title="Duyệt hoàn tiền"
        subtitle="Xác nhận sau khi đã hoàn tiền thủ công trên VNPay. Không có nút hủy đơn trực tiếp."
      />

      <div className="mb-4 flex flex-wrap items-center gap-3">
        <label className="text-body-sm text-on-surface-variant">
          Trạng thái
          <select
            className="ml-2 rounded-lg border border-outline-variant bg-surface px-3 py-2 text-body-md"
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value)}
          >
            {STATUS_OPTIONS.map((value) => (
              <option key={value || "all"} value={value}>
                {value ? STATUS_LABELS[value] || value : "Tất cả"}
              </option>
            ))}
          </select>
        </label>
        <button
          type="button"
          onClick={load}
          disabled={loadStatus === "loading"}
          className="rounded-lg border border-outline-variant px-3 py-2 text-label-md hover:bg-surface-container-high disabled:opacity-60"
        >
          Làm mới
        </button>
      </div>

      {loadStatus === "loading" ? <AccountSkeleton rows={4} /> : null}
      {loadStatus === "error" ? <ErrorState message={errorMessage} onRetry={load} /> : null}

      {loadStatus === "ready" ? (
        <div className="overflow-x-auto">
          <table className="min-w-full text-left text-body-sm">
            <thead className="border-b border-outline-variant text-on-surface-variant">
              <tr>
                <th className="px-3 py-2">Thời gian</th>
                <th className="px-3 py-2">Đơn hàng</th>
                <th className="px-3 py-2">Người yêu cầu</th>
                <th className="px-3 py-2">Số tiền</th>
                <th className="px-3 py-2">Thanh toán</th>
                <th className="px-3 py-2">Trạng thái</th>
                <th className="px-3 py-2">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {queue.items.length ? (
                queue.items.map((item) => (
                  <tr key={item.id} className="border-b border-outline-variant/50">
                    <td className="px-3 py-2">
                      {item.requestedAt ? new Date(item.requestedAt).toLocaleString("vi-VN") : "—"}
                    </td>
                    <td className="px-3 py-2 font-mono text-xs">{item.orderId}</td>
                    <td className="px-3 py-2">{REQUESTED_BY_LABELS[item.requestedBy] || item.requestedBy}</td>
                    <td className="px-3 py-2 font-medium">{formatVndPrice(item.amount)}</td>
                    <td className="px-3 py-2">{item.paymentMethod || "—"}</td>
                    <td className="px-3 py-2">{STATUS_LABELS[item.status] || item.status}</td>
                    <td className="px-3 py-2">
                      {item.status === "REQUESTED" ? (
                        <div className="flex flex-wrap gap-2">
                          <button
                            type="button"
                            disabled={actionId === item.id}
                            onClick={() => runAction(item.id, "confirm")}
                            className="rounded bg-primary-container px-2 py-1 text-label-sm text-on-primary-container"
                          >
                            Xác nhận đã hoàn tiền
                          </button>
                          <button
                            type="button"
                            disabled={actionId === item.id}
                            onClick={() => runAction(item.id, "reject")}
                            className="rounded border border-outline-variant px-2 py-1 text-label-sm"
                          >
                            Từ chối
                          </button>
                        </div>
                      ) : (
                        <span className="text-on-surface-variant">—</span>
                      )}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={7} className="px-3 py-8 text-center text-on-surface-variant">
                    Không có yêu cầu hoàn tiền.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
          <p className="mt-3 text-body-sm text-on-surface-variant">
            Tổng {queue.pagination.totalItems} yêu cầu
          </p>
        </div>
      ) : null}
    </AccountCard>
  );
}
