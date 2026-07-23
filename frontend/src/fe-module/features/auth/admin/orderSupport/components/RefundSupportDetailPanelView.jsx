import { AdminSurfaceCard } from "../../components/ui";
import { REFUND_SUPPORT_VIEW_MODES } from "../constants/refundSupportListConstants.js";
import {
  formatOrderStatusLabel,
  formatPaymentMethodLabel,
  formatPaymentStatusLabel,
} from "../utils/orderSupportDisplayUtils.js";
import {
  formatRefundRequestedByLabel,
  formatRefundStatusLabel,
} from "../utils/refundSupportFilterHelpers.js";
import { SupportCrossLinkButton } from "./ui/SupportCrossLinkButton.jsx";
import { SupportDetailRow } from "./ui/SupportDetailRow.jsx";
import { OrderSupportUuidCell } from "./OrderSupportUuidCell.jsx";

export function RefundSupportDetailPanelView({
  detail,
  refundRequestId,
  viewMode,
  formatDateTime,
  formatVndPrice,
  onNavigateToOrder,
  onNavigateToPayment,
  onCopied,
}) {
  if (!detail) return null;

  const isDetail = viewMode === REFUND_SUPPORT_VIEW_MODES.DETAIL;
  const isNote = viewMode === REFUND_SUPPORT_VIEW_MODES.NOTE;

  if (isNote) {
    return (
      <AdminSurfaceCard padding="md" className="bg-admin-surface-muted/50">
        <h3 className="mb-2 text-sm font-semibold text-admin-text">Ghi chú admin</h3>
        <p className="text-sm text-admin-text">{detail.adminNote || "Chưa có ghi chú."}</p>
      </AdminSurfaceCard>
    );
  }

  if (isDetail) {
    return (
      <div className="space-y-4">
        <div className="grid gap-4 sm:grid-cols-2">
          <SupportDetailRow label="TT thanh toán đơn" value={formatPaymentStatusLabel(detail.orderPaymentStatus)} />
          <SupportDetailRow label="TT đơn hàng" value={formatOrderStatusLabel(detail.orderStatus)} />
          <SupportDetailRow label="Lý do" value={detail.reason} />
          <SupportDetailRow label="Người mua ID" value={detail.buyerId} mono />
          <SupportDetailRow label="Người yêu cầu ID" value={detail.requestedByUserId} mono />
        </div>
        <div className="flex flex-wrap gap-2">
          {detail.orderId ? (
            <SupportCrossLinkButton onClick={onNavigateToOrder}>
              Xem chi tiết đơn hàng
            </SupportCrossLinkButton>
          ) : null}
          {detail.paymentId ? (
            <SupportCrossLinkButton onClick={onNavigateToPayment}>
              Xem chi tiết thanh toán
            </SupportCrossLinkButton>
          ) : null}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="grid gap-4 sm:grid-cols-2">
        <SupportDetailRow label="Trạng thái" value={formatRefundStatusLabel(detail.status)} />
        <SupportDetailRow label="Số tiền" value={formatVndPrice(detail.amount)} />
        <SupportDetailRow
          label="Người yêu cầu"
          value={formatRefundRequestedByLabel(detail.requestedBy)}
        />
        <SupportDetailRow label="Phương thức" value={formatPaymentMethodLabel(detail.paymentMethod)} />
        <SupportDetailRow label="Thời gian yêu cầu" value={formatDateTime(detail.requestedAt)} />
        <SupportDetailRow label="Xác nhận lúc" value={formatDateTime(detail.confirmedAt)} />
        <SupportDetailRow label="Từ chối lúc" value={formatDateTime(detail.rejectedAt)} />
      </div>
      <div className="space-y-3">
        <div>
          <p className="text-xs font-medium text-admin-text-muted">Refund ID</p>
          <OrderSupportUuidCell value={refundRequestId || detail.id} onCopied={onCopied} />
        </div>
        <div>
          <p className="text-xs font-medium text-admin-text-muted">Order ID</p>
          <OrderSupportUuidCell value={detail.orderId} onCopied={onCopied} />
        </div>
        <div>
          <p className="text-xs font-medium text-admin-text-muted">Payment ID</p>
          <OrderSupportUuidCell value={detail.paymentId} onCopied={onCopied} />
        </div>
      </div>
    </div>
  );
}
