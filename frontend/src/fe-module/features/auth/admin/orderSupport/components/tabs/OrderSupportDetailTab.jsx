import { useCallback, useEffect, useState } from "react";
import { getOrderSupportDetail } from "../../api/orderSupportApi.js";
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
  ORDER_SUPPORT_EMPTY_ORDER_MESSAGE,
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

export function OrderSupportDetailTab({ orderId, onNavigate, onNotify }) {
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

  if (!orderId) {
    return (
      <div>
        <TabPanelHeader title={ORDER_SUPPORT_ORDER_TITLE} subtitle={ORDER_SUPPORT_ORDER_SUBTITLE} />
        <SupportEmptyState message={ORDER_SUPPORT_EMPTY_ORDER_MESSAGE} />
      </div>
    );
  }

  if (status === "loading" || status === "idle") {
    return (
      <div>
        <TabPanelHeader title={ORDER_SUPPORT_ORDER_TITLE} subtitle={ORDER_SUPPORT_ORDER_SUBTITLE} />
        <AccountSkeleton />
      </div>
    );
  }

  if (status === "forbidden") {
    return (
      <div>
        <TabPanelHeader title={ORDER_SUPPORT_ORDER_TITLE} subtitle={ORDER_SUPPORT_ORDER_SUBTITLE} />
        <SupportForbiddenState message={errorMessage} />
      </div>
    );
  }

  if (status === "unavailable") {
    return (
      <div>
        <TabPanelHeader title={ORDER_SUPPORT_ORDER_TITLE} subtitle={ORDER_SUPPORT_ORDER_SUBTITLE} />
        <SupportUnavailableState message={errorMessage} />
      </div>
    );
  }

  if (status === "error") {
    return (
      <div>
        <TabPanelHeader title={ORDER_SUPPORT_ORDER_TITLE} subtitle={ORDER_SUPPORT_ORDER_SUBTITLE} />
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

  return (
    <div className="space-y-6">
      <TabPanelHeader title={ORDER_SUPPORT_ORDER_TITLE} subtitle={ORDER_SUPPORT_ORDER_SUBTITLE} />

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
              <li key={shipment.shipment_id} className="flex flex-col gap-2 py-3 sm:flex-row sm:items-center sm:justify-between">
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
