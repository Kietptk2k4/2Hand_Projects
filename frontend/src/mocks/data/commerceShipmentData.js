import { buildDetailFromSummary } from "./commerceOrderDetailData";
import { findOrderSummaryForUser } from "./commerceOrderListData";

function offsetIso(baseIso, hours) {
  const date = new Date(baseIso);
  date.setHours(date.getHours() + hours);
  return date.toISOString();
}

function orderIdFromShipmentId(shipmentId) {
  const suffix = shipmentId.replace(/^sh-/, "");
  if (!/^\d{6}$/.test(suffix)) return null;
  return `o2000000-0000-4000-8000-000000${suffix}`;
}

function findShipmentForUser(userId, shipmentId) {
  const orderId = orderIdFromShipmentId(shipmentId);
  if (!orderId) return null;

  const summary = findOrderSummaryForUser(userId, orderId);
  if (!summary) return null;

  const detail = buildDetailFromSummary(summary);
  const shipment = detail.shipments.find((item) => item.shipment_id === shipmentId);
  if (!shipment) return null;

  return { summary, detail, shipment };
}

function buildStatusHistory(shipment, createdAt) {
  const status = shipment.status;
  const history = [
    {
      old_status: null,
      new_status: "CREATED",
      raw_status: "created",
      occurred_at: createdAt,
    },
  ];

  if (["SHIPPED", "DELIVERED"].includes(status)) {
    history.push({
      old_status: "CREATED",
      new_status: "SHIPPED",
      raw_status: "picked",
      occurred_at: shipment.shipped_at || offsetIso(createdAt, 24),
    });
  }

  if (status === "DELIVERED") {
    history.push({
      old_status: "SHIPPED",
      new_status: "DELIVERED",
      raw_status: "delivering",
      occurred_at: offsetIso(shipment.shipped_at || createdAt, 12),
    });
    history.push({
      old_status: "SHIPPED",
      new_status: "DELIVERED",
      raw_status: "delivered",
      occurred_at: shipment.delivered_at || offsetIso(createdAt, 48),
    });
  }

  return history;
}

function buildTrackingTimeline(shipment, createdAt) {
  const status = shipment.status;
  const timeline = [
    {
      oldStatus: null,
      newStatus: "CREATED",
      rawStatus: "created",
      occurredAt: createdAt,
    },
  ];

  if (["SHIPPED", "DELIVERED"].includes(status)) {
    timeline.push({
      oldStatus: "CREATED",
      newStatus: "SHIPPED",
      rawStatus: "picked",
      occurredAt: shipment.shipped_at || offsetIso(createdAt, 24),
    });
  }

  if (status === "DELIVERED") {
    timeline.push({
      oldStatus: "SHIPPED",
      newStatus: "DELIVERED",
      rawStatus: "delivering",
      occurredAt: offsetIso(shipment.shipped_at || createdAt, 12),
    });
    timeline.push({
      oldStatus: "SHIPPED",
      newStatus: "DELIVERED",
      rawStatus: "delivered",
      occurredAt: shipment.delivered_at || offsetIso(createdAt, 48),
    });
  }

  return timeline;
}

function resolveCodAmount(summary) {
  if (summary.payment_method !== "COD") return 0;
  return summary.final_amount;
}

function resolveWeightGram(itemCount) {
  return Math.max(500, (itemCount || 1) * 350);
}

export function getShipmentDetailForUser(userId, shipmentId) {
  const found = findShipmentForUser(userId, shipmentId);
  if (!found) {
    return { error: "COMMERCE-404-SHIPMENT", status: 404 };
  }

  const { summary, detail, shipment } = found;
  const orderItems = detail.items.filter((item) => item.shipment_id === shipmentId);
  const createdAt = summary.created_at;

  return {
    data: {
      shipment_id: shipment.shipment_id,
      order_id: summary.order_id,
      seller_id: shipment.seller_id,
      accessed_as: "BUYER",
      carrier: shipment.carrier,
      shipment_type: shipment.shipment_type,
      status: shipment.status,
      ghn_order_code: `GHN-${summary.order_id.slice(-6).toUpperCase()}`,
      tracking_number: shipment.tracking_number,
      shipping_fee: shipment.shipping_fee,
      cod_amount: resolveCodAmount(summary),
      weight_gram: resolveWeightGram(summary.item_count),
      estimated_delivery_date: shipment.estimated_delivery_date,
      shipped_at: shipment.shipped_at,
      delivered_at: shipment.delivered_at,
      created_at: createdAt,
      updated_at: summary.updated_at || createdAt,
      shipping_address: shipment.shipping_address,
      order_items: orderItems,
      status_history: buildStatusHistory(shipment, createdAt),
    },
  };
}

export function getShipmentTrackingForUser(userId, shipmentId) {
  const found = findShipmentForUser(userId, shipmentId);
  if (!found) {
    return { error: "COMMERCE-404-SHIPMENT", status: 404 };
  }

  const { summary, shipment } = found;
  const createdAt = summary.created_at;
  const status = shipment.status;

  return {
    data: {
      shipmentId: shipment.shipment_id,
      orderId: summary.order_id,
      sellerId: shipment.seller_id,
      accessedAs: "BUYER",
      status,
      carrier: shipment.carrier,
      shipmentType: shipment.shipment_type,
      trackingNumber: shipment.tracking_number,
      ghnOrderCode: `GHN-${summary.order_id.slice(-6).toUpperCase()}`,
      shippedAt: shipment.shipped_at,
      deliveredAt: shipment.delivered_at,
      estimatedDeliveryDate: shipment.estimated_delivery_date,
      orderStatus: summary.order_status,
      shipmentDelivered: status === "DELIVERED",
      orderCompleted: summary.order_status === "COMPLETED",
      timeline: buildTrackingTimeline(shipment, createdAt),
    },
  };
}
