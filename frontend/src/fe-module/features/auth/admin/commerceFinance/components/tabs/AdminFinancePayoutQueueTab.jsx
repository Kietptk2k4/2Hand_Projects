import { useCallback, useEffect, useState } from "react";
import { formatVndPrice } from "../../../../../social/utils/formatPrice";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import { AccountCard, AccountSkeleton, TabPanelHeader } from "../../../../../../shared/ui/auth/authUi.jsx";
import { ErrorState } from "../../../../../../shared/ui/PageState.jsx";
import {
  approveAdminPayoutRequest,
  fetchAdminPayoutQueue,
  markAdminPayoutRequestPaid,
  rejectAdminPayoutRequest,
} from "../../api/adminFinancePayoutApi.js";

const STATUS_OPTIONS = ["", "REQUESTED", "APPROVED", "PAID", "REJECTED", "CANCELLED"];

const STATUS_LABELS = {
  REQUESTED: "Chờ duyệt",
  APPROVED: "Đã duyệt",
  PAID: "Đã chuyển",
  REJECTED: "Từ chối",
  CANCELLED: "Đã hủy",
};

function mapQueueResponse(raw) {
  const pagination = raw?.pagination ?? {};
  return {
    items: (raw?.items ?? []).map((item) => ({
      id: item.id,
      sellerId: item.seller_id ?? item.sellerId,
      amount: Number(item.amount) || 0,
      status: item.status,
      bankName: item.bank_name ?? item.bankName,
      bankAccountName: item.bank_account_name ?? item.bankAccountName,
      bankAccountNumber: item.bank_account_number ?? item.bankAccountNumber,
      requestedAt: item.requested_at ?? item.requestedAt,
      adminNote: item.admin_note ?? item.adminNote,
      bankTransferRef: item.bank_transfer_ref ?? item.bankTransferRef,
    })),
    pagination: {
      page: Number(pagination.page) || 1,
      totalItems: Number(pagination.total_items ?? pagination.totalItems) || 0,
    },
  };
}

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
      setQueue(mapQueueResponse(raw));
      setLoadStatus("ready");
    } catch (error) {
      if (String(error?.code ?? "").includes("401")) {
        showSessionExpired(error?.message);
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
        if (action === "approve") {
          await approveAdminPayoutRequest(payoutRequestId);
          onNotify?.({ type: "success", message: "Đã duyệt yêu cầu rút tiền." });
        }
        if (action === "reject") {
          const note = window.prompt("Lý do từ chối (tuỳ chọn)") || "";
          await rejectAdminPayoutRequest(payoutRequestId, note);
          onNotify?.({ type: "success", message: "Đã từ chối yêu cầu rút tiền." });
        }
        if (action === "mark-paid") {
          const ref = window.prompt("Mã tham chiếu chuyển khoản");
          if (!ref?.trim()) return;
          await markAdminPayoutRequestPaid(payoutRequestId, ref.trim());
          onNotify?.({ type: "success", message: "Đã ghi nhận chuyển khoản." });
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
        title="Hàng đợi rút tiền"
        subtitle="Duyệt, từ chối hoặc ghi nhận chuyển khoản cho seller."
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
                <th className="px-3 py-2">Seller</th>
                <th className="px-3 py-2">Số tiền</th>
                <th className="px-3 py-2">Tài khoản</th>
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
                    <td className="px-3 py-2 font-mono text-xs">{item.sellerId}</td>
                    <td className="px-3 py-2 font-medium">{formatVndPrice(item.amount)}</td>
                    <td className="px-3 py-2">
                      <div>{item.bankName}</div>
                      <div className="text-on-surface-variant">
                        {item.bankAccountName} · {item.bankAccountNumber}
                      </div>
                    </td>
                    <td className="px-3 py-2">{STATUS_LABELS[item.status] || item.status}</td>
                    <td className="px-3 py-2">
                      <div className="flex flex-wrap gap-2">
                        {item.status === "REQUESTED" ? (
                          <>
                            <button
                              type="button"
                              disabled={actionId === item.id}
                              onClick={() => runAction(item.id, "approve")}
                              className="rounded bg-primary-container px-2 py-1 text-label-sm text-on-primary-container"
                            >
                              Duyệt
                            </button>
                            <button
                              type="button"
                              disabled={actionId === item.id}
                              onClick={() => runAction(item.id, "reject")}
                              className="rounded border border-outline-variant px-2 py-1 text-label-sm"
                            >
                              Từ chối
                            </button>
                          </>
                        ) : null}
                        {item.status === "APPROVED" ? (
                          <button
                            type="button"
                            disabled={actionId === item.id}
                            onClick={() => runAction(item.id, "mark-paid")}
                            className="rounded bg-tertiary-container px-2 py-1 text-label-sm text-on-tertiary-container"
                          >
                            Đã chuyển
                          </button>
                        ) : null}
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={6} className="px-3 py-8 text-center text-on-surface-variant">
                    Không có yêu cầu rút tiền.
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
