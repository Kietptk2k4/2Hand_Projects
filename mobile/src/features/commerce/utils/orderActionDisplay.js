const INACTIVE_SHIPMENT_STATUSES = new Set(["PENDING", "CANCELLED"]);

function isBuyerCancellableOrderStatus(order) {
  const status = order.orderStatus;
  if (status === "CREATED" || status === "AWAITING_PAYMENT") {
    return true;
  }
  return status === "PROCESSING" && order.paymentMethod === "COD";
}

export function canCancelOrder(order) {
  if (!order) return false;
  if (order.orderPaymentStatus !== "PENDING") return false;
  if (!isBuyerCancellableOrderStatus(order)) return false;

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
  if (order.orderPaymentStatus !== "PENDING") {
    return "Đơn đã thanh toán, không thể hủy qua luồng này.";
  }
  if (!isBuyerCancellableOrderStatus(order)) {
    return "Chỉ hủy được khi đơn chưa thanh toán và chưa giao hàng.";
  }
  const shipments = order.shipments || [];
  if (shipments.some((s) => !INACTIVE_SHIPMENT_STATUSES.has(s.status))) {
    return "Đơn đang được vận chuyển, không thể hủy.";
  }
  return "";
}
