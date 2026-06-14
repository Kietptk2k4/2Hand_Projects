import {
  findOrderSummaryForUser,
  updateOrderSummaryForUser,
} from "./commerceOrderListData";
import { mockCommerceProducts } from "./commerceProductListData";
import { getReviewIdForOrderItem } from "./commerceProductReviewIndex";

const DEFAULT_SHIPPING_ADDRESS = {
  receiver_name: "Nguyễn Văn An",
  phone: "0901234567",
  province_code: "79",
  district_code: "760",
  ward_code: "26734",
  address_detail: "123 Nguyễn Văn Linh, Phường Tân Phong",
  full_address: "123 Nguyễn Văn Linh, Phường Tân Phong, Quận 7, TP. Hồ Chí Minh",
};

const refundRequestsByOrderId = new Map();

export function getActiveRefundRequestForOrder(orderId) {
  return refundRequestsByOrderId.get(orderId) || null;
}

function attachActiveRefundRequest(data) {
  const active = getActiveRefundRequestForOrder(data.order_id);
  if (!active) return data;
  return { ...data, active_refund_request: active };
}

function offsetIso(baseIso, hours) {
  const date = new Date(baseIso);
  date.setHours(date.getHours() + hours);
  return date.toISOString();
}

function buildItemsFromSummary(summary) {
  const count = Math.max(1, summary.item_count || 1);
  const items = [];
  const shippingPerItem =
    summary.final_amount > summary.total_amount
      ? Math.round((summary.final_amount - summary.total_amount) / count)
      : 0;

  for (let i = 0; i < count; i += 1) {
    const product = mockCommerceProducts[i % mockCommerceProducts.length];
    const unitPrice = Math.round(summary.total_amount / count);

    items.push({
      order_item_id: `oi-${summary.order_id.slice(-6)}-${i + 1}`,
      product_id: product.product_id,
      seller_id: `e1000000-0000-4000-8000-${product.shop_id.slice(-12)}`,
      shipment_id: summary.order_status === "AWAITING_PAYMENT" ? null : `sh-${summary.order_id.slice(-6)}`,
      quantity: 1,
      status: resolveItemStatus(summary, i),
      unit_price_snapshot: unitPrice,
      final_price: unitPrice,
      sku_snapshot: `SKU-${product.product_id.slice(-4)}`,
      product_name_snapshot:
        i === 0 ? summary.preview_product_name : product.title,
      image_snapshot: i === 0 ? summary.preview_image_url : product.thumbnail_url,
      attributes_snapshot: i === 0 ? '{"color":"Xanh"}' : null,
      shop_name_snapshot: product.shop_name,
      shipping_fee_allocated: shippingPerItem,
      completed_at: summary.order_status === "COMPLETED" ? summary.completed_at : null,
    });
  }

  return items;
}

function resolveItemStatus(summary, index) {
  if (summary.order_status === "CANCELLED") return "CANCELLED";
  if (summary.order_status === "AWAITING_PAYMENT" || summary.order_status === "CREATED") {
    return "PROCESSING";
  }
  if (summary.order_status === "COMPLETED") {
    return index === 0 ? "COMPLETED" : "DELIVERED";
  }
  const shipmentStatus = summary.shipment_summary?.statuses?.[0];
  if (shipmentStatus === "DELIVERED") return "DELIVERED";
  if (shipmentStatus === "SHIPPED") return "SHIPPED";
  return "PROCESSING";
}

function buildShipmentTimeline(createdAt, status) {
  const events = [
    {
      old_status: "CREATED",
      new_status: "SHIPPED",
      occurred_at: offsetIso(createdAt, 24),
    },
  ];
  if (status === "DELIVERED") {
    events.push({
      old_status: "SHIPPED",
      new_status: "DELIVERED",
      occurred_at: offsetIso(createdAt, 48),
    });
  }
  return events;
}

function buildShipmentsFromSummary(summary, items) {
  if (!summary.shipment_summary?.shipment_count) return [];

  const created = summary.created_at;
  const shipmentStatus = summary.shipment_summary.statuses?.[0] || "SHIPPED";
  const shippingFee = Math.max(0, summary.final_amount - summary.total_amount);

  return [
    {
      shipment_id: `sh-${summary.order_id.slice(-6)}`,
      seller_id: items[0]?.seller_id,
      status: shipmentStatus,
      carrier: "GHN",
      tracking_number: `GHN${summary.order_id.replace(/\D/g, "").slice(-10)}`,
      shipping_fee: shippingFee,
      shipment_type: "STANDARD",
      estimated_delivery_date: offsetIso(created, 72).slice(0, 10),
      shipped_at: ["SHIPPED", "DELIVERED"].includes(shipmentStatus)
        ? offsetIso(created, 24)
        : null,
      delivered_at: shipmentStatus === "DELIVERED" ? offsetIso(created, 48) : null,
      shipping_address: { ...DEFAULT_SHIPPING_ADDRESS },
      timeline: buildShipmentTimeline(created, shipmentStatus),
    },
  ];
}

function buildPaymentTimeline(summary, createdAt) {
  const events = [
    {
      old_status: null,
      new_status: "PENDING",
      occurred_at: createdAt,
    },
  ];

  if (summary.order_payment_status === "PAID") {
    events.push({
      old_status: "PENDING",
      new_status: "PAID",
      occurred_at: offsetIso(createdAt, 1),
    });
  }

  if (summary.order_payment_status === "FAILED") {
    events.push({
      old_status: "PENDING",
      new_status: "FAILED",
      occurred_at: offsetIso(createdAt, 2),
    });
  }

  if (summary.order_payment_status === "EXPIRED") {
    events.push({
      old_status: "PENDING",
      new_status: "EXPIRED",
      occurred_at: offsetIso(createdAt, 2),
    });
  }

  return events;
}

function buildPaymentBlock(summary) {
  const payment = summary.payment || {};
  const created = summary.created_at;
  const isPaid = summary.order_payment_status === "PAID";

  return {
    payment_id: payment.payment_id,
    status: payment.status || summary.order_payment_status,
    payment_method: payment.payment_method || summary.payment_method,
    amount: payment.amount ?? summary.final_amount,
    currency: payment.currency || "VND",
    paid_at: isPaid ? offsetIso(created, 1) : null,
    expired_at: summary.order_status === "AWAITING_PAYMENT" ? offsetIso(created, 48) : null,
    checkout_url_expired_at: null,
    timeline: buildPaymentTimeline(summary, created),
  };
}

function buildOrderTimeline(summary) {
  const created = summary.created_at;
  const events = [
    {
      old_status: null,
      new_status: "CREATED",
      changed_by: "SYSTEM",
      note: null,
      occurred_at: created,
    },
  ];

  if (summary.order_status === "AWAITING_PAYMENT") {
    events.push({
      old_status: "CREATED",
      new_status: "AWAITING_PAYMENT",
      changed_by: "SYSTEM",
      note: "Chờ thanh toán PayOS",
      occurred_at: offsetIso(created, 0.1),
    });
  }

  if (summary.order_status !== "CANCELLED" && summary.order_status !== "CREATED") {
    if (summary.order_status !== "AWAITING_PAYMENT" || summary.order_payment_status === "PAID") {
      events.push({
        old_status: "AWAITING_PAYMENT",
        new_status: "PROCESSING",
        changed_by: "SYSTEM",
        note: null,
        occurred_at: offsetIso(created, 2),
      });
    }
  }

  if (summary.shipment_summary?.statuses?.includes("SHIPPED")) {
    events.push({
      old_status: "PROCESSING",
      new_status: "PROCESSING",
      changed_by: "SYSTEM",
      note: "Đơn đã được giao cho đơn vị vận chuyển",
      occurred_at: offsetIso(created, 24),
    });
  }

  if (summary.shipment_summary?.statuses?.includes("DELIVERED")) {
    events.push({
      old_status: "PROCESSING",
      new_status: "PROCESSING",
      changed_by: "SYSTEM",
      note: "Hàng đã giao thành công",
      occurred_at: offsetIso(created, 48),
    });
  }

  if (summary.order_status === "COMPLETED") {
    events.push({
      old_status: "PROCESSING",
      new_status: "COMPLETED",
      changed_by: "BUYER",
      note: "Buyer xác nhận đã nhận hàng",
      occurred_at: summary.completed_at || offsetIso(created, 72),
    });
  }

  if (summary.order_status === "CANCELLED") {
    events.push({
      old_status: "AWAITING_PAYMENT",
      new_status: "CANCELLED",
      changed_by: "SYSTEM",
      note: "Đơn đã hủy",
      occurred_at: offsetIso(created, 6),
    });
  }

  return events;
}

export function buildDetailFromSummary(summary) {
  const items = buildItemsFromSummary(summary);
  const shipments = buildShipmentsFromSummary(summary, items);

  return {
    order_id: summary.order_id,
    buyer_id: summary.buyer_id,
    order_status: summary.order_status,
    order_payment_status: summary.order_payment_status,
    payment_method: summary.payment_method,
    total_amount: summary.total_amount,
    final_amount: summary.final_amount,
    created_at: summary.created_at,
    updated_at: summary.updated_at || summary.created_at,
    completed_at: summary.completed_at,
    payment: buildPaymentBlock(summary),
    items,
    shipments,
    order_timeline: buildOrderTimeline(summary),
  };
}

function computeTrackFlags(summary, items, shipments) {
  return {
    orderCompleted: summary.order_status === "COMPLETED",
    paymentPaid: summary.order_payment_status === "PAID",
    allItemsCompleted: items.length > 0 && items.every((item) => item.status === "COMPLETED"),
    anyShipmentDelivered: shipments.some((shipment) => shipment.status === "DELIVERED"),
    anyItemDelivered: items.some((item) => item.status === "DELIVERED"),
  };
}

export function buildTrackStatusFromSummary(summary) {
  const detail = buildDetailFromSummary(summary);
  const flags = computeTrackFlags(summary, detail.items, detail.shipments);

  return {
    orderId: summary.order_id,
    orderStatus: summary.order_status,
    orderPaymentStatus: summary.order_payment_status,
    paymentMethod: summary.payment_method,
    totalAmount: summary.total_amount,
    finalAmount: summary.final_amount,
    createdAt: summary.created_at,
    updatedAt: summary.updated_at || summary.created_at,
    completedAt: summary.completed_at,
    ...flags,
    payment: {
      paymentId: detail.payment.payment_id,
      status: detail.payment.status,
      paymentMethod: detail.payment.payment_method,
      paidAt: detail.payment.paid_at,
      expiredAt: detail.payment.expired_at,
      timeline: detail.payment.timeline.map((event) => ({
        oldStatus: event.old_status,
        newStatus: event.new_status,
        occurredAt: event.occurred_at,
      })),
    },
    items: detail.items.map((item) => ({
      orderItemId: item.order_item_id,
      productId: item.product_id,
      sellerId: item.seller_id,
      productName: item.product_name_snapshot,
      quantity: item.quantity,
      status: item.status,
      shipmentId: item.shipment_id,
      completedAt: item.completed_at,
    })),
    shipments: detail.shipments.map((shipment) => ({
      shipmentId: shipment.shipment_id,
      sellerId: shipment.seller_id,
      status: shipment.status,
      carrier: shipment.carrier,
      trackingNumber: shipment.tracking_number,
      shippedAt: shipment.shipped_at,
      deliveredAt: shipment.delivered_at,
      timeline: shipment.timeline.map((event) => ({
        oldStatus: event.old_status,
        newStatus: event.new_status,
        occurredAt: event.occurred_at,
      })),
    })),
    orderTimeline: detail.order_timeline.map((event) => ({
      oldStatus: event.old_status,
      newStatus: event.new_status,
      changedBy: event.changed_by,
      note: event.note,
      occurredAt: event.occurred_at,
    })),
  };
}

export function getOrderDetailForUser(userId, orderId) {
  const summary = findOrderSummaryForUser(userId, orderId);
  if (!summary) {
    return { error: "COMMERCE-404-ORDER", status: 404 };
  }
  const data = buildDetailFromSummary(summary);
  return { data: enrichOrderItemsWithReviewIds(attachActiveRefundRequest(data)) };
}

function enrichOrderItemsWithReviewIds(data) {
  return {
    ...data,
    items: (data.items || []).map((item) => ({
      ...item,
      review_id: getReviewIdForOrderItem(item.order_item_id),
    })),
  };
}

export function getOrderTrackStatusForUser(userId, orderId) {
  const summary = findOrderSummaryForUser(userId, orderId);
  if (!summary) {
    return { error: "COMMERCE-404-ORDER", status: 404 };
  }
  return { data: buildTrackStatusFromSummary(summary) };
}

const CANCELLABLE_ORDER_STATUSES = new Set(["CREATED", "AWAITING_PAYMENT", "PROCESSING"]);
const BLOCKING_SHIPMENT_STATUSES = new Set(["SHIPPED", "DELIVERED", "RETURNED", "IN_TRANSIT", "OUT_FOR_DELIVERY"]);

function hasBlockingShipment(summary) {
  const statuses = summary.shipment_summary?.statuses || [];
  return statuses.some((status) => BLOCKING_SHIPMENT_STATUSES.has(status));
}

export function queueRefundRequestForOrder(orderId, summary, requestedBy, reason) {
  const refundRequestId = `rf-${orderId.slice(-8)}`;
  const requestedAt = new Date().toISOString();
  const active = {
    refund_request_id: refundRequestId,
    status: "REQUESTED",
    requested_by: requestedBy,
    amount: summary.final_amount,
    requested_at: requestedAt,
  };
  refundRequestsByOrderId.set(orderId, active);
  return {
    data: {
      order_id: orderId,
      status: "PROCESSING",
      cancelled_at: requestedAt,
      pending_refund: true,
      refund_request_id: refundRequestId,
      reason,
    },
    message: "Yeu cau huy don da duoc ghi nhan. Don hang dang cho hoan tien.",
  };
}

export function cancelOrderForUser(userId, orderId, reason) {
  const summary = findOrderSummaryForUser(userId, orderId);
  if (!summary) {
    return { error: "COMMERCE-404-ORDER", status: 404 };
  }

  if (summary.order_status === "CANCELLED") {
    return {
      data: {
        order_id: summary.order_id,
        status: "CANCELLED",
        cancelled_at: summary.updated_at || summary.created_at,
        already_cancelled: true,
      },
      message: "Don hang da duoc huy truoc do.",
    };
  }

  if (getActiveRefundRequestForOrder(orderId)) {
    const active = getActiveRefundRequestForOrder(orderId);
    return {
      data: {
        order_id: orderId,
        status: "PROCESSING",
        cancelled_at: active.requested_at,
        pending_refund: true,
        refund_request_id: active.refund_request_id,
      },
      message: "Yeu cau huy don da duoc ghi nhan. Don hang dang cho hoan tien.",
    };
  }

  if (
    summary.payment_method === "VNPAY" &&
    summary.order_payment_status === "PAID" &&
    summary.order_status === "PROCESSING" &&
    !hasBlockingShipment(summary)
  ) {
    return queueRefundRequestForOrder(orderId, summary, "BUYER", String(reason ?? "").trim() || "BUYER_CANCELLED");
  }

  if (
    !CANCELLABLE_ORDER_STATUSES.has(summary.order_status) ||
    summary.order_payment_status !== "PENDING" ||
    hasBlockingShipment(summary) ||
    (summary.order_status === "PROCESSING" && summary.payment_method !== "COD")
  ) {
    return { error: "COMMERCE-409-ORDER-NOT-CANCELLABLE", status: 409 };
  }

  const cancelledAt = new Date().toISOString();
  const auditNote = String(reason ?? "").trim() || "BUYER_CANCELLED";

  updateOrderSummaryForUser(userId, orderId, (order) => ({
    ...order,
    order_status: "CANCELLED",
    order_payment_status: "CANCELLED",
    completed_at: null,
    payment: order.payment
      ? { ...order.payment, status: "CANCELLED" }
      : null,
    shipment_summary: order.shipment_summary?.shipment_count
      ? {
          shipment_count: order.shipment_summary.shipment_count,
          statuses: order.shipment_summary.statuses.map(() => "CANCELLED"),
        }
      : { shipment_count: 0, statuses: [] },
    _cancel_note: auditNote,
  }));

  return {
    data: {
      order_id: orderId,
      status: "CANCELLED",
      cancelled_at: cancelledAt,
    },
    message: "Huy don hang thanh cong.",
  };
}

export function confirmOrderReceivedForUser(userId, orderId) {
  const summary = findOrderSummaryForUser(userId, orderId);
  if (!summary) {
    return { error: "COMMERCE-404-ORDER", status: 404 };
  }

  if (summary.order_status === "CANCELLED") {
    return { error: "COMMERCE-409-ORDER-NOT-CANCELLABLE", status: 409 };
  }

  if (summary.order_status === "COMPLETED") {
    return {
      data: {
        order_id: summary.order_id,
        order_status: "COMPLETED",
        payment_status: summary.order_payment_status,
        items_completed: 0,
        payment_marked_paid: false,
        order_completed: true,
        already_confirmed: true,
      },
      message: "Don hang da duoc xac nhan nhan hang truoc do.",
    };
  }

  const detail = buildDetailFromSummary(summary);
  const deliveredItems = detail.items.filter((item) => item.status === "DELIVERED");

  if (!deliveredItems.length) {
    return { error: "COMMERCE-409-ORDER-ITEMS", status: 409 };
  }

  const isCod = summary.payment_method === "COD";
  const paymentPending =
    summary.order_payment_status === "PENDING" || summary.payment?.status === "PENDING";

  if (!isCod && summary.order_payment_status !== "PAID") {
    return { error: "COMMERCE-409-PAYMENT-STATE", status: 409 };
  }

  const now = new Date().toISOString();
  let paymentMarkedPaid = false;

  updateOrderSummaryForUser(userId, orderId, (order) => {
    const nextPayment =
      isCod && paymentPending
        ? {
            ...(order.payment || {}),
            status: "PAID",
          }
        : order.payment;

    paymentMarkedPaid = Boolean(isCod && paymentPending);

    return {
      ...order,
      order_payment_status: "PAID",
      payment: nextPayment,
      order_status: "COMPLETED",
      completed_at: now,
      shipment_summary: order.shipment_summary,
    };
  });

  const itemsCompleted = deliveredItems.length;

  return {
    data: {
      order_id: orderId,
      order_status: "COMPLETED",
      payment_status: "PAID",
      items_completed: itemsCompleted,
      payment_marked_paid: paymentMarkedPaid,
      order_completed: true,
    },
    message: "Xac nhan da nhan hang thanh cong.",
  };
}
