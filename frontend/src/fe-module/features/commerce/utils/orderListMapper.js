function mapPayment(payment) {
  if (!payment) return null;

  return {
    paymentId: payment.payment_id,
    status: payment.status,
    paymentMethod: payment.payment_method,
    amount: payment.amount,
    currency: payment.currency,
  };
}

function mapShipmentSummary(summary) {
  if (!summary) return { shipmentCount: 0, statuses: [] };

  return {
    shipmentCount: summary.shipment_count ?? 0,
    statuses: summary.statuses || [],
  };
}

export function mapOrderListItem(item) {
  if (!item) return null;

  return {
    orderId: item.order_id,
    orderStatus: item.order_status,
    orderPaymentStatus: item.order_payment_status,
    paymentMethod: item.payment_method,
    totalAmount: item.total_amount,
    finalAmount: item.final_amount,
    createdAt: item.created_at,
    updatedAt: item.updated_at,
    completedAt: item.completed_at,
    itemCount: item.item_count,
    previewProductName: item.preview_product_name,
    previewImageUrl: item.preview_image_url,
    payment: mapPayment(item.payment),
    shipmentSummary: mapShipmentSummary(item.shipment_summary),
  };
}

export function mapOrderListResponse(data) {
  if (!data) {
    return { orders: [], pagination: null };
  }

  return {
    orders: (data.orders || []).map(mapOrderListItem).filter(Boolean),
    pagination: {
      page: data.pagination?.page ?? 1,
      limit: data.pagination?.limit ?? 10,
      totalItems: data.pagination?.total_items ?? 0,
      totalPages: data.pagination?.total_pages ?? 0,
      hasNext: Boolean(data.pagination?.has_next),
    },
  };
}
