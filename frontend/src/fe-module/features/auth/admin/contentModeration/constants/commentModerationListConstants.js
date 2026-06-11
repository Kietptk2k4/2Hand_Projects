export const COMMENT_MODERATION_LIST_PAGE_SIZE = 20;

export const COMMENT_MODERATION_LIST_SORT_OPTIONS = [
  { value: "created_at", label: "Ngay tao (moi nhat)" },
  { value: "updated_at", label: "Cap nhat gan day" },
  { value: "like_count", label: "Luot thich" },
];

export const COMMENT_MODERATION_LIST_STATUS_OPTIONS = [
  { value: "", label: "Tat ca trang thai" },
  { value: "ACTIVE", label: "ACTIVE" },
  { value: "DELETED", label: "DELETED" },
];
