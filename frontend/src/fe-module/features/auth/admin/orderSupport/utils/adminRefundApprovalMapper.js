import { formatVndPrice } from "../../../../social/utils/formatPrice";

export const REFUND_STATUS_LABELS = {
  REQUESTED: "Chờ duyệt",
  CONFIRMED: "Đã hoàn tiền",
  REJECTED: "Từ chối",
};

export const REFUND_REQUESTED_BY_LABELS = {
  BUYER: "Người mua",
  SELLER: "Người bán",
};

export function mapRefundApprovalItem(raw) {
  if (!raw) return null;
  return {
    id: raw.id,
    orderId: raw.order_id ?? raw.orderId,
    paymentId: raw.payment_id ?? raw.paymentId,
    buyerId: raw.buyer_id ?? raw.buyerId,
    requestedBy: raw.requested_by ?? raw.requestedBy,
    requestedByUserId: raw.requested_by_user_id ?? raw.requestedByUserId,
    amount: Number(raw.amount) || 0,
    amountLabel: formatVndPrice(Number(raw.amount) || 0),
    status: raw.status,
    reason: raw.reason,
    adminNote: raw.admin_note ?? raw.adminNote,
    paymentMethod: raw.payment_method ?? raw.paymentMethod,
    orderPaymentStatus: raw.order_payment_status ?? raw.orderPaymentStatus,
    orderStatus: raw.order_status ?? raw.orderStatus,
    requestedAt: raw.requested_at ?? raw.requestedAt,
    confirmedAt: raw.confirmed_at ?? raw.confirmedAt,
    rejectedAt: raw.rejected_at ?? raw.rejectedAt,
  };
}

export function mapRefundApprovalQueueResponse(raw) {
  const pagination = raw?.pagination ?? {};
  return {
    items: (raw?.items ?? []).map(mapRefundApprovalItem).filter(Boolean),
    pagination: {
      page: Number(pagination.page) || 1,
      totalItems: Number(pagination.total_items ?? pagination.totalItems) || 0,
    },
  };
}