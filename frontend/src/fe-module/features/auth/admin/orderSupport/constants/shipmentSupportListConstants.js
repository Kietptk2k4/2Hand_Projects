import { SHIPMENT_STATUS_LABELS } from "./shipmentOverrideConstants.js";

export const SHIPMENT_LIST_PAGE_SIZE = 20;

export const SHIPMENT_LIST_PAGE_SIZE_OPTIONS = [
  { value: "20", label: "20 / trang" },
  { value: "50", label: "50 / trang" },
  { value: "100", label: "100 / trang" },
];

export const SHIPMENT_SUPPORT_VIEW_MODES = {
  SUMMARY: "summary",
  TIMELINE: "timeline",
  WEBHOOKS: "webhooks",
  OVERRIDE: "override",
};

export const SHIPMENT_STAT_PRESETS = [
  { id: "SHIPPED", label: "Đang giao", status: "SHIPPED" },
  { id: "DELIVERED", label: "Đã giao", status: "DELIVERED" },
  { id: "PENDING", label: "Chờ xử lý", status: "PENDING" },
  { id: "FAILED", label: "Thất bại", status: "FAILED" },
];

export const SHIPMENT_LIST_SORT_OPTIONS = [
  { value: "updated_at", label: "Cập nhật gần nhất" },
  { value: "created_at", label: "Tạo gần nhất" },
  { value: "shipped_at", label: "Gửi hàng gần nhất" },
];

export const SHIPMENT_LIST_STATUS_OPTIONS = [
  { value: "", label: "Tất cả trạng thái" },
  ...Object.entries(SHIPMENT_STATUS_LABELS).map(([value, label]) => ({ value, label })),
];

export const SHIPMENT_LIST_CARRIER_OPTIONS = [
  { value: "", label: "Tất cả đơn vị" },
  { value: "GHN", label: "GHN" },
  { value: "MANUAL", label: "Giao thủ công" },
  { value: "SELF_DELIVERY", label: "Tự giao" },
];

export const SHIPMENT_QUICK_FILTER_PRESETS = [
  { id: "all", label: "Tất cả" },
  { id: "shipped", label: "Đang giao" },
  { id: "delivered", label: "Đã giao" },
  { id: "pending", label: "Chờ xử lý" },
  { id: "failed", label: "Thất bại" },
];
