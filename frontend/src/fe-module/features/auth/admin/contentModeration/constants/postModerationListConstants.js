export const POST_MODERATION_LIST_PAGE_SIZE = 20;

export const POST_MODERATION_LIST_SORT_OPTIONS = [
  { value: "created_at", label: "Ngày tạo (mới nhất)" },
  { value: "updated_at", label: "Cập nhật gần đây" },
  { value: "moderation_status", label: "Trạng thái kiểm duyệt" },
  { value: "like_count", label: "Lượt thích" },
];

export const POST_MODERATION_LIST_STATUS_OPTIONS = [
  { value: "", label: "Tất cả trạng thái" },
  { value: "ACTIVE", label: "ACTIVE" },
  { value: "DRAFT", label: "DRAFT" },
  { value: "DELETED", label: "DELETED" },
];

export const POST_MODERATION_LIST_MODERATION_STATUS_OPTIONS = [
  { value: "", label: "Tất cả kiểm duyệt" },
  { value: "NONE", label: "NONE" },
  { value: "HIDDEN", label: "HIDDEN" },
  { value: "REMOVED", label: "REMOVED" },
];
