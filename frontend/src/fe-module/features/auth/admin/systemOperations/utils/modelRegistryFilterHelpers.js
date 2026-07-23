import { ARTIFACT_STATUS_LABELS } from "../constants/modelRegistryConstants.js";

export function buildModelRegistryActiveFilterChips(filters) {
  if (!filters?.status) return [];

  return [
    {
      key: "status",
      label: `Trạng thái: ${ARTIFACT_STATUS_LABELS[filters.status] || filters.status}`,
    },
  ];
}

export function removeModelRegistryFilterChip(filters, chipKey) {
  const next = { ...filters };
  if (chipKey === "status") next.status = "";
  return next;
}

export function buildModelRegistryQuickFilter(preset) {
  const base = { status: "" };

  if (preset === "active") return { ...base, status: "active" };
  if (preset === "rejected") return { ...base, status: "rejected" };
  if (preset === "inactive") return { ...base, status: "inactive" };

  return base;
}

export function isModelRegistryQuickPresetActive(filters, preset) {
  if (preset === "all") return !filters?.status;
  return filters?.status === preset;
}
