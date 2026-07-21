import {
  getAuditActionLabel,
  getAuditTargetTypeLabel,
  isCriticalAuditAction,
} from "../constants/adminAuditActionLabels.js";
import { AUDIT_STATUS_LABELS } from "../constants/adminAuditConstants.js";

function startOfLocalDay(date) {
  const next = new Date(date);
  next.setHours(0, 0, 0, 0);
  return next;
}

export function buildAuditQuickDateRange(preset) {
  const now = new Date();
  const to = now.toISOString();

  if (preset === "today") {
    return { from: startOfLocalDay(now).toISOString(), to };
  }

  if (preset === "7d") {
    const fromDate = new Date(now);
    fromDate.setDate(fromDate.getDate() - 7);
    return { from: fromDate.toISOString(), to };
  }

  if (preset === "30d") {
    const fromDate = new Date(now);
    fromDate.setDate(fromDate.getDate() - 30);
    return { from: fromDate.toISOString(), to };
  }

  return { from: "", to: "" };
}

export function filterAuditLogsForDisplay(logs, filters) {
  if (!Array.isArray(logs)) return [];
  if (filters?.critical_only !== "true") return logs;
  return logs.filter((log) => isCriticalAuditAction(log.actionType));
}

export function buildAuditActiveFilterChips(filters) {
  if (!filters) return [];

  const chips = [];

  if (filters.critical_only === "true") {
    chips.push({ key: "critical_only", label: "Chỉ hành động quan trọng" });
  }
  if (filters.status) {
    chips.push({
      key: "status",
      label: `Trạng thái: ${AUDIT_STATUS_LABELS[filters.status] || filters.status}`,
    });
  }
  if (filters.admin_id) {
    chips.push({ key: "admin_id", label: `Admin: ${filters.admin_id.slice(0, 8)}…` });
  }
  if (filters.action) {
    chips.push({
      key: "action",
      label: `Hành động: ${getAuditActionLabel(filters.action)}`,
    });
  }
  if (filters.target_type) {
    chips.push({
      key: "target_type",
      label: `Đối tượng: ${getAuditTargetTypeLabel(filters.target_type)}`,
    });
  }
  if (filters.target_id) {
    chips.push({ key: "target_id", label: `Target ID: ${filters.target_id.slice(0, 10)}…` });
  }
  if (filters.from || filters.to) {
    chips.push({ key: "date_range", label: "Khoảng thời gian tùy chỉnh" });
  }

  return chips;
}

export function removeAuditFilterChip(filters, chipKey) {
  const next = { ...filters, page: "1" };

  if (chipKey === "critical_only") next.critical_only = "";
  if (chipKey === "status") next.status = "";
  if (chipKey === "admin_id") next.admin_id = "";
  if (chipKey === "action") next.action = "";
  if (chipKey === "target_type") next.target_type = "";
  if (chipKey === "target_id") next.target_id = "";
  if (chipKey === "date_range") {
    next.from = "";
    next.to = "";
  }

  return next;
}

export function isAuditQuickPresetActive(filters, preset) {
  if (preset === "critical") return filters?.critical_only === "true";
  if (preset === "failure") return filters?.status === "FAILURE";

  const range = buildAuditQuickDateRange(preset);
  return filters?.from === range.from && filters?.to === range.to && !filters?.critical_only;
}
