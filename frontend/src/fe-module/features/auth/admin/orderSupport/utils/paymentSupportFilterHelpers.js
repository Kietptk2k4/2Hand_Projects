import {
  PAYMENT_LIST_METHOD_OPTIONS,
  PAYMENT_LIST_RECONCILIATION_OPTIONS,
  PAYMENT_LIST_STATUS_OPTIONS,
} from "../constants/paymentSupportListConstants.js";
import {
  formatPaymentMethodLabel,
  formatPaymentStatusLabel,
  formatReconciliationStatusLabel,
} from "./orderSupportDisplayUtils.js";

function optionLabel(options, value) {
  return options.find((item) => item.value === value)?.label || value;
}

export function buildPaymentSupportActiveFilterChips(filters) {
  if (!filters) return [];

  const chips = [];

  if (filters.q) {
    chips.push({ key: "q", label: `Tìm: ${filters.q}` });
  }
  if (filters.status) {
    chips.push({
      key: "status",
      label: `Trạng thái: ${formatPaymentStatusLabel(filters.status)}`,
    });
  }
  if (filters.reconciliation_status) {
    chips.push({
      key: "reconciliation_status",
      label: `Đối soát: ${formatReconciliationStatusLabel(filters.reconciliation_status)}`,
    });
  }
  if (filters.payment_method) {
    chips.push({
      key: "payment_method",
      label: `Phương thức: ${formatPaymentMethodLabel(filters.payment_method)}`,
    });
  }
  if (filters.order_id) {
    chips.push({ key: "order_id", label: `Order: ${filters.order_id}` });
  }
  if (filters.from) {
    chips.push({ key: "from", label: `Từ: ${filters.from}` });
  }
  if (filters.to) {
    chips.push({ key: "to", label: `Đến: ${filters.to}` });
  }

  return chips;
}

export function removePaymentSupportFilterChip(filters, chipKey) {
  const next = { ...filters, page: "1" };

  if (chipKey === "q") next.q = "";
  if (chipKey === "status") next.status = "";
  if (chipKey === "reconciliation_status") next.reconciliation_status = "";
  if (chipKey === "payment_method") next.payment_method = "";
  if (chipKey === "order_id") next.order_id = "";
  if (chipKey === "from") next.from = "";
  if (chipKey === "to") next.to = "";

  return next;
}

export function buildPaymentSupportQuickFilter(preset) {
  const base = {
    q: "",
    status: "",
    reconciliation_status: "",
    payment_method: "",
    order_id: "",
    from: "",
    to: "",
    page: "1",
  };

  if (preset === "paid") return { ...base, status: "PAID" };
  if (preset === "pending") return { ...base, status: "PENDING" };
  if (preset === "failed") return { ...base, status: "FAILED" };
  if (preset === "expired") return { ...base, status: "EXPIRED" };

  return base;
}

export function isPaymentSupportQuickPresetActive(filters, preset) {
  if (preset === "all") {
    return (
      !filters?.status &&
      !filters?.reconciliation_status &&
      !filters?.q &&
      !filters?.payment_method &&
      !filters?.order_id &&
      !filters?.from &&
      !filters?.to
    );
  }
  if (preset === "paid") {
    return filters?.status === "PAID" && !filters?.q && !filters?.reconciliation_status;
  }
  if (preset === "pending") {
    return filters?.status === "PENDING" && !filters?.q && !filters?.reconciliation_status;
  }
  if (preset === "failed") {
    return filters?.status === "FAILED" && !filters?.q && !filters?.reconciliation_status;
  }
  if (preset === "expired") {
    return filters?.status === "EXPIRED" && !filters?.q && !filters?.reconciliation_status;
  }
  return false;
}

export {
  PAYMENT_LIST_STATUS_OPTIONS,
  PAYMENT_LIST_METHOD_OPTIONS,
  PAYMENT_LIST_RECONCILIATION_OPTIONS,
};
