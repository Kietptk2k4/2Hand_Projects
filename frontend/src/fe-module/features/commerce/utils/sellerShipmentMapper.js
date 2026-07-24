import { mapReviewBuyerFields } from "./reviewParticipantMapper";

function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

function mapShippingAddress(addr) {
  if (!addr) return null;
  return {
    receiverName: pick(addr, "receiverName", "receiver_name"),
    phone: addr.phone,
    fullAddress: pick(addr, "fullAddress", "full_address"),
    addressDetail: pick(addr, "addressDetail", "address_detail"),
  };
}

export function mapSellerShipmentListItem(item) {
  if (!item) return null;

  return {
    shipmentId: pick(item, "shipmentId", "shipment_id"),
    orderId: pick(item, "orderId", "order_id"),
    carrier: item.carrier,
    shipmentType: pick(item, "shipmentType", "shipment_type"),
    status: item.status,
    trackingNumber: pick(item, "trackingNumber", "tracking_number"),
    ghnOrderCode: pick(item, "ghnOrderCode", "ghn_order_code"),
    deliveryAddressSummary: pick(item, "deliveryAddressSummary", "delivery_address_summary"),
    createdAt: pick(item, "createdAt", "created_at"),
    updatedAt: pick(item, "updatedAt", "updated_at"),
    orderItemCount: item.order_item_count ?? item.orderItemCount ?? 0,
  };
}

export function mapSellerShipmentListResponse(data) {
  if (!data) return { items: [], pagination: null, statusCounts: {} };

  const statusCounts = data.summary?.status_counts || data.summary?.statusCounts || {};

  return {
    items: (data.items || []).map(mapSellerShipmentListItem).filter(Boolean),
    pagination: data.pagination
      ? {
          page: data.pagination.page,
          limit: data.pagination.limit,
          totalItems: data.pagination.total_items ?? data.pagination.totalItems,
          totalPages: data.pagination.total_pages ?? data.pagination.totalPages,
          hasNext: Boolean(data.pagination.has_next ?? data.pagination.hasNext),
        }
      : null,
    statusCounts,
  };
}

export function mapSellerShipmentDetail(data) {
  if (!data) return null;

  return {
    shipmentId: pick(data, "shipmentId", "shipment_id"),
    orderId: pick(data, "orderId", "order_id"),
    sellerId: pick(data, "sellerId", "seller_id"),
    carrier: data.carrier,
    shipmentType: pick(data, "shipmentType", "shipment_type"),
    status: data.status,
    ghnOrderCode: pick(data, "ghnOrderCode", "ghn_order_code"),
    trackingNumber: pick(data, "trackingNumber", "tracking_number"),
    shippingFee: data.shipping_fee ?? data.shippingFee,
    codAmount: data.cod_amount ?? data.codAmount,
    weightGram: data.weight_gram ?? data.weightGram,
    estimatedDeliveryDate: pick(data, "estimatedDeliveryDate", "estimated_delivery_date"),
    shippedAt: pick(data, "shippedAt", "shipped_at"),
    deliveredAt: pick(data, "deliveredAt", "delivered_at"),
    createdAt: pick(data, "createdAt", "created_at"),
    updatedAt: pick(data, "updatedAt", "updated_at"),
    shippingAddress: mapShippingAddress(data.shipping_address || data.shippingAddress),
    orderItems: (data.order_items || data.orderItems || []).map((row) => ({
      orderItemId: pick(row, "orderItemId", "order_item_id"),
      productNameSnapshot: pick(row, "productNameSnapshot", "product_name_snapshot"),
      quantity: row.quantity,
      status: row.status,
    })),
    ...mapReviewBuyerFields(data),
  };
}

export function mapCreateShipmentPayload(form) {
  const payload = {
    order_id: form.orderId,
    order_item_ids: form.orderItemIds,
    carrier: form.carrier,
    shipment_type: form.shipmentType,
  };
  if (form.weightGram) payload.weight_gram = Number(form.weightGram);
  if (form.trackingNumber?.trim()) payload.tracking_number = form.trackingNumber.trim();
  return payload;
}

export function mapCreateShipmentResponse(data) {
  return mapSellerShipmentDetail(data);
}

export function mapUpdateShipmentPayload({ status, trackingNumber }) {
  const payload = {};
  if (status) payload.status = status;
  if (trackingNumber !== undefined && trackingNumber !== null) {
    payload.tracking_number = trackingNumber;
  }
  return payload;
}

export function mapGhnPrintLabelResponse(data) {
  if (!data) return null;
  return {
    shipmentId: pick(data, "shipmentId", "shipment_id"),
    ghnOrderCode: pick(data, "ghnOrderCode", "ghn_order_code"),
    format: data.format,
    printToken: pick(data, "printToken", "print_token"),
    printUrl: pick(data, "printUrl", "print_url"),
    expiresInMinutes: data.expires_in_minutes ?? data.expiresInMinutes,
  };
}
