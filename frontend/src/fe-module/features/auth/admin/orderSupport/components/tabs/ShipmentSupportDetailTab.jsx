import { useCallback, useEffect, useState } from "react";
import { getShipmentSupportDetail } from "../../api/orderSupportApi.js";
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
  ORDER_SUPPORT_MASKED_NOTICE,
  ORDER_SUPPORT_SHIPMENT_SUBTITLE,
  ORDER_SUPPORT_SHIPMENT_TITLE,
} from "../../constants/orderSupportUiStrings.js";
import { useOrderSupportPermissions } from "../../hooks/useOrderSupportPermissions.js";
import { handleSupportLoadError } from "../../utils/orderSupportTabErrors.js";
import { navigateToOrderDetail, navigateToWebhookLogs } from "../../utils/supportNavigation.js";
import { ShipmentSupportListPanel } from "../ShipmentSupportListPanel.jsx";
import { SupportForbiddenState } from "../SupportForbiddenState.jsx";
import { SupportStatusBadge } from "../SupportStatusBadge.jsx";
import { SupportUnavailableState } from "../SupportUnavailableState.jsx";
import { ShipmentStatusOverrideCard } from "../ShipmentStatusOverrideCard.jsx";

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

function ShipmentDetailPanel({ shipmentId, onNavigate, onNotify }) {
  const { showSessionExpired } = useAuthSession();
  const { canReadShipment, canWriteShipment, canForceWriteShipment } = useOrderSupportPermissions();
  const [detail, setDetail] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchDetail = useCallback(async () => {
    if (!shipmentId) return;

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getShipmentSupportDetail(shipmentId);
      setDetail(data);
      setStatus("ready");
    } catch (error) {
      handleSupportLoadError(error, {
        showSessionExpired,
        setStatus,
        setErrorMessage,
        permissionCode: ORDER_SUPPORT_PERMISSIONS.READ_SHIPMENT,
        actionLabel: "xem chi tiết vận chuyển",
        fallbackMessage: "Không tải được chi tiết vận chuyển.",
        notFoundMessage: "Không tìm thấy vận đơn.",
      });
    }
  }, [shipmentId, showSessionExpired]);

  useEffect(() => {
    if (!shipmentId) {
      setDetail(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }
    fetchDetail();
  }, [shipmentId, fetchDetail]);

  if (!shipmentId) {
    return null;
  }

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

  const address = detail.shipping_address;

  return (
    <div className="space-y-6 border-t border-outline-variant pt-6">
      <h3 className="text-base font-semibold text-on-surface">Chi tiết vận đơn</h3>

      {!canReadShipment ? (
        <SupportForbiddenState message="Tài khoản thiếu quyền SHIPMENT_SUPPORT_READ." />
      ) : null}

      <AccountCard>
        <div className="mb-4 flex flex-wrap items-center gap-2">
          <SupportStatusBadge status={detail.internal_status} />
          {detail.carrier_status ? (
            <span className="rounded-full bg-surface-container-high px-2.5 py-0.5 text-xs font-semibold text-on-surface">
              Carrier: {detail.carrier_status}
            </span>
          ) : null}
        </div>
        <dl className="space-y-3">
          <DetailRow label="Shipment ID" value={detail.shipment_id} mono />
          <DetailRow
            label="Order ID"
            value={
              <button
                type="button"
                onClick={() => onNavigate?.(navigateToOrderDetail(detail.order_id))}
                className="font-mono text-xs text-primary hover:underline"
              >
                {detail.order_id}
              </button>
            }
          />
          <DetailRow label="Carrier" value={detail.carrier} />
          <DetailRow label="GHN order code" value={detail.ghn_order_code} mono />
          <DetailRow label="Tracking" value={detail.tracking_number} mono />
          <DetailRow label="Phí ship" value={formatVndPrice(detail.shipping_fee)} />
          <DetailRow label="COD" value={formatVndPrice(detail.cod_amount)} />
          <DetailRow label="Gửi hàng" value={formatDateTime(detail.shipped_at)} />
          <DetailRow label="Giao hàng" value={formatDateTime(detail.delivered_at)} />
        </dl>
        {detail.ghn_order_code ? (
          <button
            type="button"
            onClick={() =>
              onNavigate?.(
                navigateToWebhookLogs({
                  provider: "GHN",
                  reference_id: detail.ghn_order_code,
                }),
              )
            }
            className="mt-4 text-sm font-medium text-primary hover:underline"
          >
            Xem webhook GHN
          </button>
        ) : null}
      </AccountCard>

      <ShipmentStatusOverrideCard
        shipmentId={shipmentId}
        detail={detail}
        canWriteShipment={canWriteShipment}
        canForceWriteShipment={canForceWriteShipment}
        onSuccess={() => fetchDetail()}
        onNotify={onNotify}
      />

      {address ? (
        <AccountCard>
          <h3 className="mb-3 text-base font-semibold text-on-surface">Địa chỉ giao hàng</h3>
          <dl className="space-y-2">
            <DetailRow label="Người nhận" value={address.receiver_name} />
            <DetailRow label="Số điện thoại" value={address.phone} />
            <DetailRow label="Địa chỉ" value={address.full_address || address.address_detail} />
          </dl>
          {detail.contact_fields_masked ? (
            <p className="mt-3 text-xs text-on-surface-variant">{ORDER_SUPPORT_MASKED_NOTICE}</p>
          ) : null}
        </AccountCard>
      ) : null}

      {detail.order_items?.length > 0 ? (
        <AccountCard>
          <h3 className="mb-3 text-base font-semibold text-on-surface">Sản phẩm trong vận đơn</h3>
          <ul className="divide-y divide-outline-variant">
            {detail.order_items.map((item) => (
              <li key={item.order_item_id} className="flex justify-between gap-4 py-2 text-sm">
                <span>{item.product_name_snapshot}</span>
                <span className="text-on-surface-variant">x{item.quantity}</span>
              </li>
            ))}
          </ul>
        </AccountCard>
      ) : null}

      {detail.status_history?.length > 0 ? (
        <AccountCard>
          <h3 className="mb-3 text-base font-semibold text-on-surface">Lịch sử trạng thái</h3>
          <ul className="space-y-3">
            {detail.status_history.map((entry, index) => (
              <li key={`${entry.occurred_at}-${index}`} className="border-l-2 border-primary/30 pl-4">
                <p className="text-sm font-medium text-on-surface">
                  {entry.old_status || "—"} → {entry.new_status}
                  {entry.raw_status ? ` (${entry.raw_status})` : ""}
                </p>
                <p className="text-xs text-on-surface-variant">{formatDateTime(entry.occurred_at)}</p>
              </li>
            ))}
          </ul>
        </AccountCard>
      ) : null}

      {detail.carrier_webhook_events?.length > 0 ? (
        <AccountCard>
          <h3 className="mb-3 text-base font-semibold text-on-surface">Sự kiện webhook carrier</h3>
          <div className="overflow-x-auto">
            <table className="w-full min-w-[480px] text-left text-sm">
              <thead>
                <tr className="border-b border-outline-variant text-on-surface-variant">
                  <th className="py-2 pr-4 font-medium">Carrier status</th>
                  <th className="py-2 pr-4 font-medium">Processed</th>
                  <th className="py-2 font-medium">Nhận lúc</th>
                </tr>
              </thead>
              <tbody>
                {detail.carrier_webhook_events.map((event, index) => (
                  <tr key={`${event.received_at}-${index}`} className="border-b border-outline-variant/60">
                    <td className="py-3 pr-4">{event.carrier_status}</td>
                    <td className="py-3 pr-4">{event.processed ? "Có" : "Chưa"}</td>
                    <td className="py-3">{formatDateTime(event.received_at)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </AccountCard>
      ) : null}
    </div>
  );
}

export function ShipmentSupportDetailTab({
  shipmentId,
  shipmentListFilters,
  onShipmentListFiltersChange,
  onShipmentSelect,
  onNavigate,
  onNotify,
}) {
  return (
    <div className="space-y-6">
      <TabPanelHeader title={ORDER_SUPPORT_SHIPMENT_TITLE} subtitle={ORDER_SUPPORT_SHIPMENT_SUBTITLE} />

      <ShipmentSupportListPanel
        shipmentListFilters={shipmentListFilters}
        onFiltersChange={onShipmentListFiltersChange}
        selectedShipmentId={shipmentId}
        onShipmentSelect={onShipmentSelect}
      />

      <ShipmentDetailPanel
        shipmentId={shipmentId}
        onNavigate={onNavigate}
        onNotify={onNotify}
      />
    </div>
  );
}
