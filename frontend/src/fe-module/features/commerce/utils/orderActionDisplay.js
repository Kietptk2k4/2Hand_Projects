const BLOCKING_SHIPMENT_STATUSES = new Set(["SHIPPED", "DELIVERED", "RETURNED"]);

function isBuyerCancellableOrderStatus(order) {
  const status = order.orderStatus;
  if (status === "CREATED" || status === "AWAITING_PAYMENT") {
    return true;
  }
  return status === "PROCESSING" && order.paymentMethod === "COD";
}

function hasBlockingShipment(order) {
  const shipments = order.shipments || [];
  if (!shipments.length) return false;
  return shipments.some((shipment) => BLOCKING_SHIPMENT_STATUSES.has(shipment.status));
}

export function isPendingRefund(order) {
  return Boolean(order?.activeRefundRequest);
}

export function canCancelOrder(order) {
  if (!order) return false;
  if (isPendingRefund(order)) return false;
  if (order.orderStatus === "CANCELLED") return false;

  if (
    order.paymentMethod === "VNPAY" &&
    order.orderPaymentStatus === "PAID" &&
    order.orderStatus === "PROCESSING"
  ) {
    return !hasBlockingShipment(order);
  }

  if (order.orderPaymentStatus !== "PENDING") return false;
  if (!isBuyerCancellableOrderStatus(order)) return false;
  return !hasBlockingShipment(order);
}

export function canConfirmOrderReceived(order) {
  if (!order) return false;
  if (isPendingRefund(order)) return false;
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
  if (isPendingRefund(order)) {
    return "Đơn đang chờ hoàn tiền — không thể hủy thêm.";
  }
  if (order.orderStatus === "CANCELLED") return "Đơn đã được hủy.";
  if (hasBlockingShipment(order)) {
    return "Đơn đang được vận chuyển, không thể hủy.";
  }
  if (
    order.paymentMethod === "VNPAY" &&
    order.orderPaymentStatus === "PAID" &&
    order.orderStatus === "PROCESSING"
  ) {
    return "";
  }
  if (order.orderPaymentStatus !== "PENDING") {
    return "Đơn đã thanh toán, không thể hủy qua luồng này.";
  }
  if (!isBuyerCancellableOrderStatus(order)) {
    return "Chỉ hủy được khi đơn chưa thanh toán và chưa giao hàng.";
  }
  return "";
}
