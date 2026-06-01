export function mapPayOsCheckoutUrlResponse(data) {
  if (!data) return null;

  return {
    paymentId: data.payment_id,
    orderId: data.order_id,
    payosOrderCode: data.payos_order_code,
    payosCheckoutUrl: data.payos_checkout_url,
    checkoutUrlExpiredAt: data.checkout_url_expired_at,
  };
}

export function mapPaymentStatusResponse(data) {
  if (!data) return null;

  return {
    paymentId: data.payment_id,
    orderId: data.order_id,
    paymentMethod: data.payment_method,
    amount: data.amount,
    currency: data.currency,
    status: data.status,
    paidAt: data.paid_at,
    expiredAt: data.expired_at,
    payosCheckoutUrl: data.payos_checkout_url,
    orderStatus: data.order_status,
    orderPaymentStatus: data.order_payment_status,
  };
}
