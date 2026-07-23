export const REVIEW_MODERATION_LIST_PAGE_SIZE = 20;

export const REVIEW_MODERATION_PAGE_SIZE_OPTIONS = [
  { value: "20", label: "20 / trang" },
  { value: "50", label: "50 / trang" },
  { value: "100", label: "100 / trang" },
];

export const REVIEW_MODERATION_LIST_SORT_OPTIONS = [
  { value: "NEWEST", label: "Mới nhất" },
  { value: "OLDEST", label: "Cũ nhất" },
  { value: "RATING_ASC", label: "Rating thấp → cao" },
  { value: "RATING_DESC", label: "Rating cao → thấp" },
];

export const REVIEW_MODERATION_STATUS_OPTIONS = [
  { value: "", label: "Tất cả trạng thái" },
  { value: "VISIBLE", label: "Công khai" },
  { value: "HIDDEN", label: "Bị ẩn" },
];

export const REVIEW_MODERATION_RATING_OPTIONS = [
  { value: "", label: "Tất cả sao" },
  { value: "5", label: "5 sao" },
  { value: "4", label: "4 sao" },
  { value: "3", label: "3 sao" },
  { value: "2", label: "2 sao" },
  { value: "1", label: "1 sao" },
];

export const REVIEW_MODERATION_QUICK_FILTER_PRESETS = [
  { id: "all", label: "Tất cả" },
  { id: "visible", label: "Công khai" },
  { id: "needs_attention", label: "Cần xử lý" },
  { id: "low_rating", label: "1 sao" },
];

export const REVIEW_MODERATION_STAT_PRESETS = [
  { id: "visible", label: "Công khai", status: "VISIBLE" },
  { id: "hidden", label: "Bị ẩn", status: "HIDDEN" },
];
