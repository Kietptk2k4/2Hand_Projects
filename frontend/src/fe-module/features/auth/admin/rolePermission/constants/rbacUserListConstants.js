export const RBAC_USER_LIST_PAGE_SIZE = 20;

export const RBAC_USER_LIST_SORT_OPTIONS = [
  { value: "email", label: "Email (A-Z)" },
  { value: "display_name", label: "Ten hien thi (A-Z)" },
  { value: "created_at", label: "Ngay tao (moi nhat)" },
  { value: "status", label: "Trang thai" },
];

export const RBAC_USER_LIST_STATUS_OPTIONS = [
  { value: "", label: "Tat ca trang thai" },
  { value: "ACTIVE", label: "ACTIVE" },
  { value: "PENDING_VERIFICATION", label: "PENDING_VERIFICATION" },
  { value: "SUSPENDED", label: "SUSPENDED" },
];