/** Chuỗi UI dùng chung — chỉ hiển thị, không phải giá trị API */

export const NOT_UPDATED = "Chưa cập nhật";

export const USER_STATUS_LABELS = {
  ACTIVE: "Đang hoạt động",
  PENDING_VERIFICATION: "Chờ xác thực email",
  SUSPENDED: "Đã đình chỉ",
  DELETED: "Đã xóa",
  UNKNOWN: "Không xác định",
};

export const APPEARANCE_LABELS = {
  LIGHT: "Sáng",
  DARK: "Tối",
  SYSTEM: "Theo hệ thống",
};

export function getUserStatusLabel(status) {
  return USER_STATUS_LABELS[status] || USER_STATUS_LABELS.UNKNOWN;
}