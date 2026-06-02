function mapTimeline(events) {
  return (events || []).map((event, index) => ({
    oldStatus: event.old_status,
    newStatus: event.new_status,
    changedBy: event.changed_by,
    note: event.note,
    occurredAt: event.occurred_at,
    id: `tl-${index}-${event.occurred_at}`,
  }));
}

function mapPayment(payment) {
  if (!payment) return null;

  return {
    paymentId: payment.payment_id,
    status: payment.status,
    paymentMethod: payment.payment_method,
    amount: payment.amount,
    currency: payment.currency,
    paidAt: payment.paid_at,
    expiredAt: payment.expired_at,
    checkoutUrlExpiredAt: payment.checkout_url_expired_at,
    timeline: mapTimeline(payment.timeline),
  };
}

function mapItems(items) {
  return (items || []).map((item) => ({
    orderItemId: item.order_item_id,
    productId: item.product_id,
    sellerId: item.seller_id,
    shipmentId: item.shipment_id,
    quantity: item.quantity,
    status: item.status,
    unitPriceSnapshot: item.unit_price_snapshot,
    finalPrice: item.final_price,
    skuSnapshot: item.sku_snapshot,
    productNameSnapshot: item.product_name_snapshot,
    imageSnapshot: item.image_snapshot,
    attributesSnapshot: item.attributes_snapshot,
    shopNameSnapshot: item.shop_name_snapshot,
    shippingFeeAllocated: item.shipping_fee_allocated,
    completedAt: item.completed_at,
    reviewId: item.review_id,
  }));
}

function mapShipments(shipments) {
  return (shipments || []).map((shipment) => ({
    shipmentId: shipment.shipment_id,
    sellerId: shipment.seller_id,
    status: shipment.status,
    carrier: shipment.carrier,
    trackingNumber: shipment.tracking_number,
    shippingFee: shipment.shipping_fee,
    shipmentType: shipment.shipment_type,
    estimatedDeliveryDate: shipment.estimated_delivery_date,
    shippedAt: shipment.shipped_at,
    deliveredAt: shipment.delivered_at,
    shippingAddress: shipment.shipping_address
      ? {
          receiverName: shipment.shipping_address.receiver_name,
          phone: shipment.shipping_address.phone,
          provinceCode: shipment.shipping_address.province_code,
          districtCode: shipment.shipping_address.district_code,
          wardCode: shipment.shipping_address.ward_code,
          addressDetail: shipment.shipping_address.address_detail,
          fullAddress: shipment.shipping_address.full_address,
        }
      : null,
    timeline: mapTimeline(shipment.timeline),
  }));
}

function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

export function mapCancelOrderResponse(data) {
  if (!data) return null;

  return {
    orderId: pick(data, "orderId", "order_id"),
    status: data.status,
    cancelledAt: pick(data, "cancelledAt", "cancelled_at"),
    alreadyCancelled: Boolean(data.already_cancelled ?? data.alreadyCancelled),
  };
}

export function mapConfirmOrderReceivedResponse(data) {
  if (!data) return null;

  return {
    orderId: pick(data, "orderId", "order_id"),
    orderStatus: pick(data, "orderStatus", "order_status"),
    paymentStatus: pick(data, "paymentStatus", "payment_status"),
    itemsCompleted: data.items_completed ?? data.itemsCompleted ?? 0,
    paymentMarkedPaid: Boolean(data.payment_marked_paid ?? data.paymentMarkedPaid),
    orderCompleted: Boolean(data.order_completed ?? data.orderCompleted),
    alreadyConfirmed: Boolean(data.already_confirmed ?? data.alreadyConfirmed),
  };
}

export function mapOrderDetailResponse(data) {
  if (!data) return null;

  return {
    orderId: data.order_id,
    buyerId: data.buyer_id,
    orderStatus: data.order_status,
    orderPaymentStatus: data.order_payment_status,
    paymentMethod: data.payment_method,
    totalAmount: data.total_amount,
    finalAmount: data.final_amount,
    createdAt: data.created_at,
    updatedAt: data.updated_at,
    completedAt: data.completed_at,
    payment: mapPayment(data.payment),
    items: mapItems(data.items),
    shipments: mapShipments(data.shipments),
    orderTimeline: mapTimeline(data.order_timeline),
  };
}
