import {
  ORDER_LIST_PAYMENT_METHOD_OPTIONS,
  ORDER_LIST_PAYMENT_STATUS_OPTIONS,
  ORDER_LIST_SORT_OPTIONS,
  ORDER_LIST_STATUS_OPTIONS,
} from "../constants/orderSupportListConstants.js";
import {
  formatPaymentMethodLabel,
  formatPaymentStatusLabel,
  formatOrderStatusLabel,
} from "./orderSupportDisplayUtils.js";

function optionLabel(options, value) {
  return options.find((item) => item.value === value)?.label || value;
}

export function buildOrderSupportActiveFilterChips(filters) {
  if (!filters) return [];

  const chips = [];

  if (filters.q) {
    chips.push({ key: "q", label: `Tìm: ${filters.q}` });
  }
  if (filters.status) {
    chips.push({
      key: "status",
      label: `Trạng thái: ${formatOrderStatusLabel(filters.status)}`,
    });
  }
  if (filters.payment_status) {
    chips.push({
      key: "payment_status",
      label: `Thanh toán: ${formatPaymentStatusLabel(filters.payment_status)}`,
    });
  }
  if (filters.payment_method) {
    chips.push({
      key: "payment_method",
      label: `Phương thức: ${formatPaymentMethodLabel(filters.payment_method)}`,
    });
  }
  if (filters.from) {
    chips.push({ key: "from", label: `Từ: ${filters.from}` });
  }
  if (filters.to) {
    chips.push({ key: "to", label: `Đến: ${filters.to}` });
  }
  if (filters.sort && filters.sort !== "created_at") {
    chips.push({
      key: "sort",
      label: `Sắp xếp: ${optionLabel(ORDER_LIST_SORT_OPTIONS, filters.sort)}`,
    });
  }

  return chips;
}

export function removeOrderSupportFilterChip(filters, chipKey) {
  const next = { ...filters, page: "1" };

  if (chipKey === "q") next.q = "";
  if (chipKey === "status") next.status = "";
  if (chipKey === "payment_status") next.payment_status = "";
  if (chipKey === "payment_method") next.payment_method = "";
  if (chipKey === "from") next.from = "";
  if (chipKey === "to") next.to = "";
  if (chipKey === "sort") next.sort = "created_at";

  return next;
}

export function buildOrderSupportQuickFilter(preset) {
  const base = {
    q: "",
    status: "",
    payment_status: "",
    payment_method: "",
    from: "",
    to: "",
    sort: "created_at",
    page: "1",
  };

  if (preset === "processing") return { ...base, status: "PROCESSING" };
  if (preset === "completed") return { ...base, status: "COMPLETED" };
  if (preset === "cancelled") return { ...base, status: "CANCELLED" };
  if (preset === "awaiting_payment") return { ...base, status: "AWAITING_PAYMENT" };

  return base;
}

export function isOrderSupportQuickPresetActive(filters, preset) {
  if (preset === "all") {
    return (
      !filters?.status &&
      !filters?.payment_status &&
      !filters?.q &&
      !filters?.payment_method &&
      !filters?.from &&
      !filters?.to
    );
  }
  if (preset === "processing") {
    return filters?.status === "PROCESSING" && !filters?.q && !filters?.payment_status;
  }
  if (preset === "completed") {
    return filters?.status === "COMPLETED" && !filters?.q && !filters?.payment_status;
  }
  if (preset === "cancelled") {
    return filters?.status === "CANCELLED" && !filters?.q && !filters?.payment_status;
  }
  if (preset === "awaiting_payment") {
    return filters?.status === "AWAITING_PAYMENT" && !filters?.q && !filters?.payment_status;
  }
  return false;
}

export {
  ORDER_LIST_STATUS_OPTIONS,
  ORDER_LIST_PAYMENT_METHOD_OPTIONS,
  ORDER_LIST_PAYMENT_STATUS_OPTIONS,
  ORDER_LIST_SORT_OPTIONS,
};
