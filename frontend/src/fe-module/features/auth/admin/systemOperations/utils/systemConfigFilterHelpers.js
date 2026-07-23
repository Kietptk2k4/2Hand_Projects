import { CONFIG_ACTIVE_FILTER_OPTIONS, CONFIG_VALUE_TYPES } from "../constants/systemConfigConstants.js";
import { SYSTEM_CONFIG_VALUE_TYPE_LABELS } from "../constants/systemConfigListConstants.js";

function optionLabel(options, value) {
  return options.find((item) => item.value === value)?.label || value;
}

export function buildSystemConfigActiveFilterChips(filters) {
  if (!filters) return [];

  const chips = [];

  if (filters.q) {
    chips.push({ key: "q", label: `Tìm: ${filters.q}` });
  }
  if (filters.value_type) {
    chips.push({
      key: "value_type",
      label: `Kiểu: ${SYSTEM_CONFIG_VALUE_TYPE_LABELS[filters.value_type] || filters.value_type}`,
    });
  }
  if (filters.is_active) {
    chips.push({
      key: "is_active",
      label: `Trạng thái: ${optionLabel(CONFIG_ACTIVE_FILTER_OPTIONS, filters.is_active)}`,
    });
  }

  return chips;
}

export function removeSystemConfigFilterChip(filters, chipKey) {
  const next = { ...filters, page: "1" };

  if (chipKey === "q") next.q = "";
  if (chipKey === "value_type") next.value_type = "";
  if (chipKey === "is_active") next.is_active = "";

  return next;
}

export function buildSystemConfigQuickFilter(preset) {
  const base = {
    q: "",
    value_type: "",
    is_active: "",
    page: "1",
  };

  if (preset === "active") {
    return { ...base, is_active: "true" };
  }
  if (preset === "inactive") {
    return { ...base, is_active: "false" };
  }
  if (preset === "json") {
    return { ...base, value_type: "JSON" };
  }

  return base;
}

export function isSystemConfigQuickPresetActive(filters, preset) {
  if (preset === "active") {
    return filters?.is_active === "true" && !filters?.q && !filters?.value_type;
  }
  if (preset === "inactive") {
    return filters?.is_active === "false" && !filters?.q && !filters?.value_type;
  }
  if (preset === "json") {
    return filters?.value_type === "JSON" && !filters?.q && !filters?.is_active;
  }
  if (preset === "all") {
    return !filters?.is_active && !filters?.q && !filters?.value_type;
  }
  return false;
}
