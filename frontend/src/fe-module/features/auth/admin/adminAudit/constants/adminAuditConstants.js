import {
  AUDIT_ACTION_LABELS,
  AUDIT_TARGET_TYPE_LABELS,
} from "./adminAuditActionLabels.js";

export const AUDIT_PAGE_SIZE = 20;

export const AUDIT_PAGE_SIZE_OPTIONS = [
  { value: "20", label: "20 / trang" },
  { value: "50", label: "50 / trang" },
  { value: "100", label: "100 / trang" },
];

export const AUDIT_STATUS_LABELS = {
  SUCCESS: "Thành công",
  FAILURE: "Thất bại",
};

export const AUDIT_STATUS_OPTIONS = [
  { value: "", label: "Tất cả trạng thái" },
  { value: "SUCCESS", label: AUDIT_STATUS_LABELS.SUCCESS },
  { value: "FAILURE", label: AUDIT_STATUS_LABELS.FAILURE },
];

export const AUDIT_ACTION_OPTIONS = [
  { value: "", label: "Tất cả hành động" },
  ...Object.entries(AUDIT_ACTION_LABELS)
    .map(([value, label]) => ({ value, label }))
    .sort((a, b) => a.label.localeCompare(b.label, "vi")),
];

export const AUDIT_TARGET_TYPE_OPTIONS = [
  { value: "", label: "Tất cả đối tượng" },
  ...Object.entries(AUDIT_TARGET_TYPE_LABELS)
    .map(([value, label]) => ({ value, label }))
    .sort((a, b) => a.label.localeCompare(b.label, "vi")),
];

export const AUDIT_QUICK_FILTER_PRESETS = [
  { id: "today", label: "Hôm nay" },
  { id: "7d", label: "7 ngày" },
  { id: "30d", label: "30 ngày" },
  { id: "critical", label: "Quan trọng" },
  { id: "failure", label: "Thất bại" },
];

export const MOCK_AUDIT_LOG_ID = "d1111111-1111-4111-8111-111111111001";