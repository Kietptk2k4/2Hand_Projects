import {
  ORDER_PAYMENT_STATUS_LABELS,
  ORDER_STATUS_LABELS,
} from "../constants/orderListConstants";
import { PAYMENT_STATUS_LABELS } from "../constants/paymentStatusLabels";

export function formatOrderStatusLabel(status) {
  if (!status) return "";
  return ORDER_STATUS_LABELS[status] || status;
}

export function formatOrderPaymentStatusLabel(status) {
  if (!status) return "";
  return ORDER_PAYMENT_STATUS_LABELS[status] || status;
}

export function formatPaymentStatusLabel(status) {
  if (!status) return "";
  return PAYMENT_STATUS_LABELS[status] || status;
}
