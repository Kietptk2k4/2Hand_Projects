export const SYSTEM_CONFIG_LIST_PAGE_SIZE = 20;

export const SYSTEM_CONFIG_PAGE_SIZE_OPTIONS = [
  { value: "20", label: "20 / trang" },
  { value: "50", label: "50 / trang" },
  { value: "100", label: "100 / trang" },
];

export const SYSTEM_CONFIG_QUICK_FILTER_PRESETS = [
  { id: "all", label: "Tất cả" },
  { id: "active", label: "Đang bật" },
  { id: "inactive", label: "Đã tắt" },
  { id: "json", label: "JSON" },
];

export const SYSTEM_CONFIG_STAT_PRESETS = [
  { id: "active", label: "Đang bật", is_active: "true" },
  { id: "inactive", label: "Đã tắt", is_active: "false" },
];

export const SYSTEM_CONFIG_VALUE_TYPE_LABELS = {
  INTEGER: "Số nguyên",
  DECIMAL: "Số thập phân",
  STRING: "Chuỗi",
  BOOLEAN: "Boolean",
  JSON: "JSON",
};
