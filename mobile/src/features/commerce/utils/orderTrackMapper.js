function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

function mapTimeline(events) {
  return (events || []).map((event, index) => ({
    oldStatus: pick(event, "oldStatus", "old_status"),
    newStatus: pick(event, "newStatus", "new_status"),
    changedBy: pick(event, "changedBy", "changed_by"),
    note: pick(event, "note", "note"),
    occurredAt: pick(event, "occurredAt", "occurred_at"),
    id: `track-${index}-${pick(event, "occurredAt", "occurred_at")}`,
  }));
}

function mapPayment(payment) {
  if (!payment) return null;

  return {
    paymentId: pick(payment, "paymentId", "payment_id"),
    status: payment.status,
    paymentMethod: pick(payment, "paymentMethod", "payment_method"),
    paidAt: pick(payment, "paidAt", "paid_at"),
    expiredAt: pick(payment, "expiredAt", "expired_at"),
    timeline: mapTimeline(payment.timeline),
  };
}

function mapItems(items) {
  return (items || []).map((item) => ({
    orderItemId: pick(item, "orderItemId", "order_item_id"),
    productId: pick(item, "productId", "product_id"),
    sellerId: pick(item, "sellerId", "seller_id"),
    productName: pick(item, "productName", "product_name"),
    quantity: item.quantity,
    status: item.status,
    shipmentId: pick(item, "shipmentId", "shipment_id"),
    completedAt: pick(item, "completedAt", "completed_at"),
  }));
}

function mapShipments(shipments) {
  return (shipments || []).map((shipment) => ({
    shipmentId: pick(shipment, "shipmentId", "shipment_id"),
    sellerId: pick(shipment, "sellerId", "seller_id"),
    status: shipment.status,
    carrier: shipment.carrier,
    trackingNumber: pick(shipment, "trackingNumber", "tracking_number"),
    shippedAt: pick(shipment, "shippedAt", "shipped_at"),
    deliveredAt: pick(shipment, "deliveredAt", "delivered_at"),
    timeline: mapTimeline(shipment.timeline),
  }));
}

export function mapOrderTrackResponse(data) {
  if (!data) return null;

  return {
    orderId: pick(data, "orderId", "order_id"),
    orderStatus: pick(data, "orderStatus", "order_status"),
    orderPaymentStatus: pick(data, "orderPaymentStatus", "order_payment_status"),
    paymentMethod: pick(data, "paymentMethod", "payment_method"),
    totalAmount: pick(data, "totalAmount", "total_amount"),
    finalAmount: pick(data, "finalAmount", "final_amount"),
    createdAt: pick(data, "createdAt", "created_at"),
    updatedAt: pick(data, "updatedAt", "updated_at"),
    completedAt: pick(data, "completedAt", "completed_at"),
    orderCompleted: Boolean(data.orderCompleted ?? data.order_completed),
    paymentPaid: Boolean(data.paymentPaid ?? data.payment_paid),
    allItemsCompleted: Boolean(data.allItemsCompleted ?? data.all_items_completed),
    anyShipmentDelivered: Boolean(data.anyShipmentDelivered ?? data.any_shipment_delivered),
    anyItemDelivered: Boolean(data.anyItemDelivered ?? data.any_item_delivered),
    payment: mapPayment(data.payment),
    items: mapItems(data.items),
    shipments: mapShipments(data.shipments),
    orderTimeline: mapTimeline(data.orderTimeline ?? data.order_timeline),
  };
}
