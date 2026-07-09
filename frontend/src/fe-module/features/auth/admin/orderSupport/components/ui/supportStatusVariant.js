const SUCCESS = new Set([
  "PAID",
  "DELIVERED",
  "COMPLETED",
  "PROCESSED",
  "RECONCILED",
  "ACTIVE",
  "VISIBLE",
  "CONFIRMED",
]);

const DANGER = new Set([
  "FAILED",
  "CANCELLED",
  "INVALID_SIGNATURE",
  "REMOVED",
  "HIDDEN",
  "REJECTED",
  "EXPIRED",
  "RETURNED",
]);

const WARNING = new Set([
  "PENDING",
  "OUTSTANDING",
  "AWAITING_WEBHOOK",
  "PICKING_UP",
  "READY_TO_SHIP",
  "OUT_OF_STOCK",
  "REQUESTED",
  "AWAITING_PAYMENT",
]);

const ACTIVE = new Set(["PROCESSING", "SHIPPED", "CREATED"]);

export function supportStatusVariant(status) {
  if (!status) return "neutral";
  if (SUCCESS.has(status)) return "success";
  if (DANGER.has(status)) return "danger";
  if (WARNING.has(status)) return "warning";
  if (ACTIVE.has(status)) return "active";
  return "neutral";
}
