import {
  REFUND_LIST_PAYMENT_METHOD_OPTIONS,
  REFUND_LIST_REQUESTED_BY_OPTIONS,
  REFUND_LIST_STATUS_OPTIONS,
} from "../constants/refundSupportListConstants.js";
import {
  REFUND_REQUESTED_BY_LABELS,
  REFUND_STATUS_LABELS,
} from "./adminRefundApprovalMapper.js";
import { formatPaymentMethodLabel } from "./orderSupportDisplayUtils.js";

export function formatRefundStatusLabel(status) {
  return REFUND_STATUS_LABELS[status] || status || "—";
}

export function formatRefundRequestedByLabel(value) {
  return REFUND_REQUESTED_BY_LABELS[value] || value || "—";
}

function optionLabel(options, value) {
  return options.find((item) => item.value === value)?.label || value;
}

export function buildRefundSupportActiveFilterChips(filters) {
  if (!filters) return [];

  const chips = [];

  if (filters.q) {
    chips.push({ key: "q", label: `Tìm: ${filters.q}` });
  }
  if (filters.status) {
    chips.push({
      key: "status",
      label: `Trạng thái: ${formatRefundStatusLabel(filters.status)}`,
    });
  }
  if (filters.requested_by) {
    chips.push({
      key: "requested_by",
      label: `Người yêu cầu: ${formatRefundRequestedByLabel(filters.requested_by)}`,
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

  return chips;
}

export function removeRefundSupportFilterChip(filters, chipKey) {
  const next = { ...filters, page: "1" };

  if (chipKey === "q") next.q = "";
  if (chipKey === "status") next.status = "";
  if (chipKey === "requested_by") next.requested_by = "";
  if (chipKey === "payment_method") next.payment_method = "";
  if (chipKey === "from") next.from = "";
  if (chipKey === "to") next.to = "";

  return next;
}

export function buildRefundSupportQuickFilter(preset) {
  const base = {
    q: "",
    status: "",
    requested_by: "",
    payment_method: "",
    from: "",
    to: "",
    page: "1",
  };

  if (preset === "requested") return { ...base, status: "REQUESTED" };
  if (preset === "confirmed") return { ...base, status: "CONFIRMED" };
  if (preset === "rejected") return { ...base, status: "REJECTED" };

  return base;
}

export function isRefundSupportQuickPresetActive(filters, preset) {
  if (preset === "all") {
    return (
      !filters?.status &&
      !filters?.q &&
      !filters?.requested_by &&
      !filters?.payment_method &&
      !filters?.from &&
      !filters?.to
    );
  }
  if (preset === "requested") {
    return filters?.status === "REQUESTED" && !filters?.q && !filters?.requested_by;
  }
  if (preset === "confirmed") {
    return filters?.status === "CONFIRMED" && !filters?.q && !filters?.requested_by;
  }
  if (preset === "rejected") {
    return filters?.status === "REJECTED" && !filters?.q && !filters?.requested_by;
  }
  return false;
}

export {
  REFUND_LIST_STATUS_OPTIONS,
  REFUND_LIST_REQUESTED_BY_OPTIONS,
  REFUND_LIST_PAYMENT_METHOD_OPTIONS,
};
