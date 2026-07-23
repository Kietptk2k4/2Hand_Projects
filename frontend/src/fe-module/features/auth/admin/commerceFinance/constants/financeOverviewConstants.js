export const FINANCE_RANGE_PRESETS = [
  { id: "7d", label: "7 ngày", days: 7 },
  { id: "30d", label: "30 ngày", days: 30 },
  { id: "90d", label: "90 ngày", days: 90 },
];

export const FINANCE_GRANULARITY_OPTIONS = [
  { value: "DAY", label: "Theo ngày" },
  { value: "WEEK", label: "Theo tuần" },
  { value: "MONTH", label: "Theo tháng" },
];

export const FINANCE_DEFAULT_RANGE_DAYS = 30;
export const FINANCE_DEFAULT_GRANULARITY = "DAY";
export const FINANCE_DEFAULT_TOP_SELLERS_LIMIT = 25;
export const FINANCE_TOP_SELLERS_LIMIT_OPTIONS = [
  { value: 10, label: "Top 10" },
  { value: 25, label: "Top 25" },
  { value: 50, label: "Top 50" },
];

export const PAYOUT_STATUS_LABELS = {
  REQUESTED: "Chờ duyệt",
  APPROVED: "Đã duyệt",
  PAID: "Đã trả",
  REJECTED: "Từ chối",
  CANCELLED: "Đã hủy",
};

export const COD_BUCKET_LABELS = {
  inTransit: "Đang vận chuyển",
  pendingConfirm: "Chờ xác nhận",
  recognized: "Đã ghi nhận",
};
