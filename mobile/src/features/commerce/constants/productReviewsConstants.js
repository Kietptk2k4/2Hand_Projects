export const PAGE_SIZE = 10;

export const DETAIL_PREVIEW_LIMIT = 3;

export const DEFAULT_SORT = "NEWEST";

export const SORT_OPTIONS = [
  { value: "NEWEST", label: "Mới nhất" },
  { value: "OLDEST", label: "Cũ nhất" },
  { value: "RATING_DESC", label: "Điểm cao → thấp" },
  { value: "RATING_ASC", label: "Điểm thấp → cao" },
];

export const RATING_TABS = [
  { value: null, label: "Tất cả" },
  { value: 5, label: "5 sao" },
  { value: 4, label: "4 sao" },
  { value: 3, label: "3 sao" },
  { value: 2, label: "2 sao" },
  { value: 1, label: "1 sao" },
];