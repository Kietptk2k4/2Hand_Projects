export const ORDER_LIST_SORT_OPTIONS = [
  { value: "created_at", label: "Tạo gần nhất" },
  { value: "updated_at", label: "Cập nhật gần nhất" },
];

export const ORDER_LIST_STATUS_OPTIONS = [
  { value: "", label: "Tất cả trạng thái" },
  { value: "CREATED", label: "CREATED" },
  { value: "AWAITING_PAYMENT", label: "AWAITING_PAYMENT" },
  { value: "PROCESSING", label: "PROCESSING" },
  { value: "COMPLETED", label: "COMPLETED" },
  { value: "CANCELLED", label: "CANCELLED" },
];

export const ORDER_LIST_PAYMENT_METHOD_OPTIONS = [
  { value: "", label: "Tất cả phương thức" },
  { value: "COD", label: "COD" },
  { value: "PAYOS", label: "PAYOS" },
];

export const ORDER_LIST_PAGE_SIZE = 20;
