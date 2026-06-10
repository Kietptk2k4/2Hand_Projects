import { useCallback, useEffect, useState } from "react";
import { getPaymentSupportDetail, getPaymentsForSupport } from "../../api/orderSupportApi.js";
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
  ORDER_SUPPORT_PAYMENT_SUBTITLE,
  ORDER_SUPPORT_PAYMENT_TITLE,
} from "../../constants/orderSupportUiStrings.js";
import { useOrderSupportPermissions } from "../../hooks/useOrderSupportPermissions.js";
import { handleSupportLoadError } from "../../utils/orderSupportTabErrors.js";
import { navigateToOrderDetail, navigateToPaymentDetail, navigateToWebhookLogs } from "../../utils/supportNavigation.js";
import { SupportForbiddenState } from "../SupportForbiddenState.jsx";
import { SupportStatusBadge } from "../SupportStatusBadge.jsx";
import { SupportUnavailableState } from "../SupportUnavailableState.jsx";

const PAYMENT_STATUS_OPTIONS = ["", "PENDING", "PAID", "FAILED", "CANCELLED", "EXPIRED"];
const PAYMENT_METHOD_OPTIONS = ["", "COD", "PAYOS"];
const PAGE_SIZE = 20;

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

function PaymentSupportDetailPanel({ paymentId, orderId, onNavigate }) {
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

  if (!paymentId) return null;

  if (status === "loading" || status === "idle") {
    return <AccountSkeleton />;
  }

  if (status === "forbidden") {
    return <SupportForbiddenState message={errorMessage} />;
  }

  if (status === "unavailable") {
    return <SupportUnavailableState message={errorMessage} />;
  }

  if (status === "error") {
    return (
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
    );
  }

  const showOutstandingAlert = detail.reconciliation_status === "OUTSTANDING";

  return (
    <div className="space-y-6">
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

export function PaymentSupportDetailTab({
  paymentId,
  orderId,
  paymentFilters,
  onNavigate,
  onPaymentFiltersChange,
}) {
  const { showSessionExpired } = useAuthSession();
  const { canReadPayment } = useOrderSupportPermissions();
  const [listResult, setListResult] = useState(null);
  const [listStatus, setListStatus] = useState("idle");
  const [listErrorMessage, setListErrorMessage] = useState("");

  const [draftFilters, setDraftFilters] = useState({
    status: paymentFilters?.status || "",
    payment_method: paymentFilters?.payment_method || "",
    order_id: paymentFilters?.order_id || "",
    from: paymentFilters?.from || "",
    to: paymentFilters?.to || "",
  });

  useEffect(() => {
    setDraftFilters({
      status: paymentFilters?.status || "",
      payment_method: paymentFilters?.payment_method || "",
      order_id: paymentFilters?.order_id || "",
      from: paymentFilters?.from || "",
      to: paymentFilters?.to || "",
    });
  }, [paymentFilters]);

  const fetchPayments = useCallback(async () => {
    setListStatus("loading");
    setListErrorMessage("");

    try {
      const data = await getPaymentsForSupport({
        status: paymentFilters?.status || undefined,
        payment_method: paymentFilters?.payment_method || undefined,
        order_id: paymentFilters?.order_id || undefined,
        from: paymentFilters?.from || undefined,
        to: paymentFilters?.to || undefined,
        page: Number(paymentFilters?.page) || 1,
        size: Number(paymentFilters?.size) || PAGE_SIZE,
      });
      setListResult(data);
      setListStatus("ready");
    } catch (error) {
      handleSupportLoadError(error, {
        showSessionExpired,
        setStatus: setListStatus,
        setErrorMessage: setListErrorMessage,
        permissionCode: ORDER_SUPPORT_PERMISSIONS.READ_PAYMENT,
        actionLabel: "xem danh sách thanh toán",
        fallbackMessage: "Không tải được danh sách thanh toán.",
      });
    }
  }, [paymentFilters, showSessionExpired]);

  useEffect(() => {
    fetchPayments();
  }, [fetchPayments]);

  const handleApplyFilters = (event) => {
    event.preventDefault();
    onPaymentFiltersChange?.({
      ...draftFilters,
      page: 1,
      size: PAGE_SIZE,
    });
  };

  const handleClearFilters = () => {
    const cleared = {
      status: "",
      payment_method: "",
      order_id: "",
      from: "",
      to: "",
      page: 1,
      size: PAGE_SIZE,
    };
    setDraftFilters(cleared);
    onPaymentFiltersChange?.(cleared);
  };

  const currentPage = Number(paymentFilters?.page) || 1;
  const totalPages = listResult?.total_pages || 1;

  const handlePageChange = (nextPage) => {
    onPaymentFiltersChange?.({
      ...paymentFilters,
      page: nextPage,
      size: PAGE_SIZE,
    });
  };

  const handleSelectPayment = (selectedPaymentId) => {
    onNavigate?.(navigateToPaymentDetail(selectedPaymentId, null, orderId));
  };

  return (
    <div className="space-y-6">
      <TabPanelHeader title={ORDER_SUPPORT_PAYMENT_TITLE} subtitle={ORDER_SUPPORT_PAYMENT_SUBTITLE} />

      {!canReadPayment ? (
        <SupportForbiddenState message="Tài khoản thiếu quyền PAYMENT_SUPPORT_READ." />
      ) : null}

      <AccountCard>
        <form onSubmit={handleApplyFilters} className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Trạng thái</label>
            <select
              value={draftFilters.status}
              onChange={(e) => setDraftFilters((prev) => ({ ...prev, status: e.target.value }))}
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
            >
              {PAYMENT_STATUS_OPTIONS.map((option) => (
                <option key={option || "all"} value={option}>
                  {option || "Tất cả"}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Phương thức</label>
            <select
              value={draftFilters.payment_method}
              onChange={(e) => setDraftFilters((prev) => ({ ...prev, payment_method: e.target.value }))}
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
            >
              {PAYMENT_METHOD_OPTIONS.map((option) => (
                <option key={option || "all"} value={option}>
                  {option || "Tất cả"}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Order ID</label>
            <input
              type="text"
              value={draftFilters.order_id}
              onChange={(e) => setDraftFilters((prev) => ({ ...prev, order_id: e.target.value }))}
              placeholder="UUID đơn hàng"
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
            />
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Từ (ISO)</label>
            <input
              type="datetime-local"
              value={draftFilters.from}
              onChange={(e) => setDraftFilters((prev) => ({ ...prev, from: e.target.value }))}
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
            />
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Đến (ISO)</label>
            <input
              type="datetime-local"
              value={draftFilters.to}
              onChange={(e) => setDraftFilters((prev) => ({ ...prev, to: e.target.value }))}
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
            />
          </div>
          <div className="flex items-end gap-2 md:col-span-2 lg:col-span-3">
            <button
              type="submit"
              className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:opacity-90"
            >
              Áp dụng bộ lọc
            </button>
            <button
              type="button"
              onClick={handleClearFilters}
              className="rounded-lg border border-outline-variant px-4 py-2 text-sm font-medium text-on-surface-variant hover:bg-surface-container-low"
            >
              Xóa bộ lọc
            </button>
          </div>
        </form>
      </AccountCard>

      {listStatus === "loading" ? <AccountSkeleton /> : null}
      {listStatus === "forbidden" ? <SupportForbiddenState message={listErrorMessage} /> : null}
      {listStatus === "unavailable" ? <SupportUnavailableState message={listErrorMessage} /> : null}

      {listStatus === "error" ? (
        <AccountCard className="border-error/30">
          <ErrorState message={listErrorMessage} />
          <button
            type="button"
            onClick={fetchPayments}
            className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:opacity-90"
          >
            Thử lại
          </button>
        </AccountCard>
      ) : null}

      {listStatus === "ready" ? (
        <AccountCard>
          <div className="mb-4 flex flex-wrap items-center justify-between gap-2">
            <p className="text-sm text-on-surface-variant">
              {listResult.total_elements ?? 0} thanh toán · Trang {listResult.page}/{totalPages}
            </p>
            <p className="text-xs text-on-surface-variant">Sắp xếp: mới nhất trước</p>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full min-w-[720px] text-left text-sm">
              <thead>
                <tr className="border-b border-outline-variant text-on-surface-variant">
                  <th className="py-2 pr-4 font-medium">Tạo lúc</th>
                  <th className="py-2 pr-4 font-medium">Payment ID</th>
                  <th className="py-2 pr-4 font-medium">Order ID</th>
                  <th className="py-2 pr-4 font-medium">Phương thức</th>
                  <th className="py-2 pr-4 font-medium">Số tiền</th>
                  <th className="py-2 font-medium">Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                {(listResult.payments ?? []).length === 0 ? (
                  <tr>
                    <td colSpan={6} className="py-6 text-center text-on-surface-variant">
                      Không có thanh toán phù hợp bộ lọc.
                    </td>
                  </tr>
                ) : (
                  listResult.payments.map((payment) => {
                    const isSelected = payment.payment_id === paymentId;
                    return (
                      <tr
                        key={payment.payment_id}
                        className={[
                          "cursor-pointer border-b border-outline-variant/60 hover:bg-surface-container-low",
                          isSelected ? "bg-primary/5" : "",
                        ].join(" ")}
                        onClick={() => handleSelectPayment(payment.payment_id)}
                      >
                        <td className="py-3 pr-4">{formatDateTime(payment.created_at)}</td>
                        <td className="py-3 pr-4 font-mono text-xs">{payment.payment_id}</td>
                        <td className="py-3 pr-4 font-mono text-xs">{payment.order_id}</td>
                        <td className="py-3 pr-4">{payment.payment_method}</td>
                        <td className="py-3 pr-4">{formatVndPrice(payment.amount)}</td>
                        <td className="py-3">
                          <SupportStatusBadge status={payment.status} />
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
          {totalPages > 1 ? (
            <div className="mt-4 flex items-center justify-center gap-2">
              <button
                type="button"
                disabled={currentPage <= 1}
                onClick={() => handlePageChange(currentPage - 1)}
                className="rounded-lg border border-outline-variant px-3 py-1.5 text-sm disabled:opacity-40"
              >
                Trước
              </button>
              <span className="text-sm text-on-surface-variant">
                {currentPage} / {totalPages}
              </span>
              <button
                type="button"
                disabled={currentPage >= totalPages}
                onClick={() => handlePageChange(currentPage + 1)}
                className="rounded-lg border border-outline-variant px-3 py-1.5 text-sm disabled:opacity-40"
              >
                Sau
              </button>
            </div>
          ) : null}
        </AccountCard>
      ) : null}

      {paymentId ? (
        <div>
          <h3 className="mb-3 text-base font-semibold text-on-surface">Chi tiết thanh toán đang xem</h3>
          <PaymentSupportDetailPanel paymentId={paymentId} orderId={orderId} onNavigate={onNavigate} />
        </div>
      ) : null}
    </div>
  );
}
