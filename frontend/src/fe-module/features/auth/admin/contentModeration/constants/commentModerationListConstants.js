import {
  COMMENT_MODERATION_STATUS_LABELS,
  COMMENT_STATUS_LABELS,
} from "./commentModerationDisplayLabels.js";

export const COMMENT_MODERATION_LIST_PAGE_SIZE = 20;

export const COMMENT_MODERATION_PAGE_SIZE_OPTIONS = [
  { value: "20", label: "20 / trang" },
  { value: "50", label: "50 / trang" },
  { value: "100", label: "100 / trang" },
];

export const COMMENT_MODERATION_STAT_PRESETS = [
  { id: "hidden", label: "Cần xử lý", moderation_status: "HIDDEN" },
  { id: "removed", label: "Đã gỡ", moderation_status: "REMOVED" },
  { id: "active", label: "Đang hoạt động", status: "ACTIVE", moderation_status: "NONE" },
];

export const COMMENT_MODERATION_QUICK_FILTER_PRESETS = [
  { id: "hidden", label: "Cần xử lý" },
  { id: "removed", label: "Đã gỡ" },
  { id: "deleted", label: "Đã xóa" },
  { id: "active", label: "Đang hoạt động" },
];

export const COMMENT_MODERATION_LIST_SORT_OPTIONS = [
  { value: "created_at", label: "Ngày tạo (mới nhất)" },
  { value: "updated_at", label: "Cập nhật gần đây" },
  { value: "moderation_status", label: "Trạng thái kiểm duyệt" },
  { value: "like_count", label: "Lượt thích" },
];

export const COMMENT_MODERATION_LIST_STATUS_OPTIONS = [
  { value: "", label: "Tất cả trạng thái" },
  { value: "ACTIVE", label: COMMENT_STATUS_LABELS.ACTIVE },
  { value: "DELETED", label: COMMENT_STATUS_LABELS.DELETED },
];

export const COMMENT_MODERATION_LIST_MODERATION_STATUS_OPTIONS = [
  { value: "", label: "Tất cả kiểm duyệt" },
  { value: "NONE", label: COMMENT_MODERATION_STATUS_LABELS.NONE },
  { value: "HIDDEN", label: COMMENT_MODERATION_STATUS_LABELS.HIDDEN },
  { value: "REMOVED", label: COMMENT_MODERATION_STATUS_LABELS.REMOVED },
];
