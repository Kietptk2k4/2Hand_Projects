export const SHIPMENT_STATUSES = [
  "PENDING",
  "PICKING_UP",
  "READY_TO_SHIP",
  "SHIPPED",
  "DELIVERED",
  "FAILED",
  "CANCELLED",
  "RETURNED",
];

export const TERMINAL_SHIPMENT_STATUSES = new Set([
  "DELIVERED",
  "CANCELLED",
  "RETURNED",
  "FAILED",
]);

export const SHIPMENT_STATUS_LABELS = {
  PENDING: "Chờ xử lý",
  PICKING_UP: "Đang lấy hàng",
  READY_TO_SHIP: "Sẵn sàng giao",
  SHIPPED: "Đang giao",
  DELIVERED: "Đã giao",
  FAILED: "Giao thất bại",
  CANCELLED: "Đã hủy",
  RETURNED: "Hoàn trả",
};

const GHN_TRANSITIONS = {
  PENDING: ["PICKING_UP", "READY_TO_SHIP", "SHIPPED", "CANCELLED", "FAILED"],
  PICKING_UP: ["READY_TO_SHIP", "SHIPPED", "CANCELLED", "FAILED"],
  READY_TO_SHIP: ["SHIPPED", "CANCELLED", "FAILED"],
  SHIPPED: ["DELIVERED", "FAILED", "RETURNED"],
  DELIVERED: ["RETURNED"],
  FAILED: [],
  CANCELLED: [],
  RETURNED: [],
};

const MANUAL_TRANSITIONS = {
  PENDING: ["READY_TO_SHIP"],
  READY_TO_SHIP: ["SHIPPED"],
  SHIPPED: ["DELIVERED", "FAILED"],
};

export function isTerminalShipmentStatus(status) {
  return TERMINAL_SHIPMENT_STATUSES.has(status);
}

function transitionMapForCarrier(carrier) {
  if (carrier === "MANUAL" || carrier === "SELF_DELIVERY") {
    return MANUAL_TRANSITIONS;
  }
  return GHN_TRANSITIONS;
}

export function getAllowedTargetStatuses({ carrier, currentStatus, force = false }) {
  if (!currentStatus) {
    return SHIPMENT_STATUSES;
  }
  if (force) {
    return SHIPMENT_STATUSES;
  }
  if (isTerminalShipmentStatus(currentStatus)) {
    return [currentStatus];
  }
  const transitions = transitionMapForCarrier(carrier)[currentStatus] || [];
  return [currentStatus, ...transitions];
}
