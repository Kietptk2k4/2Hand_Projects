import { ANNOUNCEMENT_SEVERITY_LABELS, ANNOUNCEMENT_STATUS_LABELS } from "../constants/announcementListConstants.js";

const STATUS_FILTER_OPTIONS = [
  { value: "", label: "Tất cả trạng thái" },
  { value: "DRAFT", label: ANNOUNCEMENT_STATUS_LABELS.DRAFT },
  { value: "SENT", label: ANNOUNCEMENT_STATUS_LABELS.SENT },
  { value: "CANCELLED", label: ANNOUNCEMENT_STATUS_LABELS.CANCELLED },
];

function optionLabel(options, value) {
  return options.find((item) => item.value === value)?.label || value;
}

export function buildAnnouncementActiveFilterChips(filters) {
  if (!filters) return [];

  const chips = [];

  if (filters.q) {
    chips.push({ key: "q", label: `Tìm: ${filters.q}` });
  }
  if (filters.status) {
    chips.push({
      key: "status",
      label: `Trạng thái: ${ANNOUNCEMENT_STATUS_LABELS[filters.status] || filters.status}`,
    });
  }
  if (filters.severity) {
    chips.push({
      key: "severity",
      label: `Mức độ: ${ANNOUNCEMENT_SEVERITY_LABELS[filters.severity] || filters.severity}`,
    });
  }

  return chips;
}

export function removeAnnouncementFilterChip(filters, chipKey) {
  const next = { ...filters, page: "1" };

  if (chipKey === "q") next.q = "";
  if (chipKey === "status") next.status = "";
  if (chipKey === "severity") next.severity = "";

  return next;
}

export function buildAnnouncementQuickFilter(preset) {
  const base = {
    q: "",
    status: "",
    severity: "",
    page: "1",
  };

  if (preset === "draft") return { ...base, status: "DRAFT" };
  if (preset === "sent") return { ...base, status: "SENT" };
  if (preset === "cancelled") return { ...base, status: "CANCELLED" };
  if (preset === "warning") return { ...base, severity: "WARNING" };

  return base;
}

export function isAnnouncementQuickPresetActive(filters, preset) {
  if (preset === "draft") {
    return filters?.status === "DRAFT" && !filters?.q && !filters?.severity;
  }
  if (preset === "sent") {
    return filters?.status === "SENT" && !filters?.q && !filters?.severity;
  }
  if (preset === "cancelled") {
    return filters?.status === "CANCELLED" && !filters?.q && !filters?.severity;
  }
  if (preset === "warning") {
    return filters?.severity === "WARNING" && !filters?.q && !filters?.status;
  }
  if (preset === "all") {
    return !filters?.status && !filters?.q && !filters?.severity;
  }
  return false;
}

export { STATUS_FILTER_OPTIONS };
