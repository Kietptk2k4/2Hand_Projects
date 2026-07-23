import {
  SHIPMENT_LIST_CARRIER_OPTIONS,
  SHIPMENT_LIST_SORT_OPTIONS,
  SHIPMENT_LIST_STATUS_OPTIONS,
} from "../constants/shipmentSupportListConstants.js";
import {
  formatCarrierLabel,
  formatShipmentStatusLabel,
} from "./orderSupportDisplayUtils.js";

function sortLabel(value) {
  return SHIPMENT_LIST_SORT_OPTIONS.find((item) => item.value === value)?.label || value;
}

export function buildShipmentSupportActiveFilterChips(filters) {
  if (!filters) return [];

  const chips = [];

  if (filters.q) {
    chips.push({ key: "q", label: `Tìm: ${filters.q}` });
  }
  if (filters.status) {
    chips.push({
      key: "status",
      label: `Trạng thái: ${formatShipmentStatusLabel(filters.status)}`,
    });
  }
  if (filters.carrier) {
    chips.push({
      key: "carrier",
      label: `Đơn vị: ${formatCarrierLabel(filters.carrier)}`,
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
  if (filters.sort && filters.sort !== "updated_at") {
    chips.push({ key: "sort", label: `Sắp xếp: ${sortLabel(filters.sort)}` });
  }

  return chips;
}

export function removeShipmentSupportFilterChip(filters, chipKey) {
  const next = { ...filters, page: "1" };

  if (chipKey === "q") next.q = "";
  if (chipKey === "status") next.status = "";
  if (chipKey === "carrier") next.carrier = "";
  if (chipKey === "order_id") next.order_id = "";
  if (chipKey === "from") next.from = "";
  if (chipKey === "to") next.to = "";
  if (chipKey === "sort") next.sort = "updated_at";

  return next;
}

export function buildShipmentSupportQuickFilter(preset) {
  const base = {
    q: "",
    status: "",
    carrier: "",
    order_id: "",
    from: "",
    to: "",
    sort: "updated_at",
    page: "1",
  };

  if (preset === "shipped") return { ...base, status: "SHIPPED" };
  if (preset === "delivered") return { ...base, status: "DELIVERED" };
  if (preset === "pending") return { ...base, status: "PENDING" };
  if (preset === "failed") return { ...base, status: "FAILED" };

  return base;
}

export function isShipmentSupportQuickPresetActive(filters, preset) {
  if (preset === "all") {
    return (
      !filters?.status &&
      !filters?.q &&
      !filters?.carrier &&
      !filters?.order_id &&
      !filters?.from &&
      !filters?.to
    );
  }
  if (preset === "shipped") {
    return filters?.status === "SHIPPED" && !filters?.q && !filters?.carrier;
  }
  if (preset === "delivered") {
    return filters?.status === "DELIVERED" && !filters?.q && !filters?.carrier;
  }
  if (preset === "pending") {
    return filters?.status === "PENDING" && !filters?.q && !filters?.carrier;
  }
  if (preset === "failed") {
    return filters?.status === "FAILED" && !filters?.q && !filters?.carrier;
  }
  return false;
}

export { SHIPMENT_LIST_STATUS_OPTIONS, SHIPMENT_LIST_CARRIER_OPTIONS };
