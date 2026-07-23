import { SHIPMENT_SUPPORT_VIEW_MODES } from "../constants/shipmentSupportListConstants.js";
import { formatCarrierLabel } from "../utils/orderSupportDisplayUtils.js";
import { AdminSurfaceCard } from "../../components/ui";
import { ORDER_SUPPORT_MASKED_NOTICE } from "../constants/orderSupportUiStrings.js";
import { SupportCrossLinkButton } from "./ui/SupportCrossLinkButton.jsx";
import { SupportDetailRow } from "./ui/SupportDetailRow.jsx";
import { SupportTimeline } from "./ui/SupportTimeline.jsx";
import { SupportForbiddenState } from "./SupportForbiddenState.jsx";
import { SupportStatusBadge } from "./SupportStatusBadge.jsx";
import { ShipmentStatusOverrideCard } from "./ShipmentStatusOverrideCard.jsx";

export function ShipmentSupportDetailPanelView({
  detail,
  shipmentId,
  canReadShipment,
  canWriteShipment,
  canForceWriteShipment,
  formatDateTime,
  formatVndPrice,
  onNavigateToOrder,
  onNavigateToWebhook,
  onOverrideSuccess,
  onNotify,
  viewMode = SHIPMENT_SUPPORT_VIEW_MODES.SUMMARY,
}) {
  if (!detail) return null;

  const address = detail.shipping_address;
  const showSummary =
    viewMode === SHIPMENT_SUPPORT_VIEW_MODES.SUMMARY || viewMode === "summary" || !viewMode;
  const showTimeline = viewMode === SHIPMENT_SUPPORT_VIEW_MODES.TIMELINE;
  const showWebhooks = viewMode === SHIPMENT_SUPPORT_VIEW_MODES.WEBHOOKS;
  const showOverride = viewMode === SHIPMENT_SUPPORT_VIEW_MODES.OVERRIDE;

  return (
    <div className="space-y-4">
      {!canReadShipment ? (
        <SupportForbiddenState message="Tài khoản thiếu quyền SHIPMENT_SUPPORT_READ." />
      ) : null}

      {showSummary ? (
        <>
          <AdminSurfaceCard padding="lg">
            <div className="mb-4 flex flex-wrap items-center gap-2">
              <SupportStatusBadge status={detail.internal_status} kind="shipment" />
              {detail.carrier_status ? (
                <span className="inline-flex items-center rounded-md bg-admin-surface-muted px-2 py-0.5 text-xs font-medium text-admin-text-secondary">
                  Carrier: {detail.carrier_status}
                </span>
              ) : null}
            </div>
            <dl className="space-y-3">
              <SupportDetailRow label="Shipment ID" value={detail.shipment_id} mono />
              <SupportDetailRow
                label="Order ID"
                value={
                  <button
                    type="button"
                    onClick={onNavigateToOrder}
                    className="break-all font-mono text-xs text-admin-accent hover:underline"
                  >
                    {detail.order_id}
                  </button>
                }
              />
              <SupportDetailRow label="Đơn vị" value={formatCarrierLabel(detail.carrier)} />
              <SupportDetailRow label="GHN order code" value={detail.ghn_order_code} mono />
              <SupportDetailRow label="Tracking" value={detail.tracking_number} mono />
              <SupportDetailRow label="Phí ship" value={formatVndPrice(detail.shipping_fee)} />
              <SupportDetailRow label="COD" value={formatVndPrice(detail.cod_amount)} />
              <SupportDetailRow label="Gửi hàng" value={formatDateTime(detail.shipped_at)} />
              <SupportDetailRow label="Giao hàng" value={formatDateTime(detail.delivered_at)} />
            </dl>
            {detail.ghn_order_code ? (
              <SupportCrossLinkButton onClick={onNavigateToWebhook} className="mt-4">
                Xem webhook GHN
              </SupportCrossLinkButton>
            ) : null}
          </AdminSurfaceCard>

          {address ? (
            <AdminSurfaceCard padding="lg">
              <h3 className="mb-3 text-base font-semibold text-admin-text">Địa chỉ giao hàng</h3>
              <dl className="space-y-2">
                <SupportDetailRow label="Người nhận" value={address.receiver_name} />
                <SupportDetailRow label="Số điện thoại" value={address.phone} />
                <SupportDetailRow
                  label="Địa chỉ"
                  value={address.full_address || address.address_detail}
                />
              </dl>
              {detail.contact_fields_masked ? (
                <p className="mt-3 text-xs text-admin-text-muted">{ORDER_SUPPORT_MASKED_NOTICE}</p>
              ) : null}
            </AdminSurfaceCard>
          ) : null}

          {detail.order_items?.length > 0 ? (
            <AdminSurfaceCard padding="lg">
              <h3 className="mb-3 text-base font-semibold text-admin-text">Sản phẩm trong vận đơn</h3>
              <ul className="divide-y divide-admin-border-subtle">
                {detail.order_items.map((item) => (
                  <li key={item.order_item_id} className="flex justify-between gap-4 py-2 text-sm">
                    <span className="text-admin-text">{item.product_name_snapshot}</span>
                    <span className="text-admin-text-secondary">x{item.quantity}</span>
                  </li>
                ))}
              </ul>
            </AdminSurfaceCard>
          ) : null}
        </>
      ) : null}

      {showTimeline && detail.status_history?.length > 0 ? (
        <AdminSurfaceCard padding="lg">
          <SupportTimeline
            title="Lịch sử trạng thái"
            entries={detail.status_history}
            formatDateTime={formatDateTime}
          />
        </AdminSurfaceCard>
      ) : null}

      {showTimeline && !detail.status_history?.length ? (
        <p className="text-sm text-admin-text-secondary">Chưa có lịch sử trạng thái.</p>
      ) : null}

      {showWebhooks ? (
        <AdminSurfaceCard padding="lg" className="max-w-full min-w-0">
          <div className="mb-3 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
            <h3 className="text-base font-semibold text-admin-text">Sự kiện webhook carrier</h3>
            {detail.ghn_order_code ? (
              <SupportCrossLinkButton onClick={onNavigateToWebhook}>
                Xem trong Webhook logs
              </SupportCrossLinkButton>
            ) : null}
          </div>
          {detail.carrier_webhook_events?.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[480px] text-left text-sm">
                <thead>
                  <tr className="border-b border-admin-border text-admin-text-secondary">
                    <th className="py-2 pr-4 font-medium">Carrier status</th>
                    <th className="py-2 pr-4 font-medium">Processed</th>
                    <th className="py-2 font-medium">Nhận lúc</th>
                  </tr>
                </thead>
                <tbody>
                  {detail.carrier_webhook_events.map((event, index) => (
                    <tr
                      key={`${event.received_at}-${index}`}
                      className="border-b border-admin-border-subtle"
                    >
                      <td className="py-3 pr-4">{event.carrier_status}</td>
                      <td className="py-3 pr-4">{event.processed ? "Có" : "Chưa"}</td>
                      <td className="py-3">{formatDateTime(event.received_at)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-sm text-admin-text-secondary">Chưa có sự kiện webhook.</p>
          )}
        </AdminSurfaceCard>
      ) : null}

      {showOverride && canWriteShipment ? (
        <ShipmentStatusOverrideCard
          shipmentId={shipmentId}
          detail={detail}
          canWriteShipment={canWriteShipment}
          canForceWriteShipment={canForceWriteShipment}
          onSuccess={onOverrideSuccess}
          onNotify={onNotify}
        />
      ) : null}
    </div>
  );
}
