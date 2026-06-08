import { useCallback, useEffect, useState } from "react";
import { getPaymentSupportDetail } from "../../api/orderSupportApi.js";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../../security/utils/formatDateTime.js";
import { formatVndPrice } from "../../../../../social/utils/formatPrice.js";
import {
  AccountCard,
  AccountSkeleton,
  TabPanelHeader,
} from "../../../../../../shared/ui/auth/authUi.jsx";
import { ErrorState } from "../../../../../../shared/ui/PageState.jsx";
import { ORDER_SUPPORT_PERMISSIONS } from "../../constants/orderSupportPermissions.js";
import {
  ORDER_SUPPORT_EMPTY_PAYMENT_MESSAGE,
  ORDER_SUPPORT_PAYMENT_SUBTITLE,
  ORDER_SUPPORT_PAYMENT_TITLE,
} from "../../constants/orderSupportUiStrings.js";
import { useOrderSupportPermissions } from "../../hooks/useOrderSupportPermissions.js";
import { handleSupportLoadError } from "../../utils/orderSupportTabErrors.js";
import { navigateToOrderDetail, navigateToWebhookLogs } from "../../utils/supportNavigation.js";
import { SupportEmptyState } from "../SupportEmptyState.jsx";
import { SupportForbiddenState } from "../SupportForbiddenState.jsx";
import { SupportStatusBadge } from "../SupportStatusBadge.jsx";
import { SupportUnavailableState } from "../SupportUnavailableState.jsx";

function DetailRow({ label, value, mono = false }) {
  return (
    <div className="flex flex-col gap-0.5 sm:flex-row sm:justify-between sm:gap-4">
      <dt className="text-sm text-on-surface-variant">{label}</dt>
      <dd className={`text-sm font-medium text-on-surface ${mono ? "break-all font-mono text-xs" : ""}`}>
        {value ?? "—"}
      </dd>
    </div>
  );
}

export function PaymentSupportDetailTab({ paymentId, orderId, onNavigate }) {
  const { showSessionExpired } = useAuthSession();
  const { canReadPayment } = useOrderSupportPermissions();
  const [detail, setDetail] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchDetail = useCallback(async () => {
    if (!paymentId) return;

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getPaymentSupportDetail(paymentId);
      setDetail(data);
      setStatus("ready");
    } catch (error) {
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
  }, [paymentId, showSessionExpired]);

  useEffect(() => {
    if (!paymentId) {
      setDetail(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }
    fetchDetail();
  }, [paymentId, fetchDetail]);

  if (!paymentId) {
    return (
      <div>
        <TabPanelHeader title={ORDER_SUPPORT_PAYMENT_TITLE} subtitle={ORDER_SUPPORT_PAYMENT_SUBTITLE} />
        <SupportEmptyState message={ORDER_SUPPORT_EMPTY_PAYMENT_MESSAGE} />
      </div>
    );
  }

  if (status === "loading" || status === "idle") {
    return (
      <div>
        <TabPanelHeader title={ORDER_SUPPORT_PAYMENT_TITLE} subtitle={ORDER_SUPPORT_PAYMENT_SUBTITLE} />
        <AccountSkeleton />
      </div>
    );
  }

  if (status === "forbidden") {
    return (
      <div>
        <TabPanelHeader title={ORDER_SUPPORT_PAYMENT_TITLE} subtitle={ORDER_SUPPORT_PAYMENT_SUBTITLE} />
        <SupportForbiddenState message={errorMessage} />
      </div>
    );
  }

  if (status === "unavailable") {
    return (
      <div>
        <TabPanelHeader title={ORDER_SUPPORT_PAYMENT_TITLE} subtitle={ORDER_SUPPORT_PAYMENT_SUBTITLE} />
        <SupportUnavailableState message={errorMessage} />
      </div>
    );
  }

  if (status === "error") {
    return (
      <div>
        <TabPanelHeader title={ORDER_SUPPORT_PAYMENT_TITLE} subtitle={ORDER_SUPPORT_PAYMENT_SUBTITLE} />
        <AccountCard className="border-error/30">
          <ErrorState message={errorMessage} />
          <button
            type="button"
            onClick={fetchDetail}
            className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:opacity-90"
          >
            Thử lại
          </button>
        </AccountCard>
      </div>
    );
  }

  const showOutstandingAlert = detail.reconciliation_status === "OUTSTANDING";

  return (
    <div className="space-y-6">
      <TabPanelHeader title={ORDER_SUPPORT_PAYMENT_TITLE} subtitle={ORDER_SUPPORT_PAYMENT_SUBTITLE} />

      {!canReadPayment ? (
        <SupportForbiddenState message="Tài khoản thiếu quyền PAYMENT_SUPPORT_READ." />
      ) : null}

      {showOutstandingAlert ? (
        <AccountCard className="border-amber-200 bg-amber-50/60">
          <p className="text-sm font-medium text-amber-900">Cảnh báo đối soát</p>
          <p className="mt-1 text-sm text-amber-800">
            Giao dịch đã thanh toán tại gateway nhưng hệ thống chưa nhận webhook hợp lệ. Kiểm tra
            nhật ký webhook và đối soát thủ công nếu cần.
          </p>
        </AccountCard>
      ) : null}

      <AccountCard>
        <div className="mb-4 flex flex-wrap items-center gap-2">
          <SupportStatusBadge status={detail.status} />
          <SupportStatusBadge status={detail.reconciliation_status} />
        </div>
        <dl className="space-y-3">
          <DetailRow label="Payment ID" value={detail.payment_id} mono />
          <DetailRow
            label="Order ID"
            value={
              detail.order_id ? (
                <button
                  type="button"
                  onClick={() => onNavigate?.(navigateToOrderDetail(detail.order_id))}
                  className="font-mono text-xs text-primary hover:underline"
                >
                  {detail.order_id}
                </button>
              ) : (
                orderId || "—"
              )
            }
          />
          <DetailRow label="Payer ID" value={detail.payer_id} mono />
          <DetailRow label="Phương thức" value={detail.payment_method} />
          <DetailRow label="Số tiền" value={formatVndPrice(detail.amount)} />
          <DetailRow label="Provider order code" value={detail.provider_order_code} mono />
          <DetailRow label="Provider transaction" value={detail.provider_transaction_id} mono />
          <DetailRow label="Thanh toán lúc" value={formatDateTime(detail.paid_at)} />
          <DetailRow label="Tạo lúc" value={formatDateTime(detail.created_at)} />
        </dl>
      </AccountCard>

      {detail.status_timeline?.length > 0 ? (
        <AccountCard>
          <h3 className="mb-3 text-base font-semibold text-on-surface">Timeline thanh toán</h3>
          <ul className="space-y-3">
            {detail.status_timeline.map((entry, index) => (
              <li key={`${entry.occurred_at}-${index}`} className="border-l-2 border-secondary/30 pl-4">
                <p className="text-sm font-medium text-on-surface">
                  {entry.old_status || "—"} → {entry.new_status}
                </p>
                <p className="text-xs text-on-surface-variant">{formatDateTime(entry.occurred_at)}</p>
              </li>
            ))}
          </ul>
        </AccountCard>
      ) : null}

      <AccountCard>
        <div className="mb-3 flex items-center justify-between gap-2">
          <h3 className="text-base font-semibold text-on-surface">Lịch sử webhook</h3>
          {detail.provider_order_code ? (
            <button
              type="button"
              onClick={() =>
                onNavigate?.(
                  navigateToWebhookLogs({
                    provider: "PAYOS",
                    reference_id: detail.provider_order_code,
                  }),
                )
              }
              className="text-sm font-medium text-primary hover:underline"
            >
              Xem trong Webhook logs
            </button>
          ) : null}
        </div>
        {detail.webhook_events?.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[560px] text-left text-sm">
              <thead>
                <tr className="border-b border-outline-variant text-on-surface-variant">
                  <th className="py-2 pr-4 font-medium">Provider</th>
                  <th className="py-2 pr-4 font-medium">Event</th>
                  <th className="py-2 pr-4 font-medium">Chữ ký</th>
                  <th className="py-2 pr-4 font-medium">Processed</th>
                  <th className="py-2 font-medium">Nhận lúc</th>
                </tr>
              </thead>
              <tbody>
                {detail.webhook_events.map((event, index) => (
                  <tr key={`${event.received_at}-${index}`} className="border-b border-outline-variant/60">
                    <td className="py-3 pr-4">{event.provider}</td>
                    <td className="py-3 pr-4">{event.event_type}</td>
                    <td className="py-3 pr-4">{event.signature_valid ? "Hợp lệ" : "Không hợp lệ"}</td>
                    <td className="py-3 pr-4">{event.processed ? "Có" : "Chưa"}</td>
                    <td className="py-3">{formatDateTime(event.received_at)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="text-sm text-on-surface-variant">Chưa có webhook event.</p>
        )}
      </AccountCard>
    </div>
  );
}
