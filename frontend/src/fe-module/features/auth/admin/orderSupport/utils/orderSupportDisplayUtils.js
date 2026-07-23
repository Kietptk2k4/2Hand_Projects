const ORDER_STATUS_LABELS = {
  CREATED: "Mới tạo",
  AWAITING_PAYMENT: "Chờ thanh toán",
  PROCESSING: "Đang xử lý",
  COMPLETED: "Hoàn thành",
  CANCELLED: "Đã hủy",
};

const PAYMENT_STATUS_LABELS = {
  PAID: "Đã thanh toán",
  PENDING: "Chờ thanh toán",
  FAILED: "Thất bại",
  EXPIRED: "Hết hạn",
  OUTSTANDING: "Chưa thanh toán",
};

const PAYMENT_METHOD_LABELS = {
  COD: "COD",
  PAYOS: "PayOS",
  VNPAY: "VNPay",
};

const RECONCILIATION_STATUS_LABELS = {
  NOT_APPLICABLE: "Không áp dụng",
  RECONCILED: "Đã đối soát",
  OUTSTANDING: "Chưa đối soát",
  AWAITING_WEBHOOK: "Chờ webhook",
  WEBHOOK_RECEIVED: "Đã nhận webhook",
  TERMINAL_RECONCILED: "Kết thúc — đã đối soát",
  TERMINAL_OUTSTANDING: "Kết thúc — chưa đối soát",
};

import { SHIPMENT_STATUS_LABELS } from "../constants/shipmentOverrideConstants.js";

const CARRIER_LABELS = {
  GHN: "GHN",
  MANUAL: "Giao thủ công",
  SELF_DELIVERY: "Tự giao",
};

export function formatShipmentStatusLabel(status) {
  return SHIPMENT_STATUS_LABELS[status] || status || "—";
}

export function formatCarrierLabel(carrier) {
  return CARRIER_LABELS[carrier] || carrier || "—";
}

export function formatOrderStatusLabel(status) {
  return ORDER_STATUS_LABELS[status] || status || "—";
}

export function formatPaymentStatusLabel(status) {
  return PAYMENT_STATUS_LABELS[status] || status || "—";
}

export function formatPaymentMethodLabel(method) {
  return PAYMENT_METHOD_LABELS[method] || method || "—";
}

export function formatReconciliationStatusLabel(status) {
  return RECONCILIATION_STATUS_LABELS[status] || status || "—";
}

export function truncateUuid(value, head = 8, tail = 4) {
  if (!value || value.length <= head + tail + 1) return value || "—";
  return `${value.slice(0, head)}…${value.slice(-tail)}`;
}

export async function copyToClipboard(value) {
  if (!value) return false;
  try {
    await navigator.clipboard.writeText(value);
    return true;
  } catch {
    return false;
  }
}
