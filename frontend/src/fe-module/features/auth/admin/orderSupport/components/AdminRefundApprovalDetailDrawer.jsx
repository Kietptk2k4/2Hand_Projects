import { useCallback, useEffect, useState } from "react";
import { AccountSkeleton } from "../../../../../shared/ui/auth/authUi.jsx";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { fetchAdminRefundApprovalDetail } from "../api/adminRefundApprovalApi.js";
import {
  mapRefundApprovalItem,
  REFUND_REQUESTED_BY_LABELS,
  REFUND_STATUS_LABELS,
} from "../utils/adminRefundApprovalMapper.js";

function DetailRow({ label, value, mono = false }) {
  return (
    <div>
      <dt className="text-xs font-semibold uppercase tracking-wide text-on-surface-variant">{label}</dt>
      <dd className={`mt-1 text-sm text-on-surface ${mono ? "font-mono break-all" : ""}`}>{value || "—"}</dd>
    </div>
  );
}

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
    <div className="fixed inset-0 z-50 flex justify-end">
      <button
        type="button"
        aria-label="Đóng chi tiết hoàn tiền"
        className="absolute inset-0 bg-black/40"
        onClick={onClose}
      />
      <aside className="relative flex h-full w-full max-w-xl flex-col border-l border-outline-variant bg-surface shadow-xl">
        <div className="flex items-start justify-between border-b border-outline-variant px-6 py-5">
          <div>
            <h2 className="text-lg font-semibold text-on-surface">Chi tiết yêu cầu hoàn tiền</h2>
            <p className="mt-1 font-mono text-xs text-on-surface-variant">{refundRequestId}</p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg px-3 py-1.5 text-sm font-medium text-on-surface-variant hover:bg-surface-container-low"
          >
            Đóng
          </button>
        </div>

        <div className="flex-1 overflow-y-auto px-6 py-5">
          {status === "loading" ? <AccountSkeleton /> : null}
          {status === "error" ? (
            <div className="space-y-3">
              <p className="text-sm text-error">{errorMessage}</p>
              <button
                type="button"
                onClick={fetchDetail}
                className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white"
              >
                Thử lại
              </button>
            </div>
          ) : null}
          {status === "ready" && item ? (
            <div className="space-y-6">
              <div className="grid gap-4 sm:grid-cols-2">
                <DetailRow label="Trạng thái" value={REFUND_STATUS_LABELS[item.status] || item.status} />
                <DetailRow label="Số tiền" value={item.amountLabel} />
                <DetailRow label="Người yêu cầu" value={REFUND_REQUESTED_BY_LABELS[item.requestedBy] || item.requestedBy} />
                <DetailRow label="Lý do" value={item.reason} />
                <DetailRow label="Thanh toán" value={item.paymentMethod} />
                <DetailRow label="TT thanh toán đơn" value={item.orderPaymentStatus} />
                <DetailRow label="TT đơn hàng" value={item.orderStatus} />
                <DetailRow label="Thời gian yêu cầu" value={formatDateTime(item.requestedAt)} />
                <DetailRow label="Xác nhận lúc" value={formatDateTime(item.confirmedAt)} />
                <DetailRow label="Từ chối lúc" value={formatDateTime(item.rejectedAt)} />
                <DetailRow label="Đơn hàng" value={item.orderId} mono />
                <DetailRow label="Thanh toán ID" value={item.paymentId} mono />
                <DetailRow label="Người mua" value={item.buyerId} mono />
                <DetailRow label="Người yêu cầu ID" value={item.requestedByUserId} mono />
              </div>
              {item.adminNote ? (
                <div>
                  <h3 className="mb-2 text-sm font-semibold text-on-surface">Ghi chú admin</h3>
                  <p className="rounded-lg bg-surface-container-low p-3 text-sm text-on-surface">{item.adminNote}</p>
                </div>
              ) : null}
            </div>
          ) : null}
        </div>
      </aside>
    </div>
  );
}