export const PAYMENT_STATUS = {
  PENDING: "PENDING",
  PAID: "PAID",
  FAILED: "FAILED",
  CANCELLED: "CANCELLED",
  EXPIRED: "EXPIRED",
};

export const PAYOS_AUTO_REDIRECT_MS = 2000;

export const PAYMENT_STATUS_POLL_INTERVAL_MS = 5000;

export const PAYMENT_STATUS_POLL_MAX_ATTEMPTS = 6;

const TERMINAL_PAYMENT_STATUSES = new Set([
  PAYMENT_STATUS.PAID,
  PAYMENT_STATUS.FAILED,
  PAYMENT_STATUS.CANCELLED,
  PAYMENT_STATUS.EXPIRED,
]);

export function isTerminalPaymentStatus(status) {
  return TERMINAL_PAYMENT_STATUSES.has(status);
}
