const CANCELLABLE_ORDER_STATUSES = new Set(["CREATED", "AWAITING_PAYMENT"]);
const INACTIVE_SHIPMENT_STATUSES = new Set(["PENDING", "CANCELLED"]);

export function canCancelOrder(order) {
  if (!order) return false;
  if (!CANCELLABLE_ORDER_STATUSES.has(order.orderStatus)) return false;
  if (order.orderPaymentStatus !== "PENDING") return false;

  const shipments = order.shipments || [];
  if (!shipments.length) return true;

  return shipments.every((shipment) => INACTIVE_SHIPMENT_STATUSES.has(shipment.status));
}

export function canConfirmOrderReceived(order) {
  if (!order) return false;
  if (order.orderStatus === "COMPLETED" || order.orderStatus === "CANCELLED") {
    return false;
  }

  const hasDeliveredItem = (order.items || []).some((item) => item.status === "DELIVERED");
  if (!hasDeliveredItem) return false;

  if (order.paymentMethod === "COD") {
    return true;
  }

  return order.orderPaymentStatus === "PAID" || order.payment?.status === "PAID";
}

export function getCancelBlockReason(order) {
  if (!order) return "";
  if (order.orderStatus === "CANCELLED") return "Đơn đã được hủy.";
  if (!CANCELLABLE_ORDER_STATUSES.has(order.orderStatus)) {
    return "Chỉ hủy được khi đơn chưa thanh toán và chưa giao hàng.";
  }
  if (order.orderPaymentStatus !== "PENDING") {
    return "Đơn đã thanh toán, không thể hủy qua luồng này.";
  }
  const shipments = order.shipments || [];
  if (shipments.some((s) => !INACTIVE_SHIPMENT_STATUSES.has(s.status))) {
    return "Đơn đang được vận chuyển, không thể hủy.";
  }
  return "";
}
