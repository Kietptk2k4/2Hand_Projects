export const COMMENT_MODERATION_LIST_PAGE_SIZE = 20;

export const COMMENT_MODERATION_LIST_SORT_OPTIONS = [
  { value: "created_at", label: "Ngày tạo (mới nhất)" },
  { value: "updated_at", label: "Cập nhật gần đây" },
  { value: "like_count", label: "Lượt thích" },
];

export const COMMENT_MODERATION_LIST_STATUS_OPTIONS = [
  { value: "", label: "Tất cả trạng thái" },
  { value: "ACTIVE", label: "ACTIVE" },
  { value: "DELETED", label: "DELETED" },
];
