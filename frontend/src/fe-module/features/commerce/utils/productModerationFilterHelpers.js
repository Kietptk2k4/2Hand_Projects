import {
  PRODUCT_MODERATION_LIST_SORT_OPTIONS,
  PRODUCT_MODERATION_STATUS_OPTIONS,
} from "../constants/productModerationListConstants.js";
import { getProductStatusLabel } from "../constants/productModerationDisplayLabels.js";

function optionLabel(options, value) {
  return options.find((item) => item.value === value)?.label || value;
}

export function buildProductModerationActiveFilterChips(filters) {
  if (!filters) return [];

  const chips = [];

  if (filters.q) {
    chips.push({ key: "q", label: `Tìm: ${filters.q}` });
  }
  if (filters.status) {
    chips.push({ key: "status", label: `Trạng thái: ${getProductStatusLabel(filters.status)}` });
  }
  if (filters.sort && filters.sort !== "NEWEST") {
    chips.push({
      key: "sort",
      label: `Sắp xếp: ${optionLabel(PRODUCT_MODERATION_LIST_SORT_OPTIONS, filters.sort)}`,
    });
  }

  return chips;
}

export function removeProductModerationFilterChip(filters, chipKey) {
  const next = { ...filters, page: "1" };

  if (chipKey === "q") next.q = "";
  if (chipKey === "status") next.status = "";
  if (chipKey === "sort") next.sort = "NEWEST";

  return next;
}

export function buildProductModerationQuickFilter(preset) {
  const base = {
    q: "",
    sort: "NEWEST",
    page: "1",
  };

  if (preset === "active") {
    return { ...base, status: "ACTIVE" };
  }
  if (preset === "needs_attention" || preset === "removed") {
    return { ...base, status: "REMOVED" };
  }

  return { ...base, status: "" };
}

export function isProductModerationQuickPresetActive(filters, preset) {
  if (preset === "active") {
    return filters?.status === "ACTIVE" && !filters?.q;
  }
  if (preset === "needs_attention" || preset === "removed") {
    return filters?.status === "REMOVED" && !filters?.q;
  }
  if (preset === "all") {
    return !filters?.status && !filters?.q;
  }
  return false;
}
