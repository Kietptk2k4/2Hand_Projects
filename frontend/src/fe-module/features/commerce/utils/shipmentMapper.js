function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

function mapAddress(address) {
  if (!address) return null;

  return {
    receiverName: pick(address, "receiverName", "receiver_name"),
    phone: address.phone,
    provinceCode: pick(address, "provinceCode", "province_code"),
    districtCode: pick(address, "districtCode", "district_code"),
    wardCode: pick(address, "wardCode", "ward_code"),
    addressDetail: pick(address, "addressDetail", "address_detail"),
    fullAddress: pick(address, "fullAddress", "full_address"),
  };
}

function mapStatusHistory(events) {
  return (events || []).map((event, index) => ({
    oldStatus: pick(event, "oldStatus", "old_status"),
    newStatus: pick(event, "newStatus", "new_status"),
    rawStatus: pick(event, "rawStatus", "raw_status"),
    occurredAt: pick(event, "occurredAt", "occurred_at"),
    id: `sh-history-${index}-${pick(event, "occurredAt", "occurred_at")}`,
  }));
}

function mapOrderItems(items) {
  return (items || []).map((item) => ({
    orderItemId: pick(item, "orderItemId", "order_item_id"),
    productId: pick(item, "productId", "product_id"),
    sellerId: pick(item, "sellerId", "seller_id"),
    shipmentId: pick(item, "shipmentId", "shipment_id"),
    quantity: item.quantity,
    status: item.status,
    unitPriceSnapshot: pick(item, "unitPriceSnapshot", "unit_price_snapshot"),
    finalPrice: pick(item, "finalPrice", "final_price"),
    productNameSnapshot: pick(item, "productNameSnapshot", "product_name_snapshot"),
    imageSnapshot: pick(item, "imageSnapshot", "image_snapshot"),
    attributesSnapshot: pick(item, "attributesSnapshot", "attributes_snapshot"),
    shopNameSnapshot: pick(item, "shopNameSnapshot", "shop_name_snapshot"),
  }));
}

export function mapShipmentDetailResponse(data) {
  if (!data) return null;

  return {
    shipmentId: pick(data, "shipmentId", "shipment_id"),
    orderId: pick(data, "orderId", "order_id"),
    sellerId: pick(data, "sellerId", "seller_id"),
    accessedAs: pick(data, "accessedAs", "accessed_as"),
    carrier: data.carrier,
    shipmentType: pick(data, "shipmentType", "shipment_type"),
    status: data.status,
    ghnOrderCode: pick(data, "ghnOrderCode", "ghn_order_code"),
    trackingNumber: pick(data, "trackingNumber", "tracking_number"),
    shippingFee: pick(data, "shippingFee", "shipping_fee"),
    codAmount: pick(data, "codAmount", "cod_amount"),
    weightGram: pick(data, "weightGram", "weight_gram"),
    estimatedDeliveryDate: pick(data, "estimatedDeliveryDate", "estimated_delivery_date"),
    shippedAt: pick(data, "shippedAt", "shipped_at"),
    deliveredAt: pick(data, "deliveredAt", "delivered_at"),
    createdAt: pick(data, "createdAt", "created_at"),
    updatedAt: pick(data, "updatedAt", "updated_at"),
    shippingAddress: mapAddress(data.shipping_address ?? data.shippingAddress),
    orderItems: mapOrderItems(data.order_items ?? data.orderItems),
    statusHistory: mapStatusHistory(data.status_history ?? data.statusHistory),
  };
}
