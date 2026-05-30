/** Chuỗi UI dùng chung — chỉ hiển thị, không phải giá trị API */

export const SHOW_PASSWORD = "Hiện";
export const HIDE_PASSWORD = "Ẩn";

export const SESSION_EXPIRED_TITLE = "Phiên đăng nhập đã hết hạn";
export const SESSION_EXPIRED_DEFAULT_MESSAGE = "Vui lòng đăng nhập lại để tiếp tục.";
export const SESSION_EXPIRED_SIGN_IN = "Đăng nhập";
export const SESSION_EXPIRED_CLOSE = "Đóng";

export const GENERIC_ERROR_RETRY = "Có lỗi xảy ra. Vui lòng thử lại.";
export const INVALID_FIELD_MESSAGE = "Trường dữ liệu không hợp lệ.";
export const NOT_UPDATED = "Chưa cập nhật";

export const USER_STATUS_LABELS = {
  ACTIVE: "Đang hoạt động",
  PENDING_VERIFICATION: "Chờ xác thực email",
  SUSPENDED: "Đã đình chỉ",
  DELETED: "Đã xóa",
  UNKNOWN: "Không xác định",
};

export const SESSION_STATUS_LABELS = {
  ACTIVE: "Đang hoạt động",
  LOGGED_OUT: "Đã đăng xuất",
  REVOKED: "Đã thu hồi",
  EXPIRED: "Hết hạn",
};

export function getUserStatusLabel(status) {
  return USER_STATUS_LABELS[status] || USER_STATUS_LABELS.UNKNOWN;
}

export function getSessionStatusLabel(status) {
  return SESSION_STATUS_LABELS[status] || status || SESSION_STATUS_LABELS.ACTIVE;
}
