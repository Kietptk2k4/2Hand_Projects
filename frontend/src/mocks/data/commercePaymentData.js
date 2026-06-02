import { updateOrderSummaryForUser } from "./commerceOrderListData";
import { syncSellerItemsForBuyerOrderStatus } from "./commerceSellerOrderData";

const paymentsById = new Map();

const UUID_REGEX =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

const FAILURE_VARIANTS = {
  FAILED: { paymentStatus: "FAILED", orderPaymentStatus: "FAILED" },
  CANCELLED: { paymentStatus: "CANCELLED", orderPaymentStatus: "CANCELLED" },
  EXPIRED: { paymentStatus: "EXPIRED", orderPaymentStatus: "EXPIRED" },
};

function buildMockPayOsUrl(paymentId) {
  if (typeof window !== "undefined" && window.location?.origin) {
    return `${window.location.origin}/commerce/checkout/payment-result?paymentId=${paymentId}`;
  }
  return `https://pay.payos.vn/web/mock?paymentId=${paymentId}`;
}

function syncOrderForPaymentFailure(userId, orderId, { orderPaymentStatus, paymentStatus }) {
  if (!orderId) return;

  updateOrderSummaryForUser(userId, orderId, (order) => ({
    ...order,
    order_status: "CANCELLED",
    order_payment_status: orderPaymentStatus,
    payment: order.payment
      ? {
          ...order.payment,
          status: paymentStatus,
        }
      : order.payment,
    shipment_summary: order.shipment_summary?.shipment_count
      ? {
          ...order.shipment_summary,
          statuses: order.shipment_summary.statuses.map(() => "CANCELLED"),
        }
      : { shipment_count: 0, statuses: [] },
  }));

  syncSellerItemsForBuyerOrderStatus(orderId, {
    orderStatus: "CANCELLED",
    orderPaymentStatus,
    paymentStatus,
    itemStatus: "CANCELLED",
  });
}

/** Alias theo tên use case AutoCancel / HandlePaymentFailure. */
export function applyPaymentFailure(paymentId, userId, terminalStatus = "EXPIRED") {
  return applyPaymentFailureForQa(paymentId, userId, { variant: terminalStatus });
}

/**
 * Mô phỏng HandlePaymentFailure / AutoCancelUnpaidOrder outcome cho MSW QA.
 */
export function applyPaymentFailureForQa(paymentId, userId, { variant = "FAILED" } = {}) {
  const payment = findPayment(paymentId, userId);
  if (!payment) return null;

  const mapping = FAILURE_VARIANTS[variant] || FAILURE_VARIANTS.FAILED;

  if (payment.status === "PENDING") {
    payment.status = mapping.paymentStatus;
    payment.order_status = "CANCELLED";
    payment.order_payment_status = mapping.orderPaymentStatus;
    payment.payos_checkout_url = null;
    payment.checkout_url_expired_at = null;
    syncOrderForPaymentFailure(userId, payment.order_id, {
      orderPaymentStatus: mapping.orderPaymentStatus,
      paymentStatus: mapping.paymentStatus,
    });
  }

  return payment;
}

/** @deprecated Dùng applyPaymentFailureForQa */
export function markPaymentFailedForQa(paymentId, userId) {
  return applyPaymentFailureForQa(paymentId, userId, { variant: "FAILED" });
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

export function getPaymentStatus(
  paymentId,
  userId,
  { mockPaid = false, mockFailed = false, mockExpired = false } = {}
) {
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
    updateOrderSummaryForUser(userId, payment.order_id, (order) => ({
      ...order,
      order_status: "PROCESSING",
      order_payment_status: "PAID",
      payment: order.payment ? { ...order.payment, status: "PAID" } : order.payment,
    }));
  } else if (mockFailed && payment.status === "PENDING") {
    applyPaymentFailureForQa(paymentId, userId, { variant: "FAILED" });
  } else if (mockExpired && payment.status === "PENDING") {
    applyPaymentFailureForQa(paymentId, userId, { variant: "EXPIRED" });
  }

  const current = findPayment(paymentId, userId) || payment;

  let payosCheckoutUrl = null;
  const now = Date.now();
  const urlExpiry = current.checkout_url_expired_at
    ? new Date(current.checkout_url_expired_at).getTime()
    : 0;

  if (
    current.status === "PENDING" &&
    current.payment_method === "PAYOS" &&
    current.payos_checkout_url &&
    urlExpiry > now
  ) {
    payosCheckoutUrl = current.payos_checkout_url;
  }

  return {
    data: {
      payment_id: current.payment_id,
      order_id: current.order_id,
      payment_method: current.payment_method,
      amount: current.amount,
      currency: current.currency,
      status: current.status,
      paid_at: current.paid_at,
      expired_at: current.expired_at,
      payos_checkout_url: payosCheckoutUrl,
      order_status: current.order_status,
      order_payment_status: current.order_payment_status,
    },
  };
}

export { UUID_REGEX };
