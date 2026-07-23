import {
  SHOP_MODERATION_LIST_SORT_OPTIONS,
  SHOP_MODERATION_STATUS_OPTIONS,
} from "../constants/shopModerationListConstants.js";
import { getShopStatusLabel } from "../constants/shopModerationDisplayLabels.js";

function optionLabel(options, value) {
  return options.find((item) => item.value === value)?.label || value;
}

export function buildShopModerationActiveFilterChips(filters) {
  if (!filters) return [];

  const chips = [];

  if (filters.q) {
    chips.push({ key: "q", label: `Tìm: ${filters.q}` });
  }
  if (filters.status) {
    chips.push({ key: "status", label: `Trạng thái: ${getShopStatusLabel(filters.status)}` });
  }
  if (filters.sort && filters.sort !== "NEWEST") {
    chips.push({
      key: "sort",
      label: `Sắp xếp: ${optionLabel(SHOP_MODERATION_LIST_SORT_OPTIONS, filters.sort)}`,
    });
  }

  return chips;
}

export function removeShopModerationFilterChip(filters, chipKey) {
  const next = { ...filters, page: "1" };

  if (chipKey === "q") next.q = "";
  if (chipKey === "status") next.status = "";
  if (chipKey === "sort") next.sort = "NEWEST";

  return next;
}

export function buildShopModerationQuickFilter(preset) {
  const base = {
    q: "",
    sort: "NEWEST",
    page: "1",
  };

  if (preset === "active") {
    return { ...base, status: "ACTIVE" };
  }
  if (preset === "needs_attention" || preset === "suspended") {
    return { ...base, status: "SUSPENDED" };
  }
  if (preset === "closed") {
    return { ...base, status: "CLOSED" };
  }

  return { ...base, status: "" };
}

export function isShopModerationQuickPresetActive(filters, preset) {
  if (preset === "active") {
    return filters?.status === "ACTIVE" && !filters?.q;
  }
  if (preset === "needs_attention" || preset === "suspended") {
    return filters?.status === "SUSPENDED" && !filters?.q;
  }
  if (preset === "closed") {
    return filters?.status === "CLOSED" && !filters?.q;
  }
  if (preset === "all") {
    return !filters?.status && !filters?.q;
  }
  return false;
}

export function shopModerationStatusOptionLabel(value) {
  return optionLabel(SHOP_MODERATION_STATUS_OPTIONS, value);
}
