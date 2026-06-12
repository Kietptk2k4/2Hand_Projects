import { PAYMENT_STATUS } from "./paymentConstants";

export const PAYMENT_STATUS_LABELS = {
  [PAYMENT_STATUS.PENDING]: "Đang chờ thanh toán",
  [PAYMENT_STATUS.PAID]: "Đã thanh toán",
  [PAYMENT_STATUS.FAILED]: "Thanh toán thất bại",
  [PAYMENT_STATUS.CANCELLED]: "Thanh toán đã hủy",
  [PAYMENT_STATUS.EXPIRED]: "Thanh toán hết hạn",
};

const RETRYABLE_ORDER_STATUSES = new Set(["CREATED", "AWAITING_PAYMENT"]);

export function canRetryPayment({ paymentStatus, orderStatus }) {
  if (!paymentStatus || !orderStatus) return false;
  if (orderStatus === "CANCELLED") return false;
  if (paymentStatus === PAYMENT_STATUS.CANCELLED) return false;

  if (paymentStatus === PAYMENT_STATUS.PENDING) {
    return RETRYABLE_ORDER_STATUSES.has(orderStatus);
  }

  if (paymentStatus === PAYMENT_STATUS.FAILED || paymentStatus === PAYMENT_STATUS.EXPIRED) {
    return RETRYABLE_ORDER_STATUSES.has(orderStatus);
  }

  return false;
}

export function isOrderCancelledDueToPayment(orderStatus, orderPaymentStatus) {
  if (orderStatus !== "CANCELLED") return false;
  return (
    orderPaymentStatus === PAYMENT_STATUS.FAILED ||
    orderPaymentStatus === PAYMENT_STATUS.CANCELLED ||
    orderPaymentStatus === PAYMENT_STATUS.EXPIRED
  );
}