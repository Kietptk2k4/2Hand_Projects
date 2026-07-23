import { AdminSurfaceCard } from "../../components/ui";
import { ORDER_SUPPORT_MASKED_NOTICE } from "../constants/orderSupportUiStrings.js";
import { SupportCrossLinkButton } from "./ui/SupportCrossLinkButton.jsx";
import { SupportDetailRow } from "./ui/SupportDetailRow.jsx";
import { SupportTimeline } from "./ui/SupportTimeline.jsx";
import { SupportForbiddenState } from "./SupportForbiddenState.jsx";
import { SupportStatusBadge } from "./SupportStatusBadge.jsx";

export function OrderSupportDetailPanelView({
  detail,
  canReadOrder,
  formatDateTime,
  formatVndPrice,
  onNavigateToPayment,
  onNavigateToShipment,
  viewMode = "summary",
}) {
  if (!detail) return null;

  const showSummary = viewMode === "summary" || !viewMode;
  const showItems = viewMode === "items";
  const showTimeline = viewMode === "timeline";

  return (
    <div className="space-y-4">
      {!canReadOrder ? (
        <SupportForbiddenState message="Tài khoản thiếu quyền ORDER_SUPPORT_READ." />
      ) : null}

      {showSummary ? (
        <>
          <AdminSurfaceCard padding="lg">
            <div className="mb-4 flex flex-wrap items-center gap-2">
              <SupportStatusBadge status={detail.order_status} />
              <SupportStatusBadge status={detail.order_payment_status} kind="payment" />
              {detail.payment_method ? (
                <span className="text-xs text-admin-text-muted">{detail.payment_method}</span>
              ) : null}
            </div>
            <dl className="space-y-3">
              <SupportDetailRow label="Order ID" value={detail.order_id} mono />
              <SupportDetailRow label="Buyer ID" value={detail.buyer_id} mono />
              <SupportDetailRow label="Tổng tiền" value={formatVndPrice(detail.total_amount)} />
              <SupportDetailRow label="Thanh toán" value={formatVndPrice(detail.final_amount)} />
              <SupportDetailRow label="Tạo lúc" value={formatDateTime(detail.created_at)} />
              <SupportDetailRow label="Cập nhật" value={formatDateTime(detail.updated_at)} />
              <SupportDetailRow label="Hoàn thành" value={formatDateTime(detail.completed_at)} />
              {detail.cancellation_note ? (
                <SupportDetailRow label="Lý do hủy" value={detail.cancellation_note} />
              ) : null}
            </dl>
          </AdminSurfaceCard>

          {detail.active_refund_request ? (
            <AdminSurfaceCard padding="lg">
              <h3 className="mb-3 text-base font-semibold text-admin-text">Yêu cầu hoàn tiền</h3>
              <dl className="space-y-2">
                <SupportDetailRow
                  label="Trạng thái"
                  value={detail.active_refund_request.status || "—"}
                />
                <SupportDetailRow
                  label="Số tiền"
                  value={formatVndPrice(detail.active_refund_request.amount)}
                />
              </dl>
            </AdminSurfaceCard>
          ) : null}

          {detail.payment ? (
            <AdminSurfaceCard padding="lg">
              <div className="mb-3 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <h3 className="text-base font-semibold text-admin-text">Thanh toán</h3>
                <SupportCrossLinkButton onClick={onNavigateToPayment}>
                  Mở chi tiết thanh toán
                </SupportCrossLinkButton>
              </div>
              <dl className="space-y-2">
                <SupportDetailRow label="Payment ID" value={detail.payment.payment_id} mono />
                <SupportDetailRow label="Trạng thái" value={detail.payment.status} />
                <SupportDetailRow label="Số tiền" value={formatVndPrice(detail.payment.amount)} />
              </dl>
            </AdminSurfaceCard>
          ) : null}

          {detail.shipments?.length > 0 ? (
            <AdminSurfaceCard padding="lg">
              <h3 className="mb-3 text-base font-semibold text-admin-text">Vận chuyển</h3>
              <ul className="divide-y divide-admin-border-subtle">
                {detail.shipments.map((shipment) => (
                  <li
                    key={shipment.shipment_id}
                    className="flex flex-col gap-3 py-3 sm:flex-row sm:items-center sm:justify-between"
                  >
                    <div className="min-w-0">
                      <p className="font-mono text-xs text-admin-text">{shipment.shipment_id}</p>
                      <p className="text-sm text-admin-text-secondary">
                        {shipment.carrier || "—"} · {shipment.tracking_number || "Chưa có tracking"}
                      </p>
                      <SupportStatusBadge status={shipment.status} className="mt-1" />
                    </div>
                    <SupportCrossLinkButton
                      onClick={() => onNavigateToShipment?.(shipment.shipment_id)}
                    >
                      Mở chi tiết vận chuyển
                    </SupportCrossLinkButton>
                  </li>
                ))}
              </ul>
              {detail.contact_fields_masked ? (
                <p className="mt-3 text-xs text-admin-text-muted">{ORDER_SUPPORT_MASKED_NOTICE}</p>
              ) : null}
            </AdminSurfaceCard>
          ) : null}
        </>
      ) : null}

      {showItems && detail.items?.length > 0 ? (
        <AdminSurfaceCard padding="lg" className="max-w-full min-w-0">
          <h3 className="mb-3 text-base font-semibold text-admin-text">Sản phẩm trong đơn</h3>
          <div className="overflow-x-auto">
            <table className="w-full min-w-[640px] text-left text-sm">
              <thead>
                <tr className="border-b border-admin-border text-admin-text-secondary">
                  <th className="py-2 pr-4 font-medium">Sản phẩm</th>
                  <th className="py-2 pr-4 font-medium">Shop</th>
                  <th className="py-2 pr-4 font-medium">SL</th>
                  <th className="py-2 pr-4 font-medium">Giá</th>
                  <th className="py-2 font-medium">Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                {detail.items.map((item) => (
                  <tr key={item.order_item_id} className="border-b border-admin-border-subtle">
                    <td className="py-3 pr-4 text-admin-text">{item.product_name_snapshot || "—"}</td>
                    <td className="py-3 pr-4 text-admin-text-secondary">
                      {item.shop_name_snapshot || "—"}
                    </td>
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
        </AdminSurfaceCard>
      ) : showItems ? (
        <p className="text-sm text-admin-text-muted">Không có sản phẩm trong đơn.</p>
      ) : null}

      {showTimeline && detail.order_timeline?.length > 0 ? (
        <AdminSurfaceCard padding="lg">
          <SupportTimeline
            title="Timeline đơn hàng"
            entries={detail.order_timeline}
            formatDateTime={formatDateTime}
          />
        </AdminSurfaceCard>
      ) : showTimeline ? (
        <p className="text-sm text-admin-text-muted">Chưa có timeline đơn hàng.</p>
      ) : null}
    </div>
  );
}
