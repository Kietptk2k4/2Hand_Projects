export function mapQuoteResponse(data) {
  if (!data) return null;

  return {
    items: (data.items || []).map((item) => ({
      cartItemId: item.cart_item_id,
      unitPrice: item.unit_price,
      quantity: item.quantity,
      itemTotal: item.item_total,
      shippingFeeAllocated: item.shipping_fee_allocated,
    })),
    totalAmount: data.total_amount ?? 0,
    shippingFee: data.shipping_fee ?? 0,
    finalAmount: data.final_amount ?? 0,
    sellerShippingGroups: (data.seller_shipping_groups || []).map((group) => ({
      sellerId: group.seller_id,
      shopId: group.shop_id,
      shippingFee: group.shipping_fee,
      shipmentType: group.shipment_type,
    })),
  };
}

export function mapShippingFeeResponse(data) {
  if (!data) return null;

  return {
    sellerGroups: (data.seller_groups || []).map((group) => ({
      sellerId: group.seller_id,
      shopId: group.shop_id,
      shippingFee: group.shipping_fee,
      shippingFeeOrigin: group.shipping_fee_origin,
      estimatedDeliveryDate: group.estimated_delivery_date,
      shipmentType: group.shipment_type,
    })),
    totalShippingFee: data.total_shipping_fee ?? 0,
  };
}

export function mapCheckoutResponse(data) {
  if (!data) return null;

  return {
    orderId: data.order_id,
    paymentId: data.payment_id,
    paymentMethod: data.payment_method,
    paymentStatus: data.payment_status,
    orderStatus: data.order_status,
    finalAmount: data.final_amount,
    payosCheckoutUrl: data.payos_checkout_url,
  };
}