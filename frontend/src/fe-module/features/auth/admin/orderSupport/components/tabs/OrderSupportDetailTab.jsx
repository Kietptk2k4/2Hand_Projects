import { useCallback, useEffect, useState } from "react";
import { getOrderSupportDetail, listOrdersForSupport } from "../../api/orderSupportApi.js";
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
  ORDER_LIST_PAGE_SIZE,
  ORDER_LIST_PAYMENT_METHOD_OPTIONS,
  ORDER_LIST_SORT_OPTIONS,
  ORDER_LIST_STATUS_OPTIONS,
} from "../../constants/orderListConstants.js";
import {
  ORDER_SUPPORT_MASKED_NOTICE,
  ORDER_SUPPORT_ORDER_SUBTITLE,
  ORDER_SUPPORT_ORDER_TITLE,
} from "../../constants/orderSupportUiStrings.js";
import { useOrderSupportPermissions } from "../../hooks/useOrderSupportPermissions.js";
import { handleSupportLoadError } from "../../utils/orderSupportTabErrors.js";
import {
  navigateToPaymentDetail,
  navigateToShipmentDetail,
} from "../../utils/supportNavigation.js";
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

function sortColumnLabel(sortField) {
  const option = ORDER_LIST_SORT_OPTIONS.find((item) => item.value === sortField);
  return option?.label || "Tao gan nhat";
}

function OrderSupportDetailPanel({ orderId, onNavigate }) {
  const { showSessionExpired } = useAuthSession();
  const { canReadOrder } = useOrderSupportPermissions();
  const [detail, setDetail] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchDetail = useCallback(async () => {
    if (!orderId) return;

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getOrderSupportDetail(orderId);
      setDetail(data);
      setStatus("ready");
    } catch (error) {
      handleSupportLoadError(error, {
        showSessionExpired,
        setStatus,
        setErrorMessage,
        permissionCode: ORDER_SUPPORT_PERMISSIONS.READ_ORDER,
        actionLabel: "xem chi tiết đơn hàng",
        fallbackMessage: "Không tải được chi tiết đơn hàng.",
        notFoundMessage: "Không tìm thấy đơn hàng.",
      });
    }
  }, [orderId, showSessionExpired]);

  useEffect(() => {
    if (!orderId) {
      setDetail(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }
    fetchDetail();
  }, [orderId, fetchDetail]);

  if (!orderId) return null;

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

  return (
    <div className="space-y-6">
      {!canReadOrder ? (
        <SupportForbiddenState message="Tài khoản thiếu quyền ORDER_SUPPORT_READ." />
      ) : null}

      <AccountCard>
        <div className="mb-4 flex flex-wrap items-center gap-2">
          <SupportStatusBadge status={detail.order_status} />
          <SupportStatusBadge status={detail.order_payment_status} />
          {detail.payment_method ? (
            <span className="text-xs text-on-surface-variant">{detail.payment_method}</span>
          ) : null}
        </div>
        <dl className="space-y-3">
          <DetailRow label="Order ID" value={detail.order_id} mono />
          <DetailRow label="Buyer ID" value={detail.buyer_id} mono />
          <DetailRow label="Tổng tiền" value={formatVndPrice(detail.total_amount)} />
          <DetailRow label="Thanh toán" value={formatVndPrice(detail.final_amount)} />
          <DetailRow label="Tạo lúc" value={formatDateTime(detail.created_at)} />
          <DetailRow label="Cập nhật" value={formatDateTime(detail.updated_at)} />
          <DetailRow label="Hoàn thành" value={formatDateTime(detail.completed_at)} />
        </dl>
      </AccountCard>

      {detail.payment ? (
        <AccountCard>
          <div className="mb-3 flex items-center justify-between gap-2">
            <h3 className="text-base font-semibold text-on-surface">Thanh toán</h3>
            <button
              type="button"
              onClick={() =>
                onNavigate?.(
                  navigateToPaymentDetail(detail.payment.payment_id, null, detail.order_id),
                )
              }
              className="text-sm font-medium text-primary hover:underline"
            >
              Mở chi tiết thanh toán
            </button>
          </div>
          <dl className="space-y-2">
            <DetailRow label="Payment ID" value={detail.payment.payment_id} mono />
            <DetailRow label="Trạng thái" value={detail.payment.status} />
            <DetailRow label="Số tiền" value={formatVndPrice(detail.payment.amount)} />
          </dl>
        </AccountCard>
      ) : null}

      {detail.items?.length > 0 ? (
        <AccountCard>
          <h3 className="mb-3 text-base font-semibold text-on-surface">Sản phẩm trong đơn</h3>
          <div className="overflow-x-auto">
            <table className="w-full min-w-[640px] text-left text-sm">
              <thead>
                <tr className="border-b border-outline-variant text-on-surface-variant">
                  <th className="py-2 pr-4 font-medium">Sản phẩm</th>
                  <th className="py-2 pr-4 font-medium">Shop</th>
                  <th className="py-2 pr-4 font-medium">SL</th>
                  <th className="py-2 pr-4 font-medium">Giá</th>
                  <th className="py-2 font-medium">Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                {detail.items.map((item) => (
                  <tr key={item.order_item_id} className="border-b border-outline-variant/60">
                    <td className="py-3 pr-4">{item.product_name_snapshot || "—"}</td>
                    <td className="py-3 pr-4">{item.shop_name_snapshot || "—"}</td>
                    <td className="py-3 pr-4">{item.quantity}</td>
                    <td className="py-3 pr-4">{formatVndPrice(item.final_price)}</td>
                    <td className="py-3">
                      <SupportStatusBadge status={item.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </AccountCard>
      ) : null}

      {detail.shipments?.length > 0 ? (
        <AccountCard>
          <h3 className="mb-3 text-base font-semibold text-on-surface">Vận chuyển</h3>
          <ul className="divide-y divide-outline-variant">
            {detail.shipments.map((shipment) => (
              <li
                key={shipment.shipment_id}
                className="flex flex-col gap-2 py-3 sm:flex-row sm:items-center sm:justify-between"
              >
                <div>
                  <p className="font-mono text-xs text-on-surface">{shipment.shipment_id}</p>
                  <p className="text-sm text-on-surface-variant">
                    {shipment.carrier || "—"} · {shipment.tracking_number || "Chưa có tracking"}
                  </p>
                  <SupportStatusBadge status={shipment.status} className="mt-1" />
                </div>
                <button
                  type="button"
                  onClick={() =>
                    onNavigate?.(
                      navigateToShipmentDetail(shipment.shipment_id, null, detail.order_id),
                    )
                  }
                  className="text-sm font-medium text-primary hover:underline"
                >
                  Mở chi tiết vận chuyển
                </button>
              </li>
            ))}
          </ul>
          {detail.contact_fields_masked ? (
            <p className="mt-3 text-xs text-on-surface-variant">{ORDER_SUPPORT_MASKED_NOTICE}</p>
          ) : null}
        </AccountCard>
      ) : null}

      {detail.order_timeline?.length > 0 ? (
        <AccountCard>
          <h3 className="mb-3 text-base font-semibold text-on-surface">Timeline đơn hàng</h3>
          <ul className="space-y-3">
            {detail.order_timeline.map((entry, index) => (
              <li key={`${entry.occurred_at}-${index}`} className="border-l-2 border-primary/30 pl-4">
                <p className="text-sm font-medium text-on-surface">
                  {entry.old_status || "—"} → {entry.new_status}
                </p>
                <p className="text-xs text-on-surface-variant">{formatDateTime(entry.occurred_at)}</p>
                {entry.note ? <p className="mt-1 text-xs text-on-surface-variant">{entry.note}</p> : null}
              </li>
            ))}
          </ul>
        </AccountCard>
      ) : null}
    </div>
  );
}

export function OrderSupportDetailTab({
  orderId,
  orderListFilters,
  onNavigate,
  onOrderListFiltersChange,
  onOrderSelect,
}) {
  const { showSessionExpired } = useAuthSession();
  const { canReadOrder } = useOrderSupportPermissions();
  const [listResult, setListResult] = useState(null);
  const [listStatus, setListStatus] = useState("idle");
  const [listErrorMessage, setListErrorMessage] = useState("");

  const [draftFilters, setDraftFilters] = useState({
    status: orderListFilters?.status || "",
    payment_method: orderListFilters?.payment_method || "",
    from: orderListFilters?.from || "",
    to: orderListFilters?.to || "",
    sort: orderListFilters?.sort || "created_at",
  });

  useEffect(() => {
    setDraftFilters({
      status: orderListFilters?.status || "",
      payment_method: orderListFilters?.payment_method || "",
      from: orderListFilters?.from || "",
      to: orderListFilters?.to || "",
      sort: orderListFilters?.sort || "created_at",
    });
  }, [orderListFilters]);

  const fetchOrders = useCallback(async () => {
    setListStatus("loading");
    setListErrorMessage("");

    try {
      const data = await listOrdersForSupport({
        status: orderListFilters?.status || undefined,
        payment_method: orderListFilters?.payment_method || undefined,
        from: orderListFilters?.from || undefined,
        to: orderListFilters?.to || undefined,
        sort: orderListFilters?.sort || "created_at",
        page: Number(orderListFilters?.page) || 1,
        size: Number(orderListFilters?.size) || ORDER_LIST_PAGE_SIZE,
      });
      setListResult(data);
      setListStatus("ready");
    } catch (error) {
      handleSupportLoadError(error, {
        showSessionExpired,
        setStatus: setListStatus,
        setErrorMessage: setListErrorMessage,
        permissionCode: ORDER_SUPPORT_PERMISSIONS.READ_ORDER,
        actionLabel: "xem danh sách đơn hàng",
        fallbackMessage: "Không tải được danh sách đơn hàng.",
      });
    }
  }, [orderListFilters, showSessionExpired]);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  const handleApplyFilters = (event) => {
    event.preventDefault();
    onOrderListFiltersChange?.({
      ...draftFilters,
      page: 1,
      size: ORDER_LIST_PAGE_SIZE,
    });
  };

  const handleClearFilters = () => {
    const cleared = {
      status: "",
      payment_method: "",
      from: "",
      to: "",
      sort: "created_at",
      page: 1,
      size: ORDER_LIST_PAGE_SIZE,
    };
    setDraftFilters(cleared);
    onOrderListFiltersChange?.(cleared);
  };

  const currentPage = Number(orderListFilters?.page) || 1;
  const totalPages = listResult?.total_pages || 1;
  const activeSort = orderListFilters?.sort || "created_at";

  const handlePageChange = (nextPage) => {
    onOrderListFiltersChange?.({
      ...orderListFilters,
      page: nextPage,
      size: ORDER_LIST_PAGE_SIZE,
    });
  };

  return (
    <div className="space-y-6">
      <TabPanelHeader title={ORDER_SUPPORT_ORDER_TITLE} subtitle={ORDER_SUPPORT_ORDER_SUBTITLE} />

      {!canReadOrder ? (
        <SupportForbiddenState message="Tài khoản thiếu quyền ORDER_SUPPORT_READ." />
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
              {ORDER_LIST_STATUS_OPTIONS.map((option) => (
                <option key={option.value || "all"} value={option.value}>
                  {option.label}
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
              {ORDER_LIST_PAYMENT_METHOD_OPTIONS.map((option) => (
                <option key={option.value || "all"} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Sắp xếp theo</label>
            <select
              value={draftFilters.sort}
              onChange={(e) => setDraftFilters((prev) => ({ ...prev, sort: e.target.value }))}
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
            >
              {ORDER_LIST_SORT_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
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
            onClick={fetchOrders}
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
              {listResult.total_elements ?? 0} đơn hàng · Sắp xếp: {sortColumnLabel(activeSort)} · Trang{" "}
              {listResult.page}/{totalPages}
            </p>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full min-w-[900px] text-left text-sm">
              <thead>
                <tr className="border-b border-outline-variant text-on-surface-variant">
                  <th className="py-2 pr-4 font-medium">Tạo lúc</th>
                  <th className="py-2 pr-4 font-medium">Order ID</th>
                  <th className="py-2 pr-4 font-medium">Buyer ID</th>
                  <th className="py-2 pr-4 font-medium">Phương thức</th>
                  <th className="py-2 pr-4 font-medium">Số tiền</th>
                  <th className="py-2 pr-4 font-medium">Trạng thái</th>
                  <th className="py-2 font-medium">Thanh toán</th>
                </tr>
              </thead>
              <tbody>
                {(listResult.orders ?? []).length === 0 ? (
                  <tr>
                    <td colSpan={7} className="py-6 text-center text-on-surface-variant">
                      Không có đơn hàng phù hợp bộ lọc.
                    </td>
                  </tr>
                ) : (
                  listResult.orders.map((order) => {
                    const isSelected = order.order_id === orderId;
                    return (
                      <tr
                        key={order.order_id}
                        className={[
                          "cursor-pointer border-b border-outline-variant/60 hover:bg-surface-container-low",
                          isSelected ? "bg-primary/5" : "",
                        ].join(" ")}
                        onClick={() => onOrderSelect?.(order.order_id)}
                      >
                        <td className="py-3 pr-4">{formatDateTime(order.created_at)}</td>
                        <td className="py-3 pr-4 font-mono text-xs">{order.order_id}</td>
                        <td className="py-3 pr-4 font-mono text-xs">{order.buyer_id}</td>
                        <td className="py-3 pr-4">{order.payment_method}</td>
                        <td className="py-3 pr-4">{formatVndPrice(order.final_amount)}</td>
                        <td className="py-3 pr-4">
                          <SupportStatusBadge status={order.order_status} />
                        </td>
                        <td className="py-3">
                          <SupportStatusBadge status={order.payment_status} />
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

      {orderId ? (
        <div>
          <h3 className="mb-3 text-base font-semibold text-on-surface">Chi tiết đơn hàng đang xem</h3>
          <OrderSupportDetailPanel orderId={orderId} onNavigate={onNavigate} />
        </div>
      ) : null}
    </div>
  );
}
