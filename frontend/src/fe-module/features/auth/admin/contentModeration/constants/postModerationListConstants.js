export const POST_MODERATION_LIST_PAGE_SIZE = 20;

export const POST_MODERATION_LIST_SORT_OPTIONS = [
  { value: "created_at", label: "Ngay tao (moi nhat)" },
  { value: "updated_at", label: "Cap nhat gan day" },
  { value: "moderation_status", label: "Trang thai kiem duyet" },
  { value: "like_count", label: "Luot thich" },
];

export const POST_MODERATION_LIST_STATUS_OPTIONS = [
  { value: "", label: "Tat ca trang thai" },
  { value: "ACTIVE", label: "ACTIVE" },
  { value: "DRAFT", label: "DRAFT" },
  { value: "DELETED", label: "DELETED" },
];

export const POST_MODERATION_LIST_MODERATION_STATUS_OPTIONS = [
  { value: "", label: "Tat ca kiem duyet" },
  { value: "NONE", label: "NONE" },
  { value: "HIDDEN", label: "HIDDEN" },
  { value: "REMOVED", label: "REMOVED" },
];
