import { PAYMENT_STATUS_LABELS } from "../constants/paymentStatusLabels";

const ORDER_STATUS_LABELS = {
  CREATED: "Đã tạo",
  AWAITING_PAYMENT: "Chờ thanh toán",
  PAID: "Đã thanh toán",
  PROCESSING: "Đang xử lý",
  SHIPPED: "Đã giao vận chuyển",
  DELIVERED: "Đã giao",
  COMPLETED: "Hoàn tất",
  CANCELLED: "Đã hủy",
};

export function formatPaymentStatusLabel(status) {
  if (!status) return "";
  return PAYMENT_STATUS_LABELS[status] || status;
}

export function formatOrderStatusLabel(status) {
  if (!status) return "";
  return ORDER_STATUS_LABELS[status] || status;
}

export function formatOrderPaymentStatusLabel(status) {
  return formatPaymentStatusLabel(status);
}

export function formatDateTime(iso) {
  if (!iso) return "";
  try {
    return new Date(iso).toLocaleString("vi-VN");
  } catch {
    return iso;
  }
}