function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

function mapTimeline(events) {
  return (events || []).map((event, index) => ({
    oldStatus: pick(event, "oldStatus", "old_status"),
    newStatus: pick(event, "newStatus", "new_status"),
    rawStatus: pick(event, "rawStatus", "raw_status"),
    occurredAt: pick(event, "occurredAt", "occurred_at"),
    id: `sh-track-${index}-${pick(event, "occurredAt", "occurred_at")}`,
  }));
}

export function mapShipmentTrackResponse(data) {
  if (!data) return null;

  const status = data.status;

  return {
    shipmentId: pick(data, "shipmentId", "shipment_id"),
    orderId: pick(data, "orderId", "order_id"),
    sellerId: pick(data, "sellerId", "seller_id"),
    accessedAs: pick(data, "accessedAs", "accessed_as"),
    status,
    carrier: data.carrier,
    shipmentType: pick(data, "shipmentType", "shipment_type"),
    trackingNumber: pick(data, "trackingNumber", "tracking_number"),
    ghnOrderCode: pick(data, "ghnOrderCode", "ghn_order_code"),
    shippedAt: pick(data, "shippedAt", "shipped_at"),
    deliveredAt: pick(data, "deliveredAt", "delivered_at"),
    estimatedDeliveryDate: pick(data, "estimatedDeliveryDate", "estimated_delivery_date"),
    orderStatus: pick(data, "orderStatus", "order_status"),
    shipmentDelivered: Boolean(
      data.shipmentDelivered ?? data.shipment_delivered ?? status === "DELIVERED",
    ),
    orderCompleted: Boolean(data.orderCompleted ?? data.order_completed),
    timeline: mapTimeline(data.timeline),
  };
}
