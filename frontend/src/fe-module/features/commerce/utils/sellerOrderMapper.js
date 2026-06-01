function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

function mapPayment(payment) {
  if (!payment) return null;
  return {
    paymentId: pick(payment, "paymentId", "payment_id"),
    status: payment.status,
    paymentMethod: pick(payment, "paymentMethod", "payment_method"),
    amount: payment.amount,
    currency: payment.currency,
  };
}

function mapShipmentSummary(summary) {
  if (!summary) return null;
  return {
    shipmentId: pick(summary, "shipmentId", "shipment_id"),
    status: summary.status,
    carrier: summary.carrier,
    trackingNumber: pick(summary, "trackingNumber", "tracking_number"),
    deliveryAddressSummary: pick(
      summary,
      "deliveryAddressSummary",
      "delivery_address_summary",
    ),
  };
}

export function mapSellerOrderListItem(item) {
  if (!item) return null;

  return {
    orderItemId: pick(item, "orderItemId", "order_item_id"),
    orderId: pick(item, "orderId", "order_id"),
    productId: pick(item, "productId", "product_id"),
    quantity: item.quantity,
    unitPriceSnapshot: item.unit_price_snapshot ?? item.unitPriceSnapshot,
    finalPrice: item.final_price ?? item.finalPrice,
    shippingFeeAllocated: item.shipping_fee_allocated ?? item.shippingFeeAllocated,
    productNameSnapshot: pick(item, "productNameSnapshot", "product_name_snapshot"),
    imageSnapshot: pick(item, "imageSnapshot", "image_snapshot"),
    itemStatus: pick(item, "itemStatus", "item_status"),
    itemCreatedAt: pick(item, "itemCreatedAt", "item_created_at"),
    itemUpdatedAt: pick(item, "itemUpdatedAt", "item_updated_at"),
    orderStatus: pick(item, "orderStatus", "order_status"),
    orderPaymentStatus: pick(item, "orderPaymentStatus", "order_payment_status"),
    orderPaymentMethod: pick(item, "orderPaymentMethod", "order_payment_method"),
    orderCreatedAt: pick(item, "orderCreatedAt", "order_created_at"),
    payment: mapPayment(item.payment),
    shipmentSummary: mapShipmentSummary(item.shipment_summary ?? item.shipmentSummary),
  };
}

export function mapSellerOrderListResponse(data) {
  if (!data) return { items: [], pagination: null };

  const pagination = data.pagination
    ? {
        page: data.pagination.page,
        limit: data.pagination.limit,
        totalItems: data.pagination.total_items ?? data.pagination.totalItems,
        totalPages: data.pagination.total_pages ?? data.pagination.totalPages,
        hasNext: Boolean(data.pagination.has_next ?? data.pagination.hasNext),
      }
    : null;

  return {
    items: (data.items || []).map(mapSellerOrderListItem).filter(Boolean),
    pagination,
  };
}
