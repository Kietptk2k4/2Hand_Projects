const paymentsById = new Map();

const UUID_REGEX =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

function buildMockPayOsUrl(paymentId) {
  if (typeof window !== "undefined" && window.location?.origin) {
    return `${window.location.origin}/commerce/checkout/payment-result?paymentId=${paymentId}`;
  }
  return `https://pay.payos.vn/web/mock?paymentId=${paymentId}`;
}

export function registerPaymentFromCheckout(userId, checkoutResult) {
  const now = new Date();
  const expiredAt = new Date(now.getTime() + 30 * 60 * 1000);

  const record = {
    payment_id: checkoutResult.payment_id,
    order_id: checkoutResult.order_id,
    user_id: userId,
    payment_method: checkoutResult.payment_method,
    amount: checkoutResult.final_amount,
    currency: "VND",
    status: "PENDING",
    paid_at: null,
    expired_at: expiredAt.toISOString(),
    payos_checkout_url: null,
    payos_order_code: null,
    checkout_url_expired_at: null,
    order_status: checkoutResult.order_status,
    order_payment_status: checkoutResult.payment_status,
  };

  paymentsById.set(checkoutResult.payment_id, record);
  return record;
}

export function findPayment(paymentId, userId) {
  const payment = paymentsById.get(paymentId);
  if (!payment || payment.user_id !== userId) return null;
  return payment;
}

export function createOrGetPayOsUrl(paymentId, userId) {
  const payment = findPayment(paymentId, userId);
  if (!payment) {
    return { error: "COMMERCE-404-PAYMENT", status: 404 };
  }

  if (payment.payment_method !== "PAYOS") {
    return { error: "COMMERCE-400-PAYMENT-METHOD", status: 409 };
  }

  if (payment.status !== "PENDING") {
    return { error: "COMMERCE-409-PAYMENT-STATE", status: 409 };
  }

  if (payment.order_status !== "AWAITING_PAYMENT") {
    return { error: "COMMERCE-409-ORDER-AWAITING-PAYMENT", status: 409 };
  }

  const now = Date.now();
  const existingExpiry = payment.checkout_url_expired_at
    ? new Date(payment.checkout_url_expired_at).getTime()
    : 0;

  if (payment.payos_checkout_url && existingExpiry > now) {
    return {
      data: {
        payment_id: payment.payment_id,
        order_id: payment.order_id,
        payos_order_code: payment.payos_order_code,
        payos_checkout_url: payment.payos_checkout_url,
        checkout_url_expired_at: payment.checkout_url_expired_at,
      },
      reused: true,
    };
  }

  const expiry = new Date(now + 15 * 60 * 1000);
  payment.payos_order_code = String(1747800000000 + Math.floor(Math.random() * 999999));
  payment.payos_checkout_url = buildMockPayOsUrl(payment.payment_id);
  payment.checkout_url_expired_at = expiry.toISOString();

  return {
    data: {
      payment_id: payment.payment_id,
      order_id: payment.order_id,
      payos_order_code: payment.payos_order_code,
      payos_checkout_url: payment.payos_checkout_url,
      checkout_url_expired_at: payment.checkout_url_expired_at,
    },
    reused: false,
  };
}

export function getPaymentStatus(paymentId, userId, { mockPaid = false } = {}) {
  const payment = findPayment(paymentId, userId);
  if (!payment) {
    return { error: "COMMERCE-404-PAYMENT", status: 404 };
  }

  if (mockPaid && payment.status === "PENDING") {
    payment.status = "PAID";
    payment.paid_at = new Date().toISOString();
    payment.order_status = "PROCESSING";
    payment.order_payment_status = "PAID";
    payment.payos_checkout_url = null;
    payment.checkout_url_expired_at = null;
  }

  let payosCheckoutUrl = null;
  const now = Date.now();
  const urlExpiry = payment.checkout_url_expired_at
    ? new Date(payment.checkout_url_expired_at).getTime()
    : 0;

  if (
    payment.status === "PENDING" &&
    payment.payment_method === "PAYOS" &&
    payment.payos_checkout_url &&
    urlExpiry > now
  ) {
    payosCheckoutUrl = payment.payos_checkout_url;
  }

  return {
    data: {
      payment_id: payment.payment_id,
      order_id: payment.order_id,
      payment_method: payment.payment_method,
      amount: payment.amount,
      currency: payment.currency,
      status: payment.status,
      paid_at: payment.paid_at,
      expired_at: payment.expired_at,
      payos_checkout_url: payosCheckoutUrl,
      order_status: payment.order_status,
      order_payment_status: payment.order_payment_status,
    },
  };
}

export function markPaymentFailedForQa(paymentId, userId) {
  const payment = findPayment(paymentId, userId);
  if (!payment) return null;
  payment.status = "FAILED";
  payment.payos_checkout_url = null;
  return payment;
}

export { UUID_REGEX };
